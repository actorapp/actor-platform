/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.auth;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.ApiConfiguration;
import im.actor.core.AuthState;
import im.actor.core.api.ApiEmailActivationType;
import im.actor.core.api.ApiPhoneActivationType;
import im.actor.core.api.ApiSex;
import im.actor.core.api.rpc.RequestCompleteOAuth2;
import im.actor.core.api.rpc.RequestGetOAuth2Params;
import im.actor.core.api.rpc.RequestSendCodeByPhoneCall;
import im.actor.core.api.rpc.RequestSignUp;
import im.actor.core.api.rpc.RequestStartAnonymousAuth;
import im.actor.core.api.rpc.RequestStartEmailAuth;
import im.actor.core.api.rpc.RequestStartPhoneAuth;
import im.actor.core.api.rpc.RequestStartUsernameAuth;
import im.actor.core.api.rpc.RequestValidateCode;
import im.actor.core.api.rpc.RequestValidatePassword;
import im.actor.core.api.rpc.ResponseAuth;
import im.actor.core.api.rpc.ResponseGetOAuth2Params;
import im.actor.core.api.rpc.ResponseStartEmailAuth;
import im.actor.core.api.rpc.ResponseStartPhoneAuth;
import im.actor.core.api.rpc.ResponseStartUsernameAuth;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.AuthCodeRes;
import im.actor.core.entity.AuthMode;
import im.actor.core.entity.AuthRes;
import im.actor.core.entity.AuthStartRes;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.modules.Modules;
import im.actor.core.modules.AbsModule;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;

public class Authentication {

    private static final String TAG = "Authentication";

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";

    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";
    private static final String KEY_TRANSACTION_HASH = "auth_transaction_hash";
    //private static final String KEY_CODE = "auth_code";
    private static final String KEY_OAUTH_REDIRECT_URL = "oauth_redirect_url";

    private Modules modules;
    private AuthState state;

    private final ArrayList<String> langs;
    private final byte[] deviceHash;
    private final ApiConfiguration apiConfiguration;

    private int myUid;

    public Authentication(Modules modules) {
        this.modules = modules;

        // Keep device hash always stable across launch
        byte[] _deviceHash = modules.getPreferences().getBytes(KEY_DEVICE_HASH);
        if (_deviceHash == null) {
            _deviceHash = Crypto.SHA256(modules.getConfiguration().getApiConfiguration().getDeviceString().getBytes());
            modules.getPreferences().putBytes(KEY_DEVICE_HASH, _deviceHash);
        }
        deviceHash = _deviceHash;

        // Languages
        langs = new ArrayList<>();
        for (String s : modules.getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }

        // Api Configuration
        apiConfiguration = modules.getConfiguration().getApiConfiguration();

        // Authenticated UID
        myUid = modules.getPreferences().getInt(KEY_AUTH_UID, 0);
    }

    public int myUid() {
        return myUid;
    }

    public boolean isLoggedIn() {
        return state == AuthState.LOGGED_IN;
    }


    public void run() {
        if (modules.getPreferences().getBool(KEY_AUTH, false)) {
            state = AuthState.LOGGED_IN;
            modules.onLoggedIn(false);
        } else {
            state = AuthState.AUTH_START;
        }
    }

    public AuthenticationBackupData performBackup() {
        if (!isLoggedIn()) {
            return null;
        }

        byte[] userData = modules.getUsersModule().getUsersStorage().getValue(myUid).toByteArray();

        return new AuthenticationBackupData(deviceHash, myUid, userData);
    }

    public void restoreBackup(AuthenticationBackupData authenticationBackupData) {

        //
        // Restore UID
        //
        myUid = authenticationBackupData.getAuthenticatedUid();
        modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
        modules.getPreferences().putBool(KEY_AUTH, true);

        //
        // Restore User
        //
        try {
            modules.getUsersModule().getUsersStorage()
                    .addOrUpdateItem(new User(authenticationBackupData.getOwnUserData()));
        } catch (IOException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    //
    // Starting Authentication
    //

    public Promise<AuthStartRes> doStartEmailAuth(final String email) {
        return new Promise<>((PromiseFunc<AuthStartRes>) resolver -> request(new RequestStartEmailAuth(email,
                apiConfiguration.getAppId(),
                apiConfiguration.getAppKey(),
                deviceHash,
                apiConfiguration.getDeviceTitle(),
                modules.getConfiguration().getTimeZone(),
                langs), new RpcCallback<ResponseStartEmailAuth>() {
            @Override
            public void onResult(ResponseStartEmailAuth response) {
                resolver.result(new AuthStartRes(
                        response.getTransactionHash(),
                        AuthMode.fromApi(response.getActivationType()),
                        response.isRegistered()));
            }

            @Override
            public void onError(RpcException e) {
                resolver.error(e);
            }
        }));
    }

    public Promise<AuthStartRes> doStartPhoneAuth(final long phone) {
        return new Promise<>((PromiseFunc<AuthStartRes>) resolver -> request(new RequestStartPhoneAuth(phone,
                apiConfiguration.getAppId(),
                apiConfiguration.getAppKey(),
                deviceHash,
                apiConfiguration.getDeviceTitle(),
                modules.getConfiguration().getTimeZone(),
                langs), new RpcCallback<ResponseStartPhoneAuth>() {
            @Override
            public void onResult(ResponseStartPhoneAuth response) {
                resolver.result(new AuthStartRes(
                        response.getTransactionHash(),
                        AuthMode.fromApi(response.getActivationType()),
                        response.isRegistered()));
            }

            @Override
            public void onError(RpcException e) {
                resolver.error(e);
            }
        }));
    }


    //
    // Code And Password Validation
    //

    public Promise<AuthCodeRes> doValidateCode(final String transactionHash, final String code) {
        return new Promise<>((PromiseFunc<AuthCodeRes>) resolver -> request(new RequestValidateCode(transactionHash, code), new RpcCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth response) {
                resolver.result(new AuthCodeRes(new AuthRes(response.toByteArray())));
            }

            @Override
            public void onError(RpcException e) {
                if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())) {
                    resolver.result(new AuthCodeRes(transactionHash));
                } else {
                    resolver.error(e);
                }
            }
        }));
    }

    public Promise<Boolean> doSendCall(final String transactionHash) {
        return new Promise<>((PromiseFunc<Boolean>) resolver -> request(new RequestSendCodeByPhoneCall(transactionHash), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                resolver.result(true);
            }

            @Override
            public void onError(RpcException e) {
                resolver.error(e);
            }
        }));
    }


    //
    // Signup
    //

    public Promise<AuthRes> doSignup(final String name, final Sex sex, final String transactionHash) {
        return new Promise<>((PromiseFunc<AuthRes>) resolver -> request(new RequestSignUp(transactionHash, name, sex.toApi(), null), new RpcCallback<ResponseAuth>() {

            @Override
            public void onResult(ResponseAuth response) {
                resolver.result(new AuthRes(response.toByteArray()));
            }

            @Override
            public void onError(RpcException e) {
                resolver.error(e);
            }
        }));
    }


    //
    // Complete Authentication
    //

    public Promise<Boolean> doCompleteAuth(final AuthRes authRes) {
        return new Promise<>((PromiseFunc<Boolean>) resolver -> {
            ResponseAuth auth;
            try {
                auth = ResponseAuth.fromBytes(authRes.getData());
            } catch (IOException e) {
                e.printStackTrace();
                resolver.error(e);
                return;
            }

            state = AuthState.LOGGED_IN;
            myUid = auth.getUser().getId();
            modules.onLoggedIn(true);
            modules.getUsersModule().getUsersStorage().addOrUpdateItem(new User(auth.getUser(), null));
            modules.getPreferences().putBool(KEY_AUTH, true);
            modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
            resolver.result(true);
        });
    }


    //
    // Deprecated
    //

    @Deprecated
    public Command<AuthState> requestStartAnonymousAuth(final String userName) {
        return callback -> request(new RequestStartAnonymousAuth(userName,
                apiConfiguration.getAppId(),
                apiConfiguration.getAppKey(),
                deviceHash,
                apiConfiguration.getDeviceTitle(),
                modules.getConfiguration().getTimeZone(),
                langs), new RpcCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth response) {
                onLoggedIn(callback, response);
            }

            @Override
            public void onError(final RpcException e) {
                Runtime.postToMainThread(() -> {
                    Log.e(TAG, e);
                    callback.onError(e);
                });
            }
        });
    }

    @Deprecated
    public AuthState getAuthState() {
        return state;
    }

    @Deprecated
    public long getPhone() {
        return modules.getPreferences().getLong(KEY_PHONE, 0);
    }

    @Deprecated
    public String getEmail() {
        return modules.getPreferences().getString(KEY_EMAIL);
    }

    @Deprecated
    public Command<AuthState> requestStartEmailAuth(final String email) {
        return callback -> {
            ArrayList<String> langs1 = new ArrayList<>();
            for (String s : modules.getConfiguration().getPreferredLanguages()) {
                langs1.add(s);
            }
            request(new RequestStartEmailAuth(email,
                    apiConfiguration.getAppId(),
                    apiConfiguration.getAppKey(),
                    deviceHash,
                    apiConfiguration.getDeviceTitle(),
                    modules.getConfiguration().getTimeZone(),
                    langs1
            ), new RpcCallback<ResponseStartEmailAuth>() {
                @Override
                public void onResult(ResponseStartEmailAuth response) {
                    modules.getPreferences().putString(KEY_EMAIL, email);
                    modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                    ApiEmailActivationType emailActivationType = response.getActivationType();
                    if (emailActivationType.equals(ApiEmailActivationType.OAUTH2)) {
                        state = AuthState.GET_OAUTH_PARAMS;
                    } else if (emailActivationType.equals(ApiEmailActivationType.CODE)) {
                        state = AuthState.CODE_VALIDATION_EMAIL;
                    } else if (emailActivationType.equals(ApiEmailActivationType.PASSWORD)) {
                        state = AuthState.PASSWORD_VALIDATION;
                    } else {
                        state = AuthState.CODE_VALIDATION_EMAIL;
                    }

                    Runtime.postToMainThread(() -> callback.onResult(state));
                }

                @Override
                public void onError(final RpcException e) {
                    Runtime.postToMainThread(() -> {
                        Log.e(TAG, e);
                        callback.onError(e);
                    });
                }
            });
        };
    }

    @Deprecated
    public Command<AuthState> requestStartUserNameAuth(final String userName) {
        return callback -> {
            ArrayList<String> langs1 = new ArrayList<>();
            for (String s : modules.getConfiguration().getPreferredLanguages()) {
                langs1.add(s);
            }
            request(new RequestStartUsernameAuth(userName,
                    apiConfiguration.getAppId(),
                    apiConfiguration.getAppKey(),
                    deviceHash,
                    apiConfiguration.getDeviceTitle(),
                    modules.getConfiguration().getTimeZone(),
                    langs1), new RpcCallback<ResponseStartUsernameAuth>() {

                @Override
                public void onResult(ResponseStartUsernameAuth response) {
                    modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                    state = AuthState.PASSWORD_VALIDATION;

                    Runtime.postToMainThread(() -> callback.onResult(state));
                }

                @Override
                public void onError(final RpcException e) {
                    Runtime.postToMainThread(() -> {
                        Log.e(TAG, e);
                        callback.onError(e);
                    });
                }
            });
        };
    }

    @Deprecated
    public Command<AuthState> requestStartPhoneAuth(final long phone) {
        return callback -> {
            ArrayList<String> langs1 = new ArrayList<>();
            for (String s : modules.getConfiguration().getPreferredLanguages()) {
                langs1.add(s);
            }
            request(new RequestStartPhoneAuth(phone,
                    apiConfiguration.getAppId(),
                    apiConfiguration.getAppKey(),
                    deviceHash,
                    apiConfiguration.getDeviceTitle(),
                    modules.getConfiguration().getTimeZone(),
                    langs1
            ), new RpcCallback<ResponseStartPhoneAuth>() {
                @Override
                public void onResult(ResponseStartPhoneAuth response) {
                    modules.getPreferences().putLong(KEY_PHONE, phone);
                    modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                    if (response.getActivationType() == ApiPhoneActivationType.CODE) {
                        state = AuthState.CODE_VALIDATION_PHONE;
                    } else if (response.getActivationType() == ApiPhoneActivationType.PASSWORD) {
                        state = AuthState.PASSWORD_VALIDATION;
                    } else {
                        state = AuthState.CODE_VALIDATION_PHONE;
                    }

                    Runtime.postToMainThread(() -> callback.onResult(state));
                }

                @Override
                public void onError(final RpcException e) {
                    Runtime.postToMainThread(() -> {
                        Log.e(TAG, e);
                        callback.onError(e);
                    });
                }
            }, AbsModule.RPC_TIMEOUT);
        };
    }

    @Deprecated
    public Command<AuthState> requestGetOAuth2Params() {
        return callback -> request(new RequestGetOAuth2Params(modules.getPreferences().getString(KEY_TRANSACTION_HASH),
                        "https://actor.im/auth/oauth2callback"),
                new RpcCallback<ResponseGetOAuth2Params>() {
                    @Override
                    public void onResult(final ResponseGetOAuth2Params response) {
                        modules.getPreferences().putString(KEY_OAUTH_REDIRECT_URL, response.getAuthUrl());

                        state = AuthState.COMPLETE_OAUTH;

                        Runtime.postToMainThread(() -> callback.onResult(state));
                    }

                    @Override
                    public void onError(final RpcException e) {
                        Runtime.postToMainThread(() -> {
                            Log.e(TAG, e);
                            callback.onError(e);
                        });
                    }
                });
    }

    @Deprecated
    public Command<AuthState> requestCompleteOauth(final String code) {
        return callback -> request(new RequestCompleteOAuth2(modules.getPreferences().getString(KEY_TRANSACTION_HASH), code), new RpcCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth response) {
                onLoggedIn(callback, response);
            }

            @Override
            public void onError(final RpcException e) {
                if ("EMAIL_EXPIRED".equals(e.getTag())) {
                    resetAuth();
                } else if ("EMAIL_UNOCCUPIED".equals(e.getTag())) {
                    // modules.getPreferences().putString(KEY_CODE, code);
                    state = AuthState.SIGN_UP;
                    callback.onResult(AuthState.SIGN_UP);
                    return;
                }

                Runtime.postToMainThread(() -> {
                    Log.e(TAG, e);
                    callback.onError(e);
                });
            }
        });
    }

    @Deprecated
    public Command<AuthState> signUp(final String name, final ApiSex sex, final String avatarPath) {
        return callback -> request(new RequestSignUp(modules.getPreferences().getString(KEY_TRANSACTION_HASH), name, sex,
                null), new RpcCallback<ResponseAuth>() {
            @Override
            public void onResult(ResponseAuth response) {
                onLoggedIn(callback, response);
                if (avatarPath != null) {
                    modules.getProfileModule().changeAvatar(avatarPath);
                }
            }

            @Override
            public void onError(final RpcException e) {
                if ("EMAIL_CODE_EXPIRED".equals(e.getTag())) {
                    resetAuth();
                }
                Runtime.postToMainThread(() -> {
                    Log.e(TAG, e);
                    callback.onError(e);
                });
            }
        });
    }

    @Deprecated
    public Command<AuthState> requestValidateCode(final String code) {
        if (code == null) {
            throw new RuntimeException("Code couldn't be null!");
        }
        return callback -> {
            String transactionHash = modules.getPreferences()
                    .getString(KEY_TRANSACTION_HASH);

            request(new RequestValidateCode(transactionHash, code), new RpcCallback<ResponseAuth>() {
                @Override
                public void onResult(ResponseAuth response) {
                    onLoggedIn(callback, response);
                }

                @Override
                public void onError(final RpcException e) {
                    if ("PHONE_CODE_EXPIRED".equals(e.getTag()) || "EMAIL_CODE_EXPIRED".equals(e.getTag())) {
                        resetAuth();
                    } else if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())) {
                        // modules.getPreferences().putString(KEY_CODE, code);
                        state = AuthState.SIGN_UP;
                        Runtime.postToMainThread(() -> callback.onResult(AuthState.SIGN_UP));
                        return;
                    }

                    Runtime.postToMainThread(() -> {
                        Log.e(TAG, e);
                        callback.onError(e);
                    });
                }
            });
        };
    }

    @Deprecated
    public Command<AuthState> requestValidatePassword(final String password) {
        return callback -> {
            String transactionHash = modules.getPreferences()
                    .getString(KEY_TRANSACTION_HASH);
            request(new RequestValidatePassword(transactionHash, password),
                    new RpcCallback<ResponseAuth>() {
                        @Override
                        public void onResult(ResponseAuth response) {
                            onLoggedIn(callback, response);
                        }

                        @Override
                        public void onError(final RpcException e) {
                            if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())) {
                                state = AuthState.SIGN_UP;
                                Runtime.postToMainThread(() -> callback.onResult(AuthState.SIGN_UP));
                                return;
                            }

                            Runtime.postToMainThread(() -> {
                                Log.e(TAG, e);
                                callback.onError(e);
                            });
                        }
                    });
        };
    }

    @Deprecated
    public Command<Boolean> requestCallActivation() {
        return callback -> {
            String transactionHash = modules.getPreferences().getString(KEY_TRANSACTION_HASH);

            request(new RequestSendCodeByPhoneCall(transactionHash), new RpcCallback<ResponseVoid>() {
                @Override
                public void onResult(ResponseVoid response) {
                    callback.onResult(true);
                }

                @Override
                public void onError(RpcException e) {
                    callback.onError(e);
                }
            });
        };
    }

    @Deprecated
    public void resetAuth() {
        state = AuthState.AUTH_START;
    }

    @Deprecated
    public void resetModule() {
        // Clearing authentication
        state = AuthState.AUTH_START;
        myUid = 0;
        modules.getPreferences().putBool(KEY_AUTH, false);
        modules.getPreferences().putInt(KEY_AUTH_UID, 0);
        modules.getPreferences().putLong(KEY_PHONE, 0);
        modules.getPreferences().putString(KEY_SMS_HASH, null);
        modules.getPreferences().putInt(KEY_SMS_CODE, 0);
    }

    @Deprecated
    private void onLoggedIn(final CommandCallback<AuthState> callback, ResponseAuth response) {
        state = AuthState.LOGGED_IN;
        myUid = response.getUser().getId();
        modules.onLoggedIn(true);
        modules.getUsersModule().getUsersStorage().addOrUpdateItem(new User(response.getUser(), null));
        modules.getPreferences().putBool(KEY_AUTH, true);
        modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
        callback.onResult(state);
    }

    private <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        modules.getActorApi().request(request, callback);
    }

    private <T extends Response> void request(Request<T> request, RpcCallback<T> callback, long timeout) {
        modules.getActorApi().request(request, callback, timeout);
    }
}