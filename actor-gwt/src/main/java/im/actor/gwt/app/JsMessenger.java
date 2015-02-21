package im.actor.gwt.app;

import com.google.gwt.storage.client.Storage;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import im.actor.gwt.app.helpers.Enums;
import im.actor.gwt.app.helpers.JsAuthErrorClosure;
import im.actor.gwt.app.helpers.JsAuthSuccessClosure;
import im.actor.gwt.app.storage.JsStorage;
import im.actor.gwt.app.sys.JsLog;
import im.actor.gwt.app.sys.JsMainThread;
import im.actor.gwt.app.threading.JsThreading;
import im.actor.gwt.app.websocket.JsNetworking;
import im.actor.model.AuthState;
import im.actor.model.Configuration;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.Messenger;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.log.Log;

/**
 * Created by ex3ndr on 21.02.15.
 */

@ExportPackage("actor")
@Export("ActorApp")
public class JsMessenger implements Exportable {

    private static final String TAG = "JsMessenger";

    private Messenger messenger;
    private JsStorage jsStorage;
    private JsMainThread mainThread;

    @Export
    public JsMessenger() {
        Storage.getLocalStorageIfSupported().clear();
        jsStorage = new JsStorage();
        mainThread = new JsMainThread();
        Configuration configuration = new ConfigurationBuilder()
                .setNetworking(new JsNetworking())
                .addEndpoint("wss://mtproto-api.actor.im:9082")
                .setStorage(jsStorage)
                .setThreading(new JsThreading())
                .setLog(new JsLog())
                .setMainThread(mainThread)
                .setUploadFilePersist(false).build();
        messenger = new Messenger(configuration);
        Log.d(TAG, "JsMessenger created");
    }

    public boolean isLocalStorageSupported() {
        return jsStorage.isLocalStorageSupported();
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
            mainThread.runOnUiThread(new Runnable() {
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
            mainThread.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    error.onError("PHONE_CODE_INVALID", "Invalid code number", false,
                            getAuthState());
                }
            });
        }
    }
}