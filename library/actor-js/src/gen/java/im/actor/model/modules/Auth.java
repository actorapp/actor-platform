package im.actor.model.modules;

import im.actor.model.ApiConfiguration;
import im.actor.model.AuthState;
import im.actor.model.MainThreadProvider;
import im.actor.model.api.rpc.RequestSendAuthCode;
import im.actor.model.api.rpc.RequestSignIn;
import im.actor.model.api.rpc.RequestSignUp;
import im.actor.model.api.rpc.ResponseAuth;
import im.actor.model.api.rpc.ResponseSendAuthCode;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.crypto.CryptoKeyPair;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.log.Log;
import im.actor.model.modules.updates.internal.LoggedIn;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Auth extends BaseModule {

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";

    private static final String KEY_PUBLIC_KEY = "auth_key_public";
    private static final String KEY_PRIVATE_KEY = "auth_key_private";

    private AuthState state;
    private MainThreadProvider mainThreadProvider;

    private byte[] publicKey;
    private byte[] privateKey;

    private byte[] deviceHash;
    private ApiConfiguration apiConfiguration;

    private int myUid;

    public Auth(Modules modules) {
        super(modules);

        long start = modules.getConfiguration().getThreadingProvider().getActorTime();
        this.mainThreadProvider = modules.getConfiguration().getMainThreadProvider();
        Log.d("CORE_INIT", "Loading stage5.3.1 in " + (modules.getConfiguration().getThreadingProvider().getActorTime() - start) + " ms");
        start = modules.getConfiguration().getThreadingProvider().getActorTime();

        this.myUid = preferences().getInt(KEY_AUTH_UID, 0);
        Log.d("CORE_INIT", "Loading stage5.3.2 in " + (modules.getConfiguration().getThreadingProvider().getActorTime() - start) + " ms");
        start = modules.getConfiguration().getThreadingProvider().getActorTime();

        // Keep device hash always stable across launch
        deviceHash = preferences().getBytes(KEY_DEVICE_HASH);
        if (deviceHash == null) {
            deviceHash = CryptoUtils.SHA256(modules.getConfiguration().getApiConfiguration().getDeviceString().getBytes());
            preferences().putBytes(KEY_DEVICE_HASH, deviceHash);
        }

        // TODO: Make key gen async. Better logic on key lost.
        publicKey = preferences().getBytes(KEY_PUBLIC_KEY);
        privateKey = preferences().getBytes(KEY_PRIVATE_KEY);

        if (publicKey == null || privateKey == null) {
            CryptoKeyPair keyPair = CryptoUtils.generateRSA1024KeyPair();
            publicKey = keyPair.getPublicKey();
            privateKey = keyPair.getPrivateKey();
            preferences().putBytes(KEY_PUBLIC_KEY, publicKey);
            preferences().putBytes(KEY_PRIVATE_KEY, privateKey);
        }

        apiConfiguration = modules.getConfiguration().getApiConfiguration();

        Log.d("CORE_INIT", "Loading stage5.3.3 in " + (modules.getConfiguration().getThreadingProvider().getActorTime() - start) + " ms");
        start = modules.getConfiguration().getThreadingProvider().getActorTime();
    }

    public void run() {
        if (preferences().getBool(KEY_AUTH, false)) {
            state = AuthState.LOGGED_IN;
            modules().onLoggedIn();
        } else {
            state = AuthState.AUTH_START;
        }
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
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
                request(new RequestSendAuthCode(phone, apiConfiguration.getAppId(),
                                apiConfiguration.getAppKey()),
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
                                publicKey,
                                deviceHash,
                                apiConfiguration.getAppTitle(),
                                apiConfiguration.getAppId(), apiConfiguration.getAppKey()),
                        new RpcCallback<ResponseAuth>() {

                            @Override
                            public void onResult(ResponseAuth response) {
                                preferences().putBool(KEY_AUTH, true);
                                state = AuthState.LOGGED_IN;
                                myUid = response.getUser().getId();
                                preferences().putInt(KEY_AUTH_UID, myUid);
                                modules().onLoggedIn();
                                updates().onUpdateReceived(new LoggedIn(response, new Runnable() {
                                    @Override
                                    public void run() {
                                        state = AuthState.LOGGED_IN;
                                        callback.onResult(state);
                                    }
                                }));
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
                        publicKey,
                        deviceHash,
                        apiConfiguration.getAppTitle(),
                        apiConfiguration.getAppId(), apiConfiguration.getAppKey(),
                        isSilent), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        preferences().putBool(KEY_AUTH, true);
                        state = AuthState.LOGGED_IN;
                        myUid = response.getUser().getId();
                        preferences().putInt(KEY_AUTH_UID, myUid);
                        modules().onLoggedIn();
                        updates().onUpdateReceived(new LoggedIn(response, new Runnable() {
                            @Override
                            public void run() {
                                state = AuthState.LOGGED_IN;
                                callback.onResult(state);
                            }
                        }));
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