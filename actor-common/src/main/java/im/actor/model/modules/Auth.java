package im.actor.model.modules;

import im.actor.model.AuthState;
import im.actor.model.MainThread;
import im.actor.model.api.rpc.RequestSendAuthCode;
import im.actor.model.api.rpc.RequestSignIn;
import im.actor.model.api.rpc.RequestSignUp;
import im.actor.model.api.rpc.ResponseAuth;
import im.actor.model.api.rpc.ResponseSendAuthCode;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.storage.PreferencesStorage;
import im.actor.model.util.RandomUtils;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Auth extends BaseModule {

    private static final int APP_ID = 1;
    private static final String APP_KEY = "??";

    private static final String KEY_DEVICE_HASH = "device_hash";
    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";

    private AuthState state;
    private MainThread mainThread;
    private byte[] deviceHash;
    private int myUid;

    public Auth(Modules modules) {
        super(modules);

        this.mainThread = modules.getConfiguration().getMainThread();

        this.myUid = preferences().getInt(KEY_AUTH_UID, 0);

        deviceHash = preferences().getBytes(KEY_DEVICE_HASH);
        if (deviceHash == null) {
            deviceHash = RandomUtils.seed(32);
            preferences().putBytes(KEY_DEVICE_HASH, deviceHash);
        }

        if (preferences().getBool(KEY_AUTH, false)) {
            state = AuthState.LOGGED_IN;
            modules.onLoggedIn();
        } else {
            state = AuthState.AUTH_START;
        }
    }

    public int myUid() {
        return myUid;
    }

    public AuthState getAuthState() {
        return state;
    }

    public Command<AuthState> requestSms(final long phone) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSendAuthCode(phone, APP_ID, APP_KEY),
                        new RpcCallback<ResponseSendAuthCode>() {
                            @Override
                            public void onResult(final ResponseSendAuthCode response) {
                                preferences().putLong(KEY_PHONE, phone);
                                preferences().putString(KEY_SMS_HASH, response.getSmsHash());
                                state = AuthState.CODE_VALIDATION;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(state);
                                    }
                                });
                            }

                            @Override
                            public void onError(final RpcException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError(e);
                                    }
                                });
                            }
                        });
            }
        };
    }

    public Command<AuthState> sendCode(final int code) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(
                        new RequestSignIn(
                                preferences().getLong(KEY_PHONE, 0),
                                preferences().getString(KEY_SMS_HASH),
                                code + "",
                                RandomUtils.seed(1024),
                                deviceHash,
                                "ActorLib",
                                APP_ID, APP_KEY),
                        new RpcCallback<ResponseAuth>() {

                            @Override
                            public void onResult(ResponseAuth response) {
                                preferences().putBool(KEY_AUTH, true);
                                state = AuthState.LOGGED_IN;
                                myUid = response.getUser().getId();
                                preferences().putInt(KEY_AUTH_UID, myUid);
                                modules().onLoggedIn();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        state = AuthState.LOGGED_IN;
                                        callback.onResult(state);
                                    }
                                });
                            }

                            @Override
                            public void onError(final RpcException e) {
                                if ("PHONE_CODE_EXPIRED".equals(e.getTag())) {
                                    resetAuth();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onError(e);
                                    }
                                });
                            }
                        });
            }
        };
    }

    public Command<AuthState> signUp(final String firstName, String avatarPath, final boolean isSilent) {
        // TODO: Perform avatar upload
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUp(preferences().getLong(KEY_PHONE, 0),
                        preferences().getString(KEY_SMS_HASH),
                        preferences().getInt(KEY_SMS_CODE, 0) + "",
                        firstName,
                        RandomUtils.seed(1024),
                        deviceHash,
                        "ActorLib",
                        APP_ID, APP_KEY,
                        isSilent), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        preferences().putBool(KEY_AUTH, true);
                        state = AuthState.LOGGED_IN;
                        myUid = response.getUser().getId();
                        preferences().putInt(KEY_AUTH_UID, myUid);
                        modules().onLoggedIn();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                state = AuthState.LOGGED_IN;
                                callback.onResult(state);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        if ("PHONE_CODE_EXPIRED".equals(e.getTag())) {
                            resetAuth();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public void resetAuth() {
        state = AuthState.AUTH_START;
    }

    public long getPhone() {
        return preferences().getLong(KEY_PHONE, 0);
    }
}