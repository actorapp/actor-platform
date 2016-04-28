/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import im.actor.core.modules.api.entity.SignUpNameState;
import im.actor.core.modules.sequence.internal.LoggedIn;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.json.JSONObject;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.sdk.intents.WebServiceUtil;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class Authentication {

    private static final String TAG = "Authentication";

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_NICKNAME = "auth_nickname";
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
            modules.onLoggedIn();
        } else {
            state = AuthState.AUTH_START;
        }
    }


    //
    // Starting Authentication
    //

    public Promise<AuthStartRes> doStartEmailAuth(final String email) {
        return new Promise<>(new PromiseFunc<AuthStartRes>() {
            @Override
            public void exec(@NotNull final PromiseResolver<AuthStartRes> resolver) {
                request(new RequestStartEmailAuth(email,
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
                });
            }
        });
    }

    public Promise<AuthStartRes> doStartPhoneAuth(final long phone) {
        return new Promise<>(new PromiseFunc<AuthStartRes>() {
            @Override
            public void exec(@NotNull final PromiseResolver<AuthStartRes> resolver) {
                request(new RequestStartPhoneAuth(phone,
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
                });
            }
        });
    }

       public Promise<AuthStartRes> doStartUsernameAuth(final String username) {
           return new Promise<>(new PromiseFunc<AuthStartRes>() {
               @Override
               public void exec(@NotNull final PromiseResolver<AuthStartRes> resolver) {
                   request(new RequestStartUsernameAuth(username,
                           apiConfiguration.getAppId(),
                           apiConfiguration.getAppKey(),
                           deviceHash,
                           apiConfiguration.getDeviceTitle(),
                           modules.getConfiguration().getTimeZone(),
                           langs), new RpcCallback<ResponseStartUsernameAuth>() {
                       @Override
                       public void onResult(ResponseStartUsernameAuth response) {
                           resolver.result(new AuthStartRes(
                                   response.getTransactionHash(),
                                   AuthMode.fromApi(response.getActivationType()),
                                   response.isRegistered()));
                       }

                       @Override
                       public void onError(RpcException e) {
                           resolver.error(e);
                       }
                   });
               }
           });
       }

    //
    // Code And Password Validation
    //

    public Promise<AuthCodeRes> doValidateCode(final String transactionHash, final String code) {
        return new Promise<>(new PromiseFunc<AuthCodeRes>() {
            @Override
            public void exec(@NotNull final PromiseResolver<AuthCodeRes> resolver) {
                request(new RequestValidateCode(transactionHash, code), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        resolver.result(new AuthCodeRes(new AuthRes(response.toByteArray())));
                    }

                    @Override
                    public void onError(RpcException e) {
                        if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())|| "USERNAME_UNOCCUPIED".equals(e.getTag())) {
                            resolver.result(new AuthCodeRes(transactionHash));
                        } else {
                            resolver.error(e);
                        }
                    }
                });
            }
        });
    }

        public Promise<AuthCodeRes> doValidatePassword(final String transactionHash, final String password) {
            return new Promise<>(new PromiseFunc<AuthCodeRes>() {
                @Override
                public void exec(@NotNull final PromiseResolver<AuthCodeRes> resolver) {
                    request(new RequestValidatePassword(transactionHash, password), new RpcCallback<ResponseAuth>() {
                        @Override
                        public void onResult(ResponseAuth response) {
                            resolver.result(new AuthCodeRes(new AuthRes(response.toByteArray())));
                        }

                        @Override
                        public void onError(RpcException e) {
                            if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())|| "USERNAME_UNOCCUPIED".equals(e.getTag())) {
                                resolver.result(new AuthCodeRes(transactionHash));
                            } else {
                                resolver.error(e);
                            }
                        }
                    });
                }
            });
        }

    public Promise<Boolean> doSendCall(final String transactionHash) {
        return new Promise<>(new PromiseFunc<Boolean>() {
            @Override
            public void exec(@NotNull final PromiseResolver<Boolean> resolver) {
                request(new RequestSendCodeByPhoneCall(transactionHash), new RpcCallback<ResponseVoid>() {
                    @Override
                    public void onResult(ResponseVoid response) {
                        resolver.result(true);
                    }

                    @Override
                    public void onError(RpcException e) {
                        resolver.error(e);
                    }
                });
            }
        });
    }


    //
    // Signup
    //

    public Promise<AuthRes> doSignup(final String name, final Sex sex, final String transactionHash) {
        return new Promise<>(new PromiseFunc<AuthRes>() {
            @Override
            public void exec(@NotNull final PromiseResolver<AuthRes> resolver) {
                request(new RequestSignUp(transactionHash, name, sex.toApi(), null), new RpcCallback<ResponseAuth>() {

                    @Override
                    public void onResult(ResponseAuth response) {
                        resolver.result(new AuthRes(response.toByteArray()));
                    }

                    @Override
                    public void onError(RpcException e) {
                        resolver.error(e);
                    }
                });
            }
        });
    }


    //
    // Complete Authentication
    //

    public Promise<Boolean> doCompleteAuth(final AuthRes authRes) {
        return new Promise<>(new PromiseFunc<Boolean>() {
            @Override
            public void exec(@NotNull final PromiseResolver<Boolean> resolver) {
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
                modules.getPreferences().putBool(KEY_AUTH, true);
                modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
                modules.onLoggedIn();
                modules.getUsersModule().getUsersStorage().addOrUpdateItem(new User(auth.getUser()));
                modules.getUpdatesModule().onUpdateReceived(new LoggedIn(auth, new Runnable() {
                    @Override
                    public void run() {
                        resolver.result(true);
                    }
                }));
            }
        });
    }


    //
    // Deprecated
    //

    @Deprecated
    public Command<AuthState> requestStartAnonymousAuth(final String userName) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestStartAnonymousAuth(userName,
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
                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
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
    public String getUserName() {
        return modules.getPreferences().getString(KEY_NICKNAME);
    }

    @Deprecated
    public Command<AuthState> requestStartEmailAuth(final String email) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                ArrayList<String> langs = new ArrayList<>();
                for (String s : modules.getConfiguration().getPreferredLanguages()) {
                    langs.add(s);
                }
                request(new RequestStartEmailAuth(email,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle(),
                        modules.getConfiguration().getTimeZone(),
                        langs
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

                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(state);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestStartUserNameAuth(final String userName) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                ArrayList<String> langs = new ArrayList<>();
                for (String s : modules.getConfiguration().getPreferredLanguages()) {
                    langs.add(s);
                }
                request(new RequestStartUsernameAuth(userName,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle(),
                        modules.getConfiguration().getTimeZone(),
                        langs), new RpcCallback<ResponseStartUsernameAuth>() {

                    @Override
                    public void onResult(ResponseStartUsernameAuth response) {
                        modules.getPreferences().putString(KEY_NICKNAME, userName);
                        modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                        state = AuthState.PASSWORD_VALIDATION;

                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(state);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestStartPhoneAuth(final long phone) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                ArrayList<String> langs = new ArrayList<>();
                for (String s : modules.getConfiguration().getPreferredLanguages()) {
                    langs.add(s);
                }
                request(new RequestStartPhoneAuth(phone,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle(),
                        modules.getConfiguration().getTimeZone(),
                        langs
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

                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(state);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestGetOAuth2Params() {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestGetOAuth2Params(modules.getPreferences().getString(KEY_TRANSACTION_HASH),
                                "https://actor.im/auth/oauth2callback"),
                        new RpcCallback<ResponseGetOAuth2Params>() {
                            @Override
                            public void onResult(final ResponseGetOAuth2Params response) {
                                modules.getPreferences().putString(KEY_OAUTH_REDIRECT_URL, response.getAuthUrl());

                                state = AuthState.COMPLETE_OAUTH;

                                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(state);
                                    }
                                });
                            }

                            @Override
                            public void onError(final RpcException e) {
                                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(TAG, e);
                                        callback.onError(e);
                                    }
                                });
                            }
                        });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestCompleteOauth(final String code) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestCompleteOAuth2(modules.getPreferences().getString(KEY_TRANSACTION_HASH), code), new RpcCallback<ResponseAuth>() {
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

                        Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> signUp(final String name, final ApiSex sex, final String avatarPath) {
        return signUp(name, sex, avatarPath, null);
    }

    @Deprecated
    public Command<AuthState> signUp(final String name, final ApiSex sex, final String avatarPath, final String password) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUp(modules.getPreferences().getString(KEY_TRANSACTION_HASH), name, sex,
                        password), new RpcCallback<ResponseAuth>() {
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
                        Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestValidateCode(final String code) {
        if (code == null) {
            throw new RuntimeException("Code couldn't be null!");
        }
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
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
                            Runtime.postToMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onResult(AuthState.SIGN_UP);
                                }
                            });
                            return;
                        }

                        Runtime.postToMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, e);
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    @Deprecated
    public Command<AuthState> requestValidatePassword(final String password) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
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
//                                if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())) {
//                                    state = AuthState.SIGN_UP;
//                                    Runtime.postToMainThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            callback.onResult(AuthState.SIGN_UP);
//                                        }
//                                    });
//                                    return;
//                                }

                                if ("USERNAME_CODE_EXPIRED".equals(e.getTag()) || "PHONE_CODE_EXPIRED".equals(e.getTag()) || "EMAIL_CODE_EXPIRED".equals(e.getTag())) {
                                     resetAuth();
                                } else if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag()) || "USERNAME_UNOCCUPIED".equals(e.getTag())) {
                                    state = AuthState.SIGN_UP;
                                    Runtime.postToMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onResult(AuthState.SIGN_UP);
                                        }
                                    });
                                    return;
                                }

                                Runtime.postToMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(TAG, e);
                                        callback.onError(e);
                                    }
                                });
                            }
                        });
            }
        };
    }


    @Deprecated
    public Command<AuthState> requestSignUp(final String username,final String zhname,final String ip) {
        ArrayList<String> langs = new ArrayList<>();
        for (String s : modules.getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }
        System.out.println("requestSignUp" + username);
        request(new RequestStartUsernameAuth(username,
                apiConfiguration.getAppId(),
                apiConfiguration.getAppKey(),
                deviceHash,
                apiConfiguration.getDeviceTitle(),
                modules.getConfiguration().getTimeZone(),
                langs), new RpcCallback<ResponseStartUsernameAuth>() {

            @Override
            public void onResult(ResponseStartUsernameAuth response) {
                final String hash = response.getTransactionHash();
                System.out.println("onResult" + username);

//                modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        request(new RequestSignUp(hash, username, ApiSex.UNKNOWN,
                                "11111111"), new RpcCallback<ResponseAuth>() {
                            @Override
                            public void onResult(ResponseAuth response) {

                            }

                            @Override
                            public void onError(final RpcException e) {
                                if ("NICKNAME_BUSY".equals(e.getTag())) {
                                    HashMap<String, String> par = new HashMap<String, String>();
                                    par.put("oaUserName", username);
                                    WebServiceUtil.webServiceRun(ip, par, "syncUser", new SignUpHandeler());
                                } else {
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final RpcException e) {
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, e);
                    }
                });
            }
        });
        return null;
    }


    class SignUpHandeler extends Handler {

        public SignUpHandeler() {
        }

        public SignUpHandeler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String datasource = b.getString("datasource");
            try {
                JSONObject jo = new JSONObject(datasource);
                String result = jo.getString("result");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    @Deprecated
    public Command<AuthState> batchSignUp(final List<SignUpNameState> userNameList) {
        if (userNameList == null) {
            return null;
        }
        signUpName(userNameList);
        return null;
//        return new Command<AuthState>() {
//            @Override
//            public void start(final CommandCallback<AuthState> callback) {
//                signUpName(userNameList);
//            }
//        };
    }

    private void requestSignUp(final String name, final List<SignUpNameState> userNameList) {
        ArrayList<String> langs = new ArrayList<>();
        for (String s : modules.getConfiguration().getPreferredLanguages()) {
            langs.add(s);
        }
        System.out.println("requestSignUp" + name);
        request(new RequestStartUsernameAuth(name,
                apiConfiguration.getAppId(),
                apiConfiguration.getAppKey(),
                deviceHash,
                apiConfiguration.getDeviceTitle(),
                modules.getConfiguration().getTimeZone(),
                langs), new RpcCallback<ResponseStartUsernameAuth>() {

            @Override
            public void onResult(ResponseStartUsernameAuth response) {
                final String hash = response.getTransactionHash();
                System.out.println("onResult" + name);

//                modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        request(new RequestSignUp(hash, name, ApiSex.UNKNOWN,
                                null), new RpcCallback<ResponseAuth>() {
                            @Override
                            public void onResult(ResponseAuth response) {

                            }

                            @Override
                            public void onError(final RpcException e) {
                                if ("NICKNAME_BUSY".equals(e.getTag())) {
//                                    resetAuth();
                                    for (int i = 0; i < userNameList.size(); i++) {
                                        if (userNameList.get(i).getName() == name) {
                                            userNameList.get(i).setState(2);
                                            if (i < userNameList.size() - 1) {
                                                Runtime.postToMainThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        signUpName(userNameList);
                                                    }
                                                });
                                            }
                                            return;
                                        }
                                    }
                                } else {
//                                    for (int i = 0; i < userNameList.size(); i++) {
//                                        signUpName(userNameList);
//                                    }
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final RpcException e) {
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, e);
                    }
                });
            }
        });

    }

    private void signUpName(List<SignUpNameState> userNameList) {
        for (int i = 0; i < userNameList.size(); i++) {
            if (userNameList.get(i).getState() == 0) {
                requestSignUp(userNameList.get(i).getName(), userNameList);
                break;
            } else if (i == userNameList.size() - 1) {
                userNameList.clear();
                userNameList = null;
                break;
            }
        }
    }

    @Deprecated
    public Command<Boolean> requestCallActivation() {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
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
            }
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
        modules.getPreferences().putBool(KEY_AUTH, true);
        modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
        modules.onLoggedIn();
        modules.getUsersModule().getUsersStorage().addOrUpdateItem(new User(response.getUser()));
        modules.getUpdatesModule().onUpdateReceived(new LoggedIn(response, new Runnable() {
            @Override
            public void run() {
                state = AuthState.LOGGED_IN;
                callback.onResult(state);
            }
        }));
    }

    private <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        modules.getActorApi().request(request, callback);
    }
}