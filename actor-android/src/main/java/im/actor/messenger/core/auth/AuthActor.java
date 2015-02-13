package im.actor.messenger.core.auth;

import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;

import im.actor.api.ApiRequestException;
import im.actor.api.crypto.KeyTools;
import im.actor.api.scheme.User;
import im.actor.api.scheme.rpc.ResponseAuth;
import im.actor.api.scheme.rpc.ResponseSendAuthCode;
import im.actor.crypto.Crypto;
import im.actor.messenger.api.ApiConversion;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.Core;
import im.actor.messenger.core.actors.profile.AvatarChangeActor;
import im.actor.messenger.storage.AuthStorage;

import java.security.KeyPair;

import static im.actor.messenger.core.Core.keyStorage;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.core.auth.AuthModel.AuthProcessState.*;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 30.08.14.
 */
public class AuthActor extends Actor {

    public static ActorSelection auth(final AuthModel model, final AuthStorage profileStorage) {
        return new ActorSelection(Props.create(AuthActor.class, new ActorCreator<AuthActor>() {
            @Override
            public AuthActor create() {
                return new AuthActor(model, profileStorage);
            }
        }), "auth");
    }

    private static final int APP_ID = 1;
    private static final String APP_KEY = Crypto.hex(Crypto.SHA256("ex3ndr is the best".getBytes()));

    private AuthModel.AuthProcessState processState;
    private AuthStorage authStorage;
    private String deviceId;

    private byte[] deviceIdHash;
    private String deviceName;

    public AuthActor(AuthModel model, AuthStorage profileStorage) {
        this.processState = model.getAuthProcessState();
        this.authStorage = profileStorage;
    }

    @Override
    public void preStart() {
        super.preStart();
        String android_id = Settings.Secure.getString(AppContext.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        deviceId = "android:" + AppContext.getContext().getPackageName() + ":" + android_id;

        Log.d("AuthActor", "DeviceID: " + deviceId);

        deviceIdHash = Crypto.SHA256(deviceId.getBytes());
        deviceName = Build.MANUFACTURER + " " + Build.MODEL;

        try {
            Cursor c = AppContext.getContext().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, new String[]
                    {
                            "display_name"
                    }, null, null, null);
            if (c != null && c.moveToFirst()) {
                deviceName += " (" + c.getString(c.getColumnIndex("display_name")) + ")";
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof AuthReset) {
            processState.changeState(STATE_START);
            authStorage.logOut();
        } else if (message instanceof AuthRequestCode) {
            AuthRequestCode requestCode = (AuthRequestCode) message;
            if (!processState.changeState(STATE_REQUESTING_SMS)) {
                return;
            }

            sendRequestAuthCode(requestCode.getPhone());

        } else if (message instanceof AuthSendCode) {
            final AuthSendCode code = (AuthSendCode) message;

            if (!processState.changeState(STATE_CODE_SENDING)) {
                return;
            }

            KeyPair savedKeyPair = keyStorage().getKeyPair();
            if (savedKeyPair == null) {
                keyStorage().setKey(im.actor.crypto.KeyTools.generateNewRsaKey());
            }

            sendCode(code.getCode());
        } else if (message instanceof ResetCodeSend) {
            processState.changeState(STATE_REQUESTED_SMS);
        } else if (message instanceof TryAgainCodeSend) {
            if (processState.getSmsCode() != 0) {
                if (processState.changeState(STATE_CODE_SENDING)) {
                    sendCode(processState.getSmsCode());
                }
            } else {
                processState.changeState(STATE_START);
            }
        } else if (message instanceof ResetCodeRequest) {
            processState.changeState(STATE_START);
        } else if (message instanceof TryAgainCodeRequest) {
            if (processState.getPhoneNumber() != 0) {
                if (processState.changeState(STATE_REQUESTING_SMS)) {
                    sendRequestAuthCode(processState.getPhoneNumber());
                }
            } else {
                processState.changeState(STATE_START);
            }
        } else if (message instanceof PerformSignup) {
            if (processState.changeState(STATE_SIGNING)) {
                if (processState.getPhoneNumber() != 0 && processState.getSmsCode() != 0 && processState.getSmsHash() != null) {
                    sendSignup(((PerformSignup) message).getName(), ((PerformSignup) message).avatarPath);
                } else {
                    processState.changeState(STATE_START);
                }
            }
        } else if (message instanceof TryAgainSignup) {
            if (processState.getPhoneNumber() != 0 && processState.getSmsCode() != 0 && processState.getSmsHash() != null &&
                    processState.getName() != null) {
                sendSignup(processState.getName(), processState.getAvatarImage());
            } else {
                processState.changeState(STATE_START);
            }
        } else if (message instanceof ResetSignup) {
            processState.changeState(STATE_SIGN_UP);
        } else {
            drop(message);
        }
    }

    private void sendCode(int code) {
        processState.setSmsCode(code);

        KeyPair keyPair = keyStorage().getKeyPair();
        ask(requests().signIn(processState.getPhoneNumber(),
                processState.getSmsHash(), "" + code, KeyTools.encodeRsaPublicKey(keyPair.getPublic()),
                deviceIdHash, deviceName, APP_ID, APP_KEY), new FutureCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth result) {
                completeAuth(result.getUser());
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof ApiRequestException) {
                    ApiRequestException requestException = (ApiRequestException) throwable;
                    if ("PHONE_NUMBER_UNOCCUPIED".equals(requestException.getErrorTag())) {
                        processState.changeState(STATE_SIGN_UP);
                        return;
                    }
                    if ("CODE_EXPIRED".equals(requestException.getErrorTag())) {
                        processState.changeState(STATE_SIGN_UP);
                        return;
                    }
                }

                processState.changeStateError(STATE_CODE_SEND_ERROR, throwable);
            }
        });
    }

    private void sendRequestAuthCode(long phone) {

        processState.setPhoneNumber(phone);

        ask(requests().sendAuthCode(phone, APP_ID, APP_KEY), new FutureCallback<ResponseSendAuthCode>() {
            @Override
            public void onResult(ResponseSendAuthCode result) {
                String smsHash = result.getSmsHash();
                if (processState.changeState(STATE_REQUESTED_SMS)) {
                    processState.setSmsHash(smsHash);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                processState.changeStateError(STATE_REQUESTING_SMS_ERROR,
                        throwable);
            }
        });
    }

    private void sendSignup(String name, final String avatarPath) {
        processState.setName(name);
        processState.setAvatarImage(avatarPath);

        KeyPair savedKeyPair = keyStorage().getKeyPair();

        ask(requests().signUp(processState.getPhoneNumber(), processState.getSmsHash(), processState.getSmsCode() + "",
                name, KeyTools.encodeRsaPublicKey(savedKeyPair.getPublic()), deviceIdHash, deviceName,
                APP_ID, APP_KEY, false), new FutureCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth result) {
                completeAuth(result.getUser());
                if (avatarPath != null) {
                    AvatarChangeActor.avatarSender().changeAvatar(avatarPath);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                processState.changeStateError(STATE_SIGNING_ERROR, throwable);
            }
        });
    }

    private void completeAuth(User u) {
        users().put(u.getId(), ApiConversion.convert(u));
        authStorage.logIn(u.getId());
        processState.changeState(STATE_SIGNED);
        Core.core().afterLogIn();
    }


    public static class AuthRequestCode {
        private final long phone;

        public AuthRequestCode(long phone) {
            this.phone = phone;
        }

        public long getPhone() {
            return phone;
        }
    }

    public static class AuthReset {
    }

    public static class AuthSendCode {
        private int code;

        public AuthSendCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static class ResetCodeRequest {
    }

    public static class TryAgainCodeRequest {
    }

    public static class ResetCodeSend {
    }

    public static class TryAgainCodeSend {
    }

    public static class PerformSignup {
        private String name;
        private String avatarPath;

        public PerformSignup(String name, String avatarPath) {
            this.name = name;
            this.avatarPath = avatarPath;
        }

        public String getAvatarPath() {
            return avatarPath;
        }

        public String getName() {
            return name;
        }
    }

    public static class TryAgainSignup {

    }

    public static class ResetSignup {

    }
}