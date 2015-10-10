/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import java.util.ArrayList;

import im.actor.core.ApiConfiguration;
import im.actor.core.AuthState;
import im.actor.core.api.ApiEmailActivationType;
import im.actor.core.api.ApiSex;
import im.actor.core.api.rpc.RequestCompleteOAuth2;
import im.actor.core.api.rpc.RequestGetOAuth2Params;
import im.actor.core.api.rpc.RequestSendCodeByPhoneCall;
import im.actor.core.api.rpc.RequestSignUp;
import im.actor.core.api.rpc.RequestStartEmailAuth;
import im.actor.core.api.rpc.RequestStartPhoneAuth;
import im.actor.core.api.rpc.RequestValidateCode;
import im.actor.core.api.rpc.ResponseAuth;
import im.actor.core.api.rpc.ResponseGetOAuth2Params;
import im.actor.core.api.rpc.ResponseStartEmailAuth;
import im.actor.core.api.rpc.ResponseStartPhoneAuth;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.ContactRecord;
import im.actor.core.entity.ContactRecordType;
import im.actor.core.entity.User;
import im.actor.core.modules.updates.internal.LoggedIn;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.parser.Request;
import im.actor.core.network.parser.Response;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.Runtime;

public class Authentication {

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_EMAIL = "auth_email";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";
    private static final String KEY_TRANSACTION_HASH = "auth_transaction_hash";
    private static final String KEY_CODE = "auth_code";
    private static final String KEY_OAUTH_REDIRECT_URL = "oauth_redirect_url";

    private Modules modules;
    private AuthState state;

    private byte[] deviceHash;
    private ApiConfiguration apiConfiguration;

    private int myUid;

    public Authentication(Modules modules) {
        this.modules = modules;

        this.myUid = modules.getPreferences().getInt(KEY_AUTH_UID, 0);

        // Keep device hash always stable across launch
        deviceHash = modules.getPreferences().getBytes(KEY_DEVICE_HASH);
        if (deviceHash == null) {
            deviceHash = Crypto.SHA256(modules.getConfiguration().getApiConfiguration().getDeviceString().getBytes());
            modules.getPreferences().putBytes(KEY_DEVICE_HASH, deviceHash);
        }

        apiConfiguration = modules.getConfiguration().getApiConfiguration();
    }

    public int myUid() {
        return myUid;
    }

    public AuthState getAuthState() {
        return state;
    }

    public boolean isLoggedIn() {
        return state == AuthState.LOGGED_IN;
    }

    public long getPhone() {
        return modules.getPreferences().getLong(KEY_PHONE, 0);
    }

    public String getEmail() {
        return modules.getPreferences().getString(KEY_EMAIL);
    }

    public void run() {
        if (modules.getPreferences().getBool(KEY_AUTH, false)) {
            state = AuthState.LOGGED_IN;
            modules.onLoggedIn();

            // Notify ActorAnalytics
            User user = modules.getUsersModule().getUsersStorage().getValue(myUid);
            ArrayList<Long> records = new ArrayList<Long>();
            for (ContactRecord contactRecord : user.getRecords()) {
                if (contactRecord.getRecordType() == ContactRecordType.PHONE) {
                    records.add(Long.parseLong(contactRecord.getRecordData()));
                }
            }
        } else {
            state = AuthState.AUTH_START;
        }
    }

    public Command<AuthState> requestStartEmailAuth(final String email) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestStartEmailAuth(email,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle()
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
                                callback.onError(e);
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<AuthState> requestStartPhoneAuth(final long phone) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestStartPhoneAuth(phone,
                        apiConfiguration.getAppId(),
                        apiConfiguration.getAppKey(),
                        deviceHash,
                        apiConfiguration.getDeviceTitle()
                ), new RpcCallback<ResponseStartPhoneAuth>() {
                    @Override
                    public void onResult(ResponseStartPhoneAuth response) {
                        modules.getPreferences().putLong(KEY_PHONE, phone);
                        modules.getPreferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                        state = AuthState.CODE_VALIDATION_PHONE;

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
                                callback.onError(e);
                                e.printStackTrace();
                            }
                        });
                    }
                });
            }
        };
    }

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
                                        callback.onError(e);
                                        e.printStackTrace();
                                    }
                                });
                            }
                        });
            }
        };
    }

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
                            modules.getPreferences().putString(KEY_CODE, code);
                            state = AuthState.SIGN_UP;
                            callback.onResult(AuthState.SIGN_UP);
                            return;
                        }

                        Runtime.postToMainThread(new Runnable() {
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

    public Command<AuthState> signUp(final String name, final ApiSex sex, final String avatarPath) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUp(modules.getPreferences().getString(KEY_TRANSACTION_HASH), name, sex), new RpcCallback<ResponseAuth>() {
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
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<AuthState> requestValidateCode(final String code) {
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
                            modules.getPreferences().putString(KEY_CODE, code);
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
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

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

    public void resetAuth() {
        state = AuthState.AUTH_START;
    }

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

    private void onLoggedIn(final CommandCallback<AuthState> callback, ResponseAuth response) {
        state = AuthState.LOGGED_IN;
        myUid = response.getUser().getId();
        modules.getPreferences().putBool(KEY_AUTH, true);
        modules.getPreferences().putInt(KEY_AUTH_UID, myUid);
        modules.onLoggedIn();
        modules.getUpdatesModule().onUpdateReceived(new LoggedIn(response, new Runnable() {
            @Override
            public void run() {

                state = AuthState.LOGGED_IN;

                callback.onResult(state);

                // Notify ActorAnalytics
                User user = modules.getUsersModule().getUsersStorage().getValue(myUid);
                ArrayList<Long> records = new ArrayList<Long>();
                for (ContactRecord contactRecord : user.getRecords()) {
                    if (contactRecord.getRecordType() == ContactRecordType.PHONE) {
                        records.add(Long.parseLong(contactRecord.getRecordData()));
                    }
                }
            }
        }), 500L);
    }

    private <T extends Response> void request(Request<T> request, RpcCallback<T> callback) {
        modules.getActorApi().request(request, callback);
    }
}