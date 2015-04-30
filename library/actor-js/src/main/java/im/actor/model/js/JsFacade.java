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
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsPeer;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.utils.IdentityUtils;
import im.actor.model.log.Log;
import im.actor.model.mvvm.MVVMEngine;

/**
 * Created by ex3ndr on 21.02.15.
 */

@ExportPackage("actor")
@Export("ActorApp")
public class JsFacade implements Exportable {

    private static final String TAG = "JsMessenger";

    private static final String APP_NAME = "Actor Web App";
    private static final int APP_ID = 1;
    private static final String APP_KEY = "??";

    private JsMessenger messenger;

    @Export
    public JsFacade() {
        String clientName = IdentityUtils.getClientName();
        String uniqueId = IdentityUtils.getUniqueId();

        JsConfigurationBuilder configuration = new JsConfigurationBuilder();
        configuration.setApiConfiguration(new ApiConfiguration(APP_NAME, APP_ID, APP_KEY, clientName, uniqueId));

        configuration.addEndpoint("wss://mtproto-api.actor.im:10443/");

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
                    error.onError("INTERNAL_ERROR", "Internal error", false,
                            getAuthState());
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
                    error.onError("INTERNAL_ERROR", "Internal error", false,
                            getAuthState());
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

    // Models

    // Dialogs

    public void bindDialogs(AngularListCallback<JsDialog> callback) {
        messenger.getDialogsList().subscribe(callback);
    }

    public void unbindDialogs(AngularListCallback<JsDialog> callback) {
        messenger.getDialogsList().unsubscribe(callback);
    }

    public void bindChat(JsPeer peer, AngularListCallback<JsMessage> callback) {
        Log.d("JsFacade", "Bind chat: " + peer.getPeerType() + " : " + peer.getPeerId());
        messenger.getConversationList(peer.convert()).subscribe(callback);
    }

    public void unbindChat(JsPeer peer, AngularListCallback<JsMessage> callback) {
        Log.d("JsFacade", "UnBind chat: " + peer.getPeerType() + " : " + peer.getPeerId());
        messenger.getConversationList(peer.convert()).subscribe(callback);
    }

    // Users

    public JsUser getUser(int uid) {
        return messenger.getUser(uid).get();
    }

    public void bindUser(int uid, AngularValueCallback
            callback) {
        messenger.getUser(uid).subscribe(callback);
    }

    public void unbindUser(int uid, AngularValueCallback callback) {
        messenger.getUser(uid).unsubscribe(callback);
    }

    // Actions

    public void sendMessage(JsPeer peer, String text) {
        messenger.sendMessage(peer.convert(), text);
    }

    // Helpers

    public void saveDraft(JsPeer peer, String text) {
        messenger.saveDraft(peer.convert(), text);
    }

    public String loadDraft(JsPeer peer) {
        return messenger.loadDraft(peer.convert());
    }

    // Events

    public void onAppVisible() {
        messenger.onAppVisible();
    }

    public void onAppHidden() {
        messenger.onAppHidden();
    }

    public void onConversationOpen(JsPeer peer) {
        Log.d("JsFacade", "On chat open: " + peer.getPeerType() + " : " + peer.getPeerId());
        messenger.onConversationOpen(peer.convert());
    }

    public void onConversationClosed(JsPeer peer) {
        Log.d("JsFacade", "On chat closed: " + peer.getPeerType() + " : " + peer.getPeerId());
        messenger.onConversationClosed(peer.convert());
    }

    public void onTyping(JsPeer peer) {
        messenger.onTyping(peer.convert());
    }

    public void onProfileOpen(int uid) {
        messenger.onProfileOpen(uid);
    }

    public void onProfileClosed(int uid) {
        messenger.onProfileClosed(uid);
    }
}