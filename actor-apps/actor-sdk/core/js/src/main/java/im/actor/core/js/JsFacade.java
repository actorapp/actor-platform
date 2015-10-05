/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Event;
import im.actor.core.*;
import im.actor.core.api.ApiAuthSession;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.js.entity.*;
import im.actor.core.js.modules.JsBindedValueCallback;
import im.actor.core.js.providers.JsNotificationsProvider;
import im.actor.core.js.providers.JsPhoneBookProvider;
import im.actor.core.js.utils.HtmlMarkdownUtils;
import im.actor.core.js.utils.IdentityUtils;
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
            e.printStackTrace();
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
        messenger.getSharedDialogList().subscribe(callback);
    }

    public void unbindDialogs(JsDisplayListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedDialogList().unsubscribe(callback);
    }

    // Contacts

    public void bindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().subscribe(callback);
    }

    public void unbindContacts(JsDisplayListCallback<JsContact> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedContactList().unsubscribe(callback);
    }

    // Chats

    public void bindChat(JsPeer peer, JsDisplayListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedChatList(peer.convert()).subscribeInverted(callback);
    }

    public void unbindChat(JsPeer peer, JsDisplayListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getSharedChatList(peer.convert()).unsubscribeInverted(callback);
    }

    public void onMessageShown(JsPeer peer, JsMessage message) {
        if (message.isOnServer()) {
            messenger.onMessageShown(peer.convert(), Long.parseLong(message.getSortKey()));
        }
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
        messenger.onAppVisible();
    }

    public void onAppHidden() {
        messenger.onAppHidden();
    }

    public void onConversationOpen(JsPeer peer) {
        messenger.onConversationOpen(peer.convert());
    }

    public void onConversationClosed(JsPeer peer) {
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
                String avatarDescriptor = provider.registerUploadFile(file);
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

    public native void handleLinkClick(Event event)/*-{
        console.warn('event type is', event.type);
        if (event.type == 'click') {
            if (window.$wnd.messenger.isElectron()) {
                console.warn('opening external');
                var url = event.target.getAttribute('href');
                window.$wnd.require('shell').openExternal(url);
                event.preventDefault()
            } else {
                console.warn('type of window.require is', typeof window.$wnd.require);
            }
        } else {
            throw new Error("Event has type " + event.type + ", must to be click");
        }
    }-*/;
}
