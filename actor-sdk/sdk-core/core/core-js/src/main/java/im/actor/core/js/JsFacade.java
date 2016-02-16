/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.user.client.Event;

import im.actor.core.*;
import im.actor.core.api.ApiAuthSession;
import im.actor.core.api.ApiDialog;
import im.actor.core.api.rpc.ResponseDoCall;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.MessageSearchEntity;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerSearchType;
import im.actor.core.entity.PeerType;
import im.actor.core.js.entity.*;
import im.actor.core.js.modules.JsBindedValueCallback;
import im.actor.core.js.providers.JsNotificationsProvider;
import im.actor.core.js.providers.JsPhoneBookProvider;
import im.actor.core.js.providers.JsCallsProvider;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.utils.HtmlMarkdownUtils;
import im.actor.core.js.utils.IdentityUtils;
import im.actor.core.modules.internal.messages.entity.EntityConverter;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.js.JsFileSystemProvider;
import im.actor.runtime.js.fs.JsBlob;
import im.actor.runtime.js.fs.JsFile;
import im.actor.runtime.js.mvvm.JsDisplayListCallback;
import im.actor.runtime.js.utils.JsPromise;
import im.actor.runtime.js.utils.JsPromiseExecutor;
import im.actor.runtime.markdown.MarkdownParser;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import java.util.Date;
import java.util.List;

@ExportPackage("actor")
@Export("ActorApp")
public class JsFacade implements Exportable {

    private static final String TAG = "JsMessenger";

    private static final String APP_NAME = "Actor Web App";
    private static final int APP_ID = 3;
    private static final String APP_KEY = "278f13e07eee8398b189bced0db2cf66703d1746e2b541d85f5b42b1641aae0e";

    private static final String[] EndpointsProduction = {
            "wss://front1-ws-mtproto-api-rev2.actor.im/",
            "wss://front2-ws-mtproto-api-rev2.actor.im/"
    };

    private static final String[] EndpointsDev1 = {
            "wss://front1-ws-mtproto-api-rev2-dev1.actor.im/"
    };

    private JsMessenger messenger;
    private JsFileSystemProvider provider;
    private Peer lastVisiblePeer;

    @Export
    public static JsFacade production() {
        return new JsFacade(EndpointsProduction);
    }

    @Export
    public static JsFacade dev1() {
        return new JsFacade(EndpointsDev1);
    }

    @Export
    public JsFacade() {
        this(EndpointsProduction);
    }

    @Export
    public JsFacade(String[] endpoints) {

        provider = (JsFileSystemProvider) Storage.getFileSystemRuntime();

        String clientName = IdentityUtils.getClientName();
        String uniqueId = IdentityUtils.getUniqueId();

        ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration.setApiConfiguration(new ApiConfiguration(APP_NAME, APP_ID, APP_KEY, clientName, uniqueId));
        configuration.setPhoneBookProvider(new JsPhoneBookProvider());
        configuration.setNotificationProvider(new JsNotificationsProvider());
        configuration.setCallsProvider(new JsCallsProvider());

        // Setting locale
        String locale = LocaleInfo.getCurrentLocale().getLocaleName();
        if (locale.equals("default")) {
            Log.d(TAG, "Default locale found");
            configuration.addPreferredLanguage("en");
        } else {
            Log.d(TAG, "Locale found:" + locale);
            configuration.addPreferredLanguage(locale.toLowerCase());
        }

        // Setting timezone
        int offset = new Date().getTimezoneOffset();
        String timeZone = TimeZone.createTimeZone(offset).getID();
        Log.d(TAG, "TimeZone found:" + timeZone + " for delta " + offset);
        configuration.setTimeZone(timeZone);

        // LocaleInfo.getCurrentLocale().getLocaleName()

        // Is Web application
        configuration.setPlatformType(PlatformType.WEB);

        // Device Category
        // Only Desktop is supported for JS library
        configuration.setDeviceCategory(DeviceCategory.DESKTOP);

        // Adding endpoints
        for (String endpoint : endpoints) {
            configuration.addEndpoint(endpoint);
        }

        messenger = new JsMessenger(configuration.build());

        Log.d(TAG, "JsMessenger created");
    }

    public boolean isLoggedIn() {
        return messenger.isLoggedIn();
    }

    public int getUid() {
        return messenger.myUid();
    }

    public boolean isElectron() {
        return messenger.isElectron();
    }

    // Auth

    public String getAuthState() {
        return Enums.convert(messenger.getAuthState());
    }

    public String getAuthPhone() {
        return "" + messenger.getAuthPhone();
    }

    public void requestSms(String phone, final JsAuthSuccessClosure success,
                           final JsAuthErrorClosure error) {
        try {
            long res = Long.parseLong(phone);
            messenger.requestStartPhoneAuth(res).start(new CommandCallback<AuthState>() {
                @Override
                public void onResult(AuthState res) {
                    success.onResult(Enums.convert(res));
                }

                @Override
                public void onError(Exception e) {
                    String tag = "INTERNAL_ERROR";
                    String message = "Internal error";
                    boolean canTryAgain = false;
                    if (e instanceof RpcException) {
                        tag = ((RpcException) e).getTag();
                        message = e.getMessage();
                        canTryAgain = ((RpcException) e).isCanTryAgain();
                    }
                    error.onError(tag, message, canTryAgain, getAuthState());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e);
            im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    error.onError("PHONE_NUMBER_INVALID", "Invalid phone number", false,
                            getAuthState());
                }
            });
        }
    }

    public void requestCodeEmail(String email, final JsAuthSuccessClosure success,
                                 final JsAuthErrorClosure error) {
        messenger.requestStartEmailAuth(email).start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(AuthState res) {
                success.onResult(Enums.convert(res));
            }

            @Override
            public void onError(Exception e) {
                String tag = "INTERNAL_ERROR";
                String message = "Internal error";
                boolean canTryAgain = false;
                if (e instanceof RpcException) {
                    tag = ((RpcException) e).getTag();
                    message = e.getMessage();
                    canTryAgain = ((RpcException) e).isCanTryAgain();
                }
                error.onError(tag, message, canTryAgain, getAuthState());
            }
        });
    }

    public void sendCode(String code, final JsAuthSuccessClosure success,
                         final JsAuthErrorClosure error) {
        try {
            messenger.validateCode(code).start(new CommandCallback<AuthState>() {
                @Override
                public void onResult(AuthState res) {
                    success.onResult(Enums.convert(res));
                }

                @Override
                public void onError(Exception e) {
                    String tag = "INTERNAL_ERROR";
                    String message = "Internal error";
                    boolean canTryAgain = false;
                    if (e instanceof RpcException) {
                        tag = ((RpcException) e).getTag();
                        message = e.getMessage();
                        canTryAgain = ((RpcException) e).isCanTryAgain();
                    }
                    error.onError(tag, message, canTryAgain, getAuthState());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e);
            im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    error.onError("PHONE_CODE_INVALID", "Invalid code number", false,
                            getAuthState());
                }
            });
        }
    }

    public void signUp(String name, final JsAuthSuccessClosure success,
                       final JsAuthErrorClosure error) {
        messenger.signUp(name, null, null).start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(AuthState res) {
                success.onResult(Enums.convert(res));
            }

            @Override
            public void onError(Exception e) {
                String tag = "INTERNAL_ERROR";
                String message = "Internal error";
                boolean canTryAgain = false;
                if (e instanceof RpcException) {
                    tag = ((RpcException) e).getTag();
                    message = e.getMessage();
                    canTryAgain = ((RpcException) e).isCanTryAgain();
                }
                error.onError(tag, message, canTryAgain, getAuthState());
            }
        });
    }

    public JsPromise loadSessions() {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.loadSessions().start(new CommandCallback<List<ApiAuthSession>>() {
                    @Override
                    public void onResult(List<ApiAuthSession> res) {
                        JsArray<JsAuthSession> jsSessions = JsArray.createArray().cast();

                        for (ApiAuthSession session : res) {
                            jsSessions.push(JsAuthSession.create(session));
                        }

                        resolve(jsSessions);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e);
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise terminateSession(final int id) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.terminateSession(id).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e);
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise terminateAllSessions() {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.terminateAllSessions().start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e);
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    // Dialogs

    public void bindDialogs(JsDisplayListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedDialogList().subscribe(callback, false);
    }

    public void unbindDialogs(JsDisplayListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedDialogList().unsubscribe(callback);
    }

    public void bindGroupDialogs(JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.getDialogsGroupedList().subscribe(callback);
    }

    public void unbindGroupDialogs(JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.getDialogsGroupedList().unsubscribe(callback);
    }

    // Contacts

    public void bindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().subscribe(callback, true);
    }

    public void unbindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().unsubscribe(callback);
    }

    // Search

    public void bindSearch(JsDisplayListCallback<JsSearchEntity> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedSearchList().subscribe(callback, false);
    }

    public void unbindSearch(JsDisplayListCallback<JsSearchEntity> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedSearchList().unsubscribe(callback);
    }

    // Chats

    public void bindChat(JsPeer peer, JsDisplayListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedChatList(peer.convert()).subscribe(callback, true);
    }

    public void unbindChat(JsPeer peer, JsDisplayListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedChatList(peer.convert()).unsubscribe(callback);
    }

    public JsMessagesBind bindMessages(JsPeer peer, JsMessagesBindClosure callback) {
        if (callback == null) {
            return null;
        }
        Peer peerC = peer.convert();

        return new JsMessagesBind(callback, messenger.getSharedChatList(peerC), messenger.getConversationVM(peerC));
    }

    public void deleteMessage(JsPeer peer, String id) {
        messenger.deleteMessages(peer.convert(), new long[]{Long.parseLong(id)});
    }

    public JsPromise deleteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.deleteChat(peer.convert()).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "deleteChat:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "deleteChat:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise clearChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.clearChat(peer.convert()).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "clearChat:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "clearChat:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise archiveChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.archiveChat(peer.convert()).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "archiveChat:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "archiveChat:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise favoriteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.favouriteChat(peer.convert()).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "favouriteChat:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "favouriteChat:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise unfavoriteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.unfavoriteChat(peer.convert()).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "unfavouriteChat:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "unfavouriteChat:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    // Peers

    public JsPeer getUserPeer(int uid) {
        return JsPeer.create(Peer.user(uid));
    }

    public JsPeer getGroupPeer(int gid) {
        return JsPeer.create(Peer.group(gid));
    }

    // Users

    public JsUser getUser(int uid) {
        return messenger.getJsUser(uid).get();
    }

    public void bindUser(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).subscribe(callback);
    }

    public void unbindUser(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).unsubscribe(callback);
    }

    public void bindUserOnline(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserOnline(uid).subscribe(callback);
    }

    public void unbindUserOnline(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserOnline(uid).unsubscribe(callback);
    }

    // Groups

    public JsGroup getGroup(int gid) {
        return messenger.getJsGroup(gid).get();
    }

    public void bindGroup(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).subscribe(callback);
    }

    public void unbindGroup(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).unsubscribe(callback);
    }

    public void bindGroupOnline(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroupOnline(gid).subscribe(callback);
    }

    public void unbindGroupOnline(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroupOnline(gid).unsubscribe(callback);
    }

    // Calls

    public JsPromise doCall(final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.doCall(uid).start(new CommandCallback<Long>() {
                    @Override
                    public void onResult(Long res) {
                        Log.d(TAG, "doCall:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "doCall:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public void answerCall(String callId) {
        messenger.answerCall(Long.parseLong(callId));
    }

    public void endCall(String callId) {
        messenger.endCall(Long.parseLong(callId));
    }

    public void bindCall(String id, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsCall(id).subscribe(callback);
    }

    public void unbindCall(String id, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsCall(id).unsubscribe(callback);
    }

    // Event Bus

    public void bindEventBus(JsEventBusCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.subscribeEventBus(callback);
    }

    public void unbindEventBus(JsEventBusCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.unsubscribeEventBus(callback);
    }

    // Actions

    public void sendMessage(JsPeer peer, String text) {
        messenger.sendMessageWithMentionsDetect(peer.convert(), text);
    }

    public void sendMarkdownMessage(JsPeer peer, String text, String markdownText) {
        messenger.sendMessageWithMentionsDetect(peer.convert(), text, markdownText);
    }

    public void sendFile(JsPeer peer, JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.sendDocument(peer.convert(),
                file.getName(), file.getMimeType(), descriptor);
    }

    public void sendPhoto(final JsPeer peer, final JsFile file) {
        messenger.sendPhoto(peer.convert(), file);
    }

    public void sendClipboardPhoto(final JsPeer peer, final JsBlob blob) {
        messenger.sendClipboardPhoto(peer.convert(), blob);
    }

    public void sendVoiceMessage(final JsPeer peer, int duration, final JsBlob blob) {
        String descriptor = provider.registerUploadFile(blob);
        messenger.sendAudio(peer.convert(), "voice.opus", duration, descriptor);
    }

    // Drafts

    public void saveDraft(JsPeer peer, String text) {
        messenger.saveDraft(peer.convert(), text);
    }

    public String loadDraft(JsPeer peer) {
        return messenger.loadDraft(peer.convert());
    }

    public JsArray<JsMentionFilterResult> findMentions(int gid, String query) {
        List<MentionFilterResult> res = messenger.findMentions(gid, query);
        JsArray<JsMentionFilterResult> mentions = JsArray.createArray().cast();
        for (MentionFilterResult m : res) {
            mentions.push(JsMentionFilterResult.create(m));
        }
        return mentions;
    }

    // Typing

    public void onTyping(JsPeer peer) {
        messenger.onTyping(peer.convert());
    }

    public JsTyping getTyping(JsPeer peer) {
        return messenger.getTyping(peer.convert()).get();
    }

    public void bindTyping(JsPeer peer, JsBindedValueCallback callback) {
        messenger.getTyping(peer.convert()).subscribe(callback);
    }

    public void unbindTyping(JsPeer peer, JsBindedValueCallback callback) {
        messenger.getTyping(peer.convert()).unsubscribe(callback);
    }

    // Updating state

    public void bindConnectState(JsBindedValueCallback callback) {
        messenger.getOnlineStatus().subscribe(callback);
    }

    public void unbindConnectState(JsBindedValueCallback callback) {
        messenger.getOnlineStatus().unsubscribe(callback);
    }

    public void bindGlobalCounter(JsBindedValueCallback callback) {
        messenger.getGlobalCounter().subscribe(callback);
    }

    public void unbindGlobalCounter(JsBindedValueCallback callback) {
        messenger.getGlobalCounter().unsubscribe(callback);
    }

    public void bindTempGlobalCounter(JsBindedValueCallback callback) {
        messenger.getTempGlobalCounter().subscribe(callback);
    }

    public void unbindTempGlobalCounter(JsBindedValueCallback callback) {
        messenger.getTempGlobalCounter().unsubscribe(callback);
    }

    // Events

    public void onAppVisible() {
        // Ignore for electron runtime
        if (isElectron()) {
            return;
        }

        messenger.getJsIdleModule().onVisible();
    }

    public void onAppHidden() {
        // Ignore for electron runtime
        if (isElectron()) {
            return;
        }

        messenger.getJsIdleModule().onHidden();
    }

    public void onConversationOpen(JsPeer peer) {
        Log.d(TAG, "onConversationOpen | " + peer);
        lastVisiblePeer = peer.convert();
        messenger.onConversationOpen(lastVisiblePeer);
    }

    public void onConversationClosed(JsPeer peer) {
        Log.d(TAG, "onConversationClosed | " + peer);
        if (lastVisiblePeer != null && lastVisiblePeer.equals(peer.convert())) {
            lastVisiblePeer = null;
            Log.d(TAG, "onConversationClosed | Closing");
        }
        messenger.onConversationClosed(peer.convert());
    }

    public void onDialogsOpen() {
        messenger.onDialogsOpen();
    }

    public void onDialogsClosed() {
        messenger.onDialogsClosed();
    }

    public void onProfileOpen(int uid) {
        messenger.onProfileOpen(uid);
    }

    public void onProfileClosed(int uid) {
        messenger.onProfileClosed(uid);
    }

    public void onDialogsEnd() {
        messenger.loadMoreDialogs();
    }

    public JsPromise loadArchivedDialogs(){
        return loadArchivedDialogs(true);
    }

    public JsPromise loadMoreArchivedDialogs(){
        return loadArchivedDialogs(false);
    }

    private JsPromise loadArchivedDialogs(final boolean init){
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.loadArchivedDialogs(init, new RpcCallback<ResponseLoadArchived>() {
                    @Override
                    public void onResult(ResponseLoadArchived response) {
                        JsArray<JsDialogShort> res = JsArray.createArray().cast();
                        for (ApiDialog d : response.getDialogs()) {
                            res.push(JsDialogShort.create(messenger.buildPeerInfo(EntityConverter.convert(d.getPeer())), d.getUnreadCount()));
                        }
                        Log.d(TAG, "loadArchivedDialogs:result");
                        resolve(res);
                    }

                    @Override
                    public void onError(RpcException e) {
                        Log.d(TAG, "loadArchivedDialogs:error");
                        reject(e.getMessage());
                    }
                });
            }
        });

    }

    public void onChatEnd(JsPeer peer) {
        messenger.loadMoreHistory(peer.convert());
    }

    // Profile

    public JsPromise editMyName(final String newName) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editMyName(newName).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "editMyName:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "editMyName:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise editMyNick(final String newNick) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editMyNick(newNick).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "editMyNick:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "editMyNick:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise editMyAbout(final String newAbout) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editMyAbout(newAbout).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "editMyAbout:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "editMyAbout:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise findAllText(final JsPeer peer, final String query) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findTextMessages(peer.convert(), query).start(new CommandCallback<List<MessageSearchEntity>>() {
                    @Override
                    public void onResult(List<MessageSearchEntity> res) {
                        resolve(convertSearchRes(res));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findAllText:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise findAllPhotos(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findAllPhotos(peer.convert()).start(new CommandCallback<List<MessageSearchEntity>>() {
                    @Override
                    public void onResult(List<MessageSearchEntity> res) {
                        resolve(convertSearchRes(res));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findAllText:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise findAllDocs(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findAllDocs(peer.convert()).start(new CommandCallback<List<MessageSearchEntity>>() {
                    @Override
                    public void onResult(List<MessageSearchEntity> res) {
                        resolve(convertSearchRes(res));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findAllText:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise findAllLinks(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findAllLinks(peer.convert()).start(new CommandCallback<List<MessageSearchEntity>>() {
                    @Override
                    public void onResult(List<MessageSearchEntity> res) {
                        resolve(convertSearchRes(res));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findAllText:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    private JsArray<JsMessageSearchEntity> convertSearchRes(List<MessageSearchEntity> res) {
        JsArray<JsMessageSearchEntity> jsRes = JsArray.createArray().cast();
        for (MessageSearchEntity e : res) {
            jsRes.push(JsMessageSearchEntity.create(e.getRid() + "",
                    messenger.buildPeerInfo(Peer.user(e.getSenderId())),
                    messenger.getFormatter().formatDate(e.getDate()),
                    JsContent.createContent(e.getContent(),
                            e.getSenderId())));
        }
        return jsRes;
    }

    public JsPromise findGroups() {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findPeers(PeerSearchType.GROUPS).start(new CommandCallback<List<PeerSearchEntity>>() {
                    @Override
                    public void onResult(List<PeerSearchEntity> res) {
                        Log.d(TAG, "findGroups:result");
                        JsArray<JsPeerSearchResult> jsRes = JsArray.createArray().cast();
                        for (PeerSearchEntity s : res) {
                            if (s.getPeer().getPeerType() == PeerType.GROUP) {
                                jsRes.push(JsPeerSearchResult.create(messenger.buildPeerInfo(s.getPeer()),
                                        s.getDescription(), s.getMembersCount(), (int) (s.getDate() / 1000L),
                                        messenger.buildPeerInfo(Peer.user(s.getCreatorUid())), s.isPublic(),
                                        s.isJoined()));
                            } else if (s.getPeer().getPeerType() == PeerType.PRIVATE) {
                                jsRes.push(JsPeerSearchResult.create(messenger.buildPeerInfo(s.getPeer())));
                            }
                            // jsRes.push();
                        }
                        resolve(jsRes);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findGroups:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public void changeMyAvatar(final JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.changeMyAvatar(descriptor);
    }

    public void removeMyAvatar() {
        messenger.removeMyAvatar();
    }

    public JsPromise editName(final int uid, final String newName) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editName(uid, newName).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "editName:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "editName:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise joinGroupViaLink(final String url) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.joinGroupViaLink(url).start(new CommandCallback<Integer>() {
                    @Override
                    public void onResult(Integer res) {
                        Log.d(TAG, "joinGroupViaLink:result");
                        resolve(JsPeer.create(Peer.group(res)));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "joinGroupViaLink:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise editGroupTitle(final int gid, final String newTitle) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editGroupTitle(gid, newTitle).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "editGroupTitle:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "editGroupTitle:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise editGroupAbout(final int gid, final String newAbout) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.editGroupAbout(gid, newAbout).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e);
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public void changeGroupAvatar(final int gid, final JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.changeGroupAvatar(gid, descriptor);
    }

    public void removeGroupAvatar(final int gid) {
        messenger.removeGroupAvatar(gid);
    }

    public JsPromise createGroup(final String title, final JsFile file, final int[] uids) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                String avatarDescriptor = file != null ? provider.registerUploadFile(file) : null;
                //noinspection ConstantConditions
                messenger.createGroup(title, avatarDescriptor, uids).start(new CommandCallback<Integer>() {
                    @Override
                    public void onResult(Integer res) {
                        Log.d(TAG, "createGroup:result");
                        resolve(JsPeer.create(Peer.group(res)));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "createGroup:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise inviteMember(final int gid, final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.inviteMember(gid, uid).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "inviteMember:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "inviteMember:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise kickMember(final int gid, final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.kickMember(gid, uid).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "kickMember:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "kickMember:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise leaveGroup(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.leaveGroup(gid).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "leaveGroup:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "leaveGroup:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise getIntegrationToken(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.requestIntegrationToken(gid).start(new CommandCallback<String>() {
                    @Override
                    public void onResult(String res) {
                        Log.d(TAG, "getIntegrationToken:result");
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "getIntegrationToken:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise revokeIntegrationToken(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.revokeIntegrationToken(gid).start(new CommandCallback<String>() {
                    @Override
                    public void onResult(String res) {
                        Log.d(TAG, "revokeIntegrationToken:result");
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "revokeIntegrationToken:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise getInviteLink(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.requestInviteLink(gid).start(new CommandCallback<String>() {
                    @Override
                    public void onResult(String res) {
                        Log.d(TAG, "getInviteLink:result");
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "getInviteLink:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise revokeInviteLink(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.revokeInviteLink(gid).start(new CommandCallback<String>() {
                    @Override
                    public void onResult(String res) {
                        Log.d(TAG, "revokeInviteLink:result");
                        resolve(res);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "revokeInviteLink:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise addContact(final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.addContact(uid).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "addContact:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "addContact:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise addLike(final JsPeer peer, final String rid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.addReaction(peer.convert(), Long.parseLong(rid), "\u2764")
                        .start(new CommandCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean res) {
                                resolve();
                            }

                            @Override
                            public void onError(Exception e) {
                                reject(e.getMessage());
                            }
                        });

            }
        });
    }

    public JsPromise removeLike(final JsPeer peer, final String rid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.removeReaction(peer.convert(), Long.parseLong(rid), "\u2764")
                        .start(new CommandCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean res) {
                                resolve();
                            }

                            @Override
                            public void onError(Exception e) {
                                reject(e.getMessage());
                            }
                        });

            }
        });
    }

    public JsPromise findUsers(final String query) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.findUsers(query).start(new CommandCallback<UserVM[]>() {
                    @Override
                    public void onResult(UserVM[] users) {
                        Log.d(TAG, "findUsers:result");
                        JsArray<JsUser> jsUsers = JsArray.createArray().cast();

                        for (UserVM user : users) {
                            jsUsers.push(messenger.getJsUser(user.getId()).get());
                        }

                        resolve(jsUsers);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "findUsers:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    public JsPromise removeContact(final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.removeContact(uid).start(new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res) {
                        Log.d(TAG, "removeContact:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "removeContact:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    // Settings

    public void changeNotificationsEnabled(JsPeer peer, boolean isEnabled) {
        messenger.changeNotificationsEnabled(peer.convert(), isEnabled);
    }

    public boolean isNotificationsEnabled(JsPeer peer) {
        return messenger.isNotificationsEnabled(peer.convert());
    }

    public boolean isSendByEnterEnabled() {
        return messenger.isSendByEnterEnabled();
    }

    public void changeSendByEnter(boolean sendByEnter) {
        messenger.changeSendByEnter(sendByEnter);
    }

    public boolean isGroupsNotificationsEnabled() {
        return messenger.isGroupNotificationsEnabled();
    }

    public void changeGroupNotificationsEnabled(boolean enabled) {
        messenger.changeGroupNotificationsEnabled(enabled);
    }

    public boolean isOnlyMentionNotifications() {
        return messenger.isGroupNotificationsOnlyMentionsEnabled();
    }

    public void changeIsOnlyMentionNotifications(boolean enabled) {
        messenger.changeGroupNotificationsOnlyMentionsEnabled(enabled);
    }

    public boolean isSoundEffectsEnabled() {
        return messenger.isConversationTonesEnabled();
    }

    public boolean isShowNotificationsTextEnabled() {
        return messenger.isShowNotificationsText();
    }

    public void changeIsShowNotificationTextEnabled(boolean value) {
        messenger.changeShowNotificationTextEnabled(value);
    }

    public void changeSoundEffectsEnabled(boolean enabled) {
        messenger.changeConversationTonesEnabled(enabled);
    }

    public String renderMarkdown(final String markdownText) {
        try {
            return HtmlMarkdownUtils.processText(markdownText, MarkdownParser.MODE_FULL);
        } catch (Exception e) {
            Log.e("Markdown", e);
            return "[Error while processing text]";
        }
    }

    public void handleLinkClick(Event event) {
        Element target = Element.as(event.getEventTarget());
        String href = target.getAttribute("href");
        if (href.startsWith("send:")) {
            String msg = href.substring("send:".length());
            msg = URL.decode(msg);
            if (lastVisiblePeer != null) {
                messenger.sendMessage(lastVisiblePeer, msg);
                event.preventDefault();
            }
        } else {
            if (JsElectronApp.isElectron()) {
                JsElectronApp.openUrlExternal(href);
                event.preventDefault();
            }
        }
    }
}
