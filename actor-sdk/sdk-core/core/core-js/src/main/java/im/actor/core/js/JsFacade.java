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
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.entity.BotCommand;
import im.actor.core.entity.EntityConverter;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.MessageSearchEntity;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerSearchType;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.js.annotations.UsedByApp;
import im.actor.core.js.entity.*;
import im.actor.core.js.modules.JsBindedValueCallback;
import im.actor.core.js.providers.JsNotificationsProvider;
import im.actor.core.js.providers.JsPhoneBookProvider;
import im.actor.core.js.providers.JsCallsProvider;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.js.utils.HtmlMarkdownUtils;
import im.actor.core.js.utils.IdentityUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.messages.*;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.js.JsFileSystemProvider;
import im.actor.runtime.js.JsLogProvider;
import im.actor.runtime.js.fs.JsBlob;
import im.actor.runtime.js.fs.JsFile;
import im.actor.runtime.js.mvvm.JsDisplayListCallback;
import im.actor.runtime.js.utils.JsPromise;
import im.actor.runtime.js.utils.JsPromiseDispatcher;
import im.actor.runtime.js.utils.JsPromiseExecutor;
import im.actor.runtime.markdown.MarkdownParser;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ExportPackage("actor")
@Export("ActorApp")
@UsedByApp
public class JsFacade implements Exportable {

    private static final String TAG = "JsMessenger";

    private static final String APP_NAME = "Actor Web App";
    private static final int APP_ID = 3;
    private static final String APP_KEY = "278f13e07eee8398b189bced0db2cf66703d1746e2b541d85f5b42b1641aae0e";

    private JsMessenger messenger;
    private JsFileSystemProvider provider;
    private Peer lastVisiblePeer;

    @Export
    @UsedByApp
    public JsFacade() {

    }

    @UsedByApp
    public void init(JsConfig config) {

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
        for (String endpoint : config.getEndpoints()) {
            configuration.addEndpoint(endpoint);
        }

        if (config.getLogHandler() != null) {
            final JsLogCallback callback = config.getLogHandler();
            JsLogProvider.setLogCallback(new JsLogProvider.LogCallback() {
                @Override
                public void log(String tag, String level, String message) {
                    callback.log(tag, level, message);
                }
            });
        }

        messenger = new JsMessenger(configuration.build());

        Log.d(TAG, "JsMessenger created");
    }

    @UsedByApp
    public boolean isLoggedIn() {
        return messenger.isLoggedIn();
    }

    @UsedByApp
    public int getUid() {
        return messenger.myUid();
    }

    @UsedByApp
    public boolean isElectron() {
        return messenger.isElectron();
    }

    // Auth

    @UsedByApp
    public String getAuthState() {
        return Enums.convert(messenger.getAuthState());
    }

    @UsedByApp
    public String getAuthPhone() {
        return "" + messenger.getAuthPhone();
    }

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
    public JsPromise terminateSession(final int id) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.terminateSession(id).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise terminateAllSessions() {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.terminateAllSessions().start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    // Dialogs

    @UsedByApp
    public void bindDialogs(JsDisplayListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedDialogList().subscribe(callback, false);
    }

    @UsedByApp
    public void unbindDialogs(JsDisplayListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedDialogList().unsubscribe(callback);
    }

    @UsedByApp
    public void bindGroupDialogs(JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.getDialogsGroupedList().subscribe(callback);
    }

    @UsedByApp
    public void unbindGroupDialogs(JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.getDialogsGroupedList().unsubscribe(callback);
    }

    // Contacts

    @UsedByApp
    public void bindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().subscribe(callback, true);
    }

    @UsedByApp
    public void unbindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().unsubscribe(callback);
    }

    // Search

    @UsedByApp
    public void bindSearch(JsDisplayListCallback<JsSearchEntity> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedSearchList().subscribe(callback, false);
    }

    @UsedByApp
    public void unbindSearch(JsDisplayListCallback<JsSearchEntity> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedSearchList().unsubscribe(callback);
    }

    // Chats

    @UsedByApp
    public void preInitChat(JsPeer peer) {
        messenger.onConversationPreLoad(peer.convert());
    }

    @UsedByApp
    public JsMessagesBind bindMessages(JsPeer peer, JsMessagesBindClosure callback) {
        if (callback == null) {
            return null;
        }
        Peer peerC = peer.convert();

        return new JsMessagesBind(callback, messenger.getSharedChatList(peerC), messenger.getConversationVM(peerC));
    }

    public JsPromise editMessage(JsPeer peer, String id, String newText) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.updateMessage(peer.convert(), newText, Long.parseLong(id)).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public void deleteMessage(JsPeer peer, String id) {
        messenger.deleteMessages(peer.convert(), new long[]{Long.parseLong(id)});
    }

    @UsedByApp
    public JsPromise deleteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.deleteChat(peer.convert()).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise clearChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.clearChat(peer.convert()).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise archiveChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.archiveChat(peer.convert()).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise favoriteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.favouriteChat(peer.convert()).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise unfavoriteChat(final JsPeer peer) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.unfavoriteChat(peer.convert()).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPeer getUserPeer(int uid) {
        return JsPeer.create(Peer.user(uid));
    }

    @UsedByApp
    public JsPeer getGroupPeer(int gid) {
        return JsPeer.create(Peer.group(gid));
    }

    // Stickers

    @UsedByApp
    public JsArray<JsSticker> getStickers() {
        return messenger.getStickers().get();
    }

    @UsedByApp
    public void bindStickers(JsBindedValueCallback callback) {
        messenger.getStickers().subscribe(callback);
    }

    @UsedByApp
    public void unbindStickers(JsBindedValueCallback callback) {
        messenger.getStickers().unsubscribe(callback);
    }

    // Users

    @UsedByApp
    public JsUser getUser(int uid) {
        return messenger.getJsUser(uid).get();
    }

    @UsedByApp
    public void bindUser(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).subscribe(callback);
    }

    @UsedByApp
    public void unbindUser(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).unsubscribe(callback);
    }

    @UsedByApp
    public void bindUserOnline(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserOnline(uid).subscribe(callback);
    }

    @UsedByApp
    public void unbindUserOnline(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserOnline(uid).unsubscribe(callback);
    }

    @UsedByApp
    public void bindUserBlocked(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserBlocked(uid).subscribe(callback);
    }

    @UsedByApp
    public void unbindUserBlocked(int uid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUserBlocked(uid).unsubscribe(callback);
    }

    @UsedByApp
    public JsPromise blockUser(final int uid) {
        return JsPromise.from(messenger.blockUser(uid));
    }

    @UsedByApp
    public JsPromise unblockUser(final int uid) {
        return JsPromise.from(messenger.unblockUser(uid));
    }

    @UsedByApp
    public JsPromise isStarted(final int uid) {
        return JsPromise.from(messenger.isStarted(uid));
    }

    // Groups

    @UsedByApp
    public JsGroup getGroup(int gid) {
        return messenger.getJsGroup(gid).get();
    }

    @UsedByApp
    public void bindGroup(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).subscribe(callback);
    }

    @UsedByApp
    public void unbindGroup(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).unsubscribe(callback);
    }

    @UsedByApp
    public void bindGroupOnline(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroupOnline(gid).subscribe(callback);
    }

    @UsedByApp
    public void unbindGroupOnline(int gid, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroupOnline(gid).unsubscribe(callback);
    }

    // Calls

    @UsedByApp
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

    @UsedByApp
    public JsPromise doGroupCall(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.doGroupCall(gid).start(new CommandCallback<Long>() {
                    @Override
                    public void onResult(Long res) {
                        Log.d(TAG, "doGroupCall:result");
                        resolve();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "doGroupCall:error");
                        reject(e.getMessage());
                    }
                });
            }
        });
    }

    @UsedByApp
    public void answerCall(String callId) {
        messenger.answerCall(Long.parseLong(callId));
    }

    @UsedByApp
    public void endCall(String callId) {
        messenger.endCall(Long.parseLong(callId));
    }

    @UsedByApp
    public void toggleCallMute(String callId) {
        messenger.toggleCallMute(Long.parseLong(callId));
    }

    @UsedByApp
    public void bindCall(String id, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsCall(id).subscribe(callback);
    }

    @UsedByApp
    public void unbindCall(String id, JsBindedValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsCall(id).unsubscribe(callback);
    }

    // Event Bus

    @UsedByApp
    public void bindEventBus(JsEventBusCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.subscribeEventBus(callback);
    }

    @UsedByApp
    public void unbindEventBus(JsEventBusCallback callback) {
        if (callback == null) {
            return;
        }

        messenger.unsubscribeEventBus(callback);
    }

    // Actions

    @UsedByApp
    public void sendMessage(JsPeer peer, String text) {
        messenger.sendMessageWithMentionsDetect(peer.convert(), text);
    }

    @UsedByApp
    public void sendFile(JsPeer peer, JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.sendDocument(peer.convert(),
                file.getName(), file.getMimeType(), descriptor);
    }

    @UsedByApp
    public void sendPhoto(final JsPeer peer, final JsFile file) {
        messenger.sendPhoto(peer.convert(), file);
    }

    @UsedByApp
    public void sendAnimation(final JsPeer peer, final JsFile file) {
        messenger.sendAnimation(peer.convert(), file);
    }

    @UsedByApp
    public void sendClipboardPhoto(final JsPeer peer, final JsBlob blob) {
        messenger.sendClipboardPhoto(peer.convert(), blob);
    }

    @UsedByApp
    public void sendVoiceMessage(final JsPeer peer, int duration, final JsBlob blob) {
        String descriptor = provider.registerUploadFile(blob);
        messenger.sendAudio(peer.convert(), "voice.opus", duration, descriptor);
    }

    @UsedByApp
    public void sendSticker(JsPeer peer, JsSticker sticker) {
        messenger.sendSticker(peer.convert(), sticker.getSticker());
    }

    // Drafts

    @UsedByApp
    public void saveDraft(JsPeer peer, String text) {
        messenger.saveDraft(peer.convert(), text);
    }

    @UsedByApp
    public String loadDraft(JsPeer peer) {
        return messenger.loadDraft(peer.convert());
    }

    @UsedByApp
    public JsArray<JsMentionFilterResult> findMentions(int gid, String query) {
        List<MentionFilterResult> res = messenger.findMentions(gid, query);
        JsArray<JsMentionFilterResult> mentions = JsArray.createArray().cast();
        for (MentionFilterResult m : res) {
            mentions.push(JsMentionFilterResult.create(m));
        }
        return mentions;
    }

    @UsedByApp
    public JsArray<JsBotCommand> findBotCommands(int uid, String query) {
        JsArray<JsBotCommand> commands = JsArray.createArray().cast();
        for (BotCommand c : messenger.getUser(uid).getBotCommands().get()) {
            if (c.getSlashCommand().startsWith(query)) {
                commands.push(JsBotCommand.create(c.getSlashCommand(), c.getDescription()));
            }
        }
        return commands;
    }

    // Typing

    @UsedByApp
    public void onTyping(JsPeer peer) {
        messenger.onTyping(peer.convert());
    }

    @UsedByApp
    public JsTyping getTyping(JsPeer peer) {
        return messenger.getTyping(peer.convert()).get();
    }

    @UsedByApp
    public void bindTyping(JsPeer peer, JsBindedValueCallback callback) {
        messenger.getTyping(peer.convert()).subscribe(callback);
    }

    @UsedByApp
    public void unbindTyping(JsPeer peer, JsBindedValueCallback callback) {
        messenger.getTyping(peer.convert()).unsubscribe(callback);
    }

    // Updating state

    @UsedByApp
    public void bindConnectState(JsBindedValueCallback callback) {
        messenger.getOnlineStatus().subscribe(callback);
    }

    @UsedByApp
    public void unbindConnectState(JsBindedValueCallback callback) {
        messenger.getOnlineStatus().unsubscribe(callback);
    }

    @UsedByApp
    public void bindGlobalCounter(JsBindedValueCallback callback) {
        messenger.getGlobalCounter().subscribe(callback);
    }

    @UsedByApp
    public void unbindGlobalCounter(JsBindedValueCallback callback) {
        messenger.getGlobalCounter().unsubscribe(callback);
    }

    @UsedByApp
    public void bindTempGlobalCounter(JsBindedValueCallback callback) {
        messenger.getTempGlobalCounter().subscribe(callback);
    }

    @UsedByApp
    public void unbindTempGlobalCounter(JsBindedValueCallback callback) {
        messenger.getTempGlobalCounter().unsubscribe(callback);
    }

    // Events

    @UsedByApp
    public void onAppVisible() {
        // Ignore for electron runtime
        if (isElectron()) {
            return;
        }

        messenger.getJsIdleModule().onVisible();
    }

    @UsedByApp
    public void onAppHidden() {
        // Ignore for electron runtime
        if (isElectron()) {
            return;
        }

        messenger.getJsIdleModule().onHidden();
    }

    @UsedByApp
    public void onConversationOpen(JsPeer peer) {
        Log.d(TAG, "onConversationOpen | " + peer);
        lastVisiblePeer = peer.convert();
        messenger.onConversationOpen(lastVisiblePeer);
    }

    @UsedByApp
    public void onConversationClosed(JsPeer peer) {
        Log.d(TAG, "onConversationClosed | " + peer);
        if (lastVisiblePeer != null && lastVisiblePeer.equals(peer.convert())) {
            lastVisiblePeer = null;
            Log.d(TAG, "onConversationClosed | Closing");
        }
        messenger.onConversationClosed(peer.convert());
    }

    @UsedByApp
    public void onDialogsOpen() {
        messenger.onDialogsOpen();
    }

    @UsedByApp
    public void onDialogsClosed() {
        messenger.onDialogsClosed();
    }

    @UsedByApp
    public void onProfileOpen(int uid) {
        messenger.onProfileOpen(uid);
    }

    @UsedByApp
    public void onProfileClosed(int uid) {
        messenger.onProfileClosed(uid);
    }

    @UsedByApp
    public void onDialogsEnd() {
        messenger.loadMoreDialogs();
    }

    @UsedByApp
    public JsPromise loadArchivedDialogs() {
        return loadArchivedDialogs(true);
    }

    @UsedByApp
    public JsPromise loadMoreArchivedDialogs() {
        return loadArchivedDialogs(false);
    }

    @UsedByApp
    private JsPromise loadArchivedDialogs(final boolean init) {
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

    @UsedByApp
    public JsPromise loadBlockedUsers() {
        return JsPromise.from(messenger.loadBlockedUsers()
                .map(users -> {
                    JsArray<JsUser> res = JsArray.createArray().cast();
                    for (User u : users) {
                        res.push(getUser(u.getUid()));
                    }
                    return res;
                })
        );
    }

    @UsedByApp
    public void onChatEnd(JsPeer peer) {
        messenger.loadMoreHistory(peer.convert());
    }

    // Profile

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
    public void changeMyAvatar(final JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.changeMyAvatar(descriptor);
    }

    @UsedByApp
    public void removeMyAvatar() {
        messenger.removeMyAvatar();
    }

    @UsedByApp
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

    @UsedByApp
    public JsPromise joinGroupViaLink(final String url) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.joinGroupViaToken(url).start(new CommandCallback<Integer>() {
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

    @UsedByApp
    public JsPromise editGroupTitle(final int gid, final String newTitle) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.editGroupTitle(gid, newTitle).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise editGroupAbout(final int gid, final String newAbout) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.editGroupAbout(gid, newAbout).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public void changeGroupAvatar(final int gid, final JsFile file) {
        String descriptor = provider.registerUploadFile(file);
        messenger.changeGroupAvatar(gid, descriptor);
    }

    @UsedByApp
    public void removeGroupAvatar(final int gid) {
        messenger.removeGroupAvatar(gid);
    }

    @UsedByApp
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

    @UsedByApp
    public JsPromise inviteMember(final int gid, final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.inviteMember(gid, uid).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise kickMember(final int gid, final int uid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.kickMember(gid, uid).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise leaveGroup(final int gid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                //noinspection ConstantConditions
                messenger.leaveGroup(gid).start(new CommandCallback<Void>() {
                    @Override
                    public void onResult(Void res) {
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
    public JsPromise addLike(final JsPeer peer, final String rid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.addReaction(peer.convert(), Long.parseLong(rid), "\u2764")
                        .start(new CommandCallback<Void>() {
                            @Override
                            public void onResult(Void res) {
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

    @UsedByApp
    public JsPromise removeLike(final JsPeer peer, final String rid) {
        return JsPromise.create(new JsPromiseExecutor() {
            @Override
            public void execute() {
                messenger.removeReaction(peer.convert(), Long.parseLong(rid), "\u2764")
                        .start(new CommandCallback<Void>() {
                            @Override
                            public void onResult(Void res) {
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

    @UsedByApp
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

    @UsedByApp
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

    @UsedByApp
    public void changeNotificationsEnabled(JsPeer peer, boolean isEnabled) {
        messenger.changeNotificationsEnabled(peer.convert(), isEnabled);
    }

    @UsedByApp
    public boolean isNotificationsEnabled(JsPeer peer) {
        return messenger.isNotificationsEnabled(peer.convert());
    }

    @UsedByApp
    public void changeAnimationAutoPlayEnabled(boolean isEnabled) {
        messenger.changeAnimationAutoPlayEnabled(isEnabled);
    }

    @UsedByApp
    public boolean isAnimationAutoPlayEnabled() {
        return messenger.isAnimationAutoPlayEnabled();
    }

    @UsedByApp
    public boolean isSendByEnterEnabled() {
        return messenger.isSendByEnterEnabled();
    }

    @UsedByApp
    public void changeSendByEnter(boolean sendByEnter) {
        messenger.changeSendByEnter(sendByEnter);
    }

    @UsedByApp
    public boolean isGroupsNotificationsEnabled() {
        return messenger.isGroupNotificationsEnabled();
    }

    @UsedByApp
    public void changeGroupNotificationsEnabled(boolean enabled) {
        messenger.changeGroupNotificationsEnabled(enabled);
    }

    @UsedByApp
    public boolean isOnlyMentionNotifications() {
        return messenger.isGroupNotificationsOnlyMentionsEnabled();
    }

    @UsedByApp
    public void changeIsOnlyMentionNotifications(boolean enabled) {
        messenger.changeGroupNotificationsOnlyMentionsEnabled(enabled);
    }

    @UsedByApp
    public boolean isSoundEffectsEnabled() {
        return messenger.isConversationTonesEnabled();
    }

    @UsedByApp
    public boolean isShowNotificationsTextEnabled() {
        return messenger.isShowNotificationsText();
    }

    @UsedByApp
    public void changeIsShowNotificationTextEnabled(boolean value) {
        messenger.changeShowNotificationTextEnabled(value);
    }

    @UsedByApp
    public void changeSoundEffectsEnabled(boolean enabled) {
        messenger.changeConversationTonesEnabled(enabled);
    }

    @UsedByApp
    public String renderMarkdown(final String markdownText) {
        try {
            return HtmlMarkdownUtils.processText(markdownText, MarkdownParser.MODE_FULL);
        } catch (Exception e) {
            Log.e("Markdown", e);
            return "[Error while processing text]";
        }
    }

    @UsedByApp
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
