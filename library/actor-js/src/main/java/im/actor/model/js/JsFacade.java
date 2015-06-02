/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import im.actor.model.ApiConfiguration;
import im.actor.model.AuthState;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.js.angular.AngularListCallback;
import im.actor.model.js.angular.AngularValueCallback;
import im.actor.model.js.entity.Enums;
import im.actor.model.js.entity.JsAuthErrorClosure;
import im.actor.model.js.entity.JsAuthSuccessClosure;
import im.actor.model.js.entity.JsClosure;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsGroup;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsPeer;
import im.actor.model.js.entity.JsTyping;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.providers.JsFileSystemProvider;
import im.actor.model.js.providers.fs.JsBlob;
import im.actor.model.js.providers.fs.JsFile;
import im.actor.model.js.utils.IdentityUtils;
import im.actor.model.log.Log;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.network.RpcException;

@ExportPackage("actor")
@Export("ActorApp")
public class JsFacade implements Exportable {

    private static final String TAG = "JsMessenger";

    private static final String APP_NAME = "Actor Web App";
    private static final int APP_ID = 3;
    private static final String APP_KEY = "278f13e07eee8398b189bced0db2cf66703d1746e2b541d85f5b42b1641aae0e";

    private JsMessenger messenger;
    private JsFileSystemProvider provider;

    @Export
    public JsFacade() {
        String clientName = IdentityUtils.getClientName();
        String uniqueId = IdentityUtils.getUniqueId();
        provider = new JsFileSystemProvider();

        JsConfigurationBuilder configuration = new JsConfigurationBuilder();
        configuration.setApiConfiguration(new ApiConfiguration(APP_NAME, APP_ID, APP_KEY, clientName, uniqueId));
        configuration.setFileSystemProvider(provider);

        configuration.addEndpoint("wss://front1-mtproto-api-rev2.actor.im:8443/");
        configuration.addEndpoint("wss://front2-mtproto-api-rev2.actor.im:8443/");

        messenger = new JsMessenger(configuration.build());

        Log.d(TAG, "JsMessenger created");
    }

    public boolean isLoggedIn() {
        return messenger.isLoggedIn();
    }

    public int getUid() {
        return messenger.myUid();
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
            messenger.requestSms(res).start(new CommandCallback<AuthState>() {
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
            MVVMEngine.runOnUiThread(new Runnable() {
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
            int res = Integer.parseInt(code);
            messenger.sendCode(res).start(new CommandCallback<AuthState>() {
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
            MVVMEngine.runOnUiThread(new Runnable() {
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
        messenger.signUp(name, null, false).start(new CommandCallback<AuthState>() {
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

    // Dialogs

    public void bindDialogs(AngularListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getDialogsList().subscribe(callback);
    }

    public void unbindDialogs(AngularListCallback<JsDialog> callback) {
        if (callback == null) {
            return;
        }
        messenger.getDialogsList().unsubscribe(callback);
    }

    // Chats

    public void bindChat(JsPeer peer, AngularListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getConversationList(peer.convert()).subscribe(callback);
    }

    public void unbindChat(JsPeer peer, AngularListCallback<JsMessage> callback) {
        if (callback == null) {
            return;
        }
        messenger.getConversationList(peer.convert()).unsubscribe(callback);
    }

    public void onMessageShown(JsPeer peer, String sortKey, boolean isOut) {
        if (!isOut) {
            messenger.onMessageShown(peer.convert(), Long.parseLong(sortKey));
        }
    }

    public void deleteChat(JsPeer peer, final JsClosure success, final JsClosure error) {
        messenger.deleteChat(peer.convert()).start(new CommandCallback<Boolean>() {
            @Override
            public void onResult(Boolean res) {
                success.callback();
            }

            @Override
            public void onError(Exception e) {
                error.callback();
            }
        });
    }

    public void clearChat(JsPeer peer, final JsClosure success, final JsClosure error) {
        messenger.clearChat(peer.convert()).start(new CommandCallback<Boolean>() {
            @Override
            public void onResult(Boolean res) {
                success.callback();
            }

            @Override
            public void onError(Exception e) {
                error.callback();
            }
        });
    }

    // Users

    public JsUser getUser(int uid) {
        return messenger.getJsUser(uid).get();
    }

    public void bindUser(int uid, AngularValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).subscribe(callback);
    }

    public void unbindUser(int uid, AngularValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsUser(uid).unsubscribe(callback);
    }

    // Groups

    public JsGroup getGroup(int gid) {
        return messenger.getJsGroup(gid).get();
    }

    public void bindGroup(int gid, AngularValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).subscribe(callback);
    }

    public void unbindGroup(int gid, AngularValueCallback callback) {
        if (callback == null) {
            return;
        }
        messenger.getJsGroup(gid).unsubscribe(callback);
    }

    // Actions

    public void sendMessage(JsPeer peer, String text) {
        messenger.sendMessage(peer.convert(), text);
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

    // Typing

    public void onTyping(JsPeer peer) {
        messenger.onTyping(peer.convert());
    }

    public JsTyping getTyping(JsPeer peer) {
        return messenger.getTyping(peer.convert()).get();
    }

    public void bindTyping(JsPeer peer, AngularValueCallback callback) {
        messenger.getTyping(peer.convert()).subscribe(callback);
    }

    public void unbindTyping(JsPeer peer, AngularValueCallback callback) {
        messenger.getTyping(peer.convert()).unsubscribe(callback);
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
}