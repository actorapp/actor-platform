/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.ArrayList;

import im.actor.model.ApiConfiguration;
import im.actor.model.AuthState;
import im.actor.model.api.EmailActivationType;
import im.actor.model.api.Sex;
import im.actor.model.api.rpc.RequestCompleteOAuth2;
import im.actor.model.api.rpc.RequestGetOAuth2Params;
import im.actor.model.api.rpc.RequestSendAuthCodeObsolete;
import im.actor.model.api.rpc.RequestSignInObsolete;
import im.actor.model.api.rpc.RequestSignUp;
import im.actor.model.api.rpc.RequestSignUpObsolete;
import im.actor.model.api.rpc.RequestStartEmailAuth;
import im.actor.model.api.rpc.RequestStartPhoneAuth;
import im.actor.model.api.rpc.RequestValidateCode;
import im.actor.model.api.rpc.ResponseAuth;
import im.actor.model.api.rpc.ResponseGetOAuth2Params;
import im.actor.model.api.rpc.ResponseSendAuthCodeObsolete;
import im.actor.model.api.rpc.ResponseStartEmailAuth;
import im.actor.model.api.rpc.ResponseStartPhoneAuth;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.ContactRecordType;
import im.actor.model.entity.User;
import im.actor.model.modules.updates.internal.LoggedIn;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class Auth extends BaseModule {

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    public static final String KEY_EMAIL = "auth_email";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";
    private static final String KEY_TRANSACTION_HASH = "auth_transaction_hash";
    private static final String KEY_CODE = "auth_code";
    public static final String KEY_OAUTH_REDIRECT_URL = "oauth_redirect_url";

    private AuthState state;

    private byte[] deviceHash;
    private ApiConfiguration apiConfiguration;

    private int myUid;

    public Auth(Modules modules) {
        super(modules);

        this.myUid = preferences().getInt(KEY_AUTH_UID, 0);

        // Keep device hash always stable across launch
        deviceHash = preferences().getBytes(KEY_DEVICE_HASH);
        if (deviceHash == null) {
            deviceHash = CryptoUtils.SHA256(modules.getConfiguration().getApiConfiguration().getDeviceString().getBytes());
            preferences().putBytes(KEY_DEVICE_HASH, deviceHash);
        }

        apiConfiguration = modules.getConfiguration().getApiConfiguration();
    }

    public void run() {
        if (preferences().getBool(KEY_AUTH, false)) {
            state = AuthState.LOGGED_IN;
            modules().onLoggedIn();

            // Notify Analytics
            User user = modules().getUsersModule().getUsers().getValue(myUid);
            ArrayList<Long> records = new ArrayList<Long>();
            for (ContactRecord contactRecord : user.getRecords()) {
                if (contactRecord.getRecordType() == ContactRecordType.PHONE) {
                    records.add(Long.parseLong(contactRecord.getRecordData()));
                }
            }
            modules().getAnalytics().onLoggedIn(CryptoUtils.hex(deviceHash), user.getUid(),
                    records.toArray(new Long[records.size()]), user.getName());
        } else {
            state = AuthState.AUTH_START;

            // Notify Analytics
            modules().getAnalytics().onLoggedOut(CryptoUtils.hex(deviceHash));
        }
    }

    private void onLoggedIn(final CommandCallback<AuthState> callback, ResponseAuth response) {


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

                // Notify Analytics
                User user = modules().getUsersModule().getUsers().getValue(myUid);
                ArrayList<Long> records = new ArrayList<Long>();
                for (ContactRecord contactRecord : user.getRecords()) {
                    if (contactRecord.getRecordType() == ContactRecordType.PHONE) {
                        records.add(Long.parseLong(contactRecord.getRecordData()));
                    }
                }
                modules().getAnalytics().onLoggedInPerformed(CryptoUtils.hex(deviceHash), user.getUid(),
                        records.toArray(new Long[records.size()]), user.getName());
            }
        }), 500L);
    }

    public int myUid() {
        return myUid;
    }

    public AuthState getAuthState() {
        return state;
    }

    public Command<AuthState> requestSmsObsolete(final long phone) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                modules().getAnalytics().trackCodeRequest(phone);

                request(new RequestSendAuthCodeObsolete(phone, apiConfiguration.getAppId(),
                                apiConfiguration.getAppKey()),
                        new RpcCallback<ResponseSendAuthCodeObsolete>() {
                            @Override
                            public void onResult(final ResponseSendAuthCodeObsolete response) {
                                preferences().putLong(KEY_PHONE, phone);
                                preferences().putString(KEY_SMS_HASH, response.getSmsHash());
                                state = AuthState.CODE_VALIDATION_PHONE;

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

    public Command<AuthState> sendCodeObsolete(final int code) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(
                        new RequestSignInObsolete(
                                preferences().getLong(KEY_PHONE, 0),
                                preferences().getString(KEY_SMS_HASH),
                                code + "",
                                deviceHash,
                                apiConfiguration.getDeviceTitle(),
                                apiConfiguration.getAppId(), apiConfiguration.getAppKey()),
                        new RpcCallback<ResponseAuth>() {

                            @Override
                            public void onResult(ResponseAuth response) {
                                onLoggedIn(callback, response);
                            }

                            @Override
                            public void onError(final RpcException e) {
                                if ("PHONE_CODE_EXPIRED".equals(e.getTag())) {
                                    resetAuth();
                                } else if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag())) {
                                    preferences().putInt(KEY_SMS_CODE, code);
                                    state = AuthState.SIGN_UP;
                                    callback.onResult(AuthState.SIGN_UP);
                                    return;
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

    public Command<AuthState> signUpObsolete(final String firstName, final String avatarPath, final boolean isSilent) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUpObsolete(preferences().getLong(KEY_PHONE, 0),
                        preferences().getString(KEY_SMS_HASH),
                        preferences().getInt(KEY_SMS_CODE, 0) + "",
                        firstName,
                        deviceHash,
                        apiConfiguration.getDeviceTitle(),
                        apiConfiguration.getAppId(), apiConfiguration.getAppKey(),
                        isSilent), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        onLoggedIn(callback, response);
                        if (avatarPath != null) {
                            modules().getProfile().changeAvatar(avatarPath);
                        }
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
                        preferences().putString(KEY_EMAIL, email);
                        preferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());
                        EmailActivationType emailActivationType = response.getActivationType();
                        if(emailActivationType.equals(EmailActivationType.OAUTH2)){
                            state = AuthState.GET_OAUTH_PARAMS;
                        }else if(emailActivationType.equals(EmailActivationType.CODE)){
                            state = AuthState.CODE_VALIDATION_EMAIL;
                        }

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
                        preferences().putLong(KEY_PHONE, phone);
                        preferences().putString(KEY_TRANSACTION_HASH, response.getTransactionHash());

                        state = AuthState.CODE_VALIDATION_PHONE;

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
                request(new RequestGetOAuth2Params(preferences().getString(KEY_TRANSACTION_HASH), "https://actor.im/auth/oauth2callback"),
                        new RpcCallback<ResponseGetOAuth2Params>() {
                            @Override
                            public void onResult(final ResponseGetOAuth2Params response) {
                                state = AuthState.COMPLETE_OAUTH;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        preferences().putString(KEY_OAUTH_REDIRECT_URL, response.getAuthUrl());
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
                                        e.printStackTrace();
                                    }
                                });
                            }
                        });
            }
        };
    }

    public Command<AuthState> requestCompleteOauth(final String code){
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestCompleteOAuth2(preferences().getString(KEY_TRANSACTION_HASH), code), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        onLoggedIn(callback, response);
                    }

                    @Override
                    public void onError(final RpcException e) {
                        if ("EMAIL_EXPIRED".equals(e.getTag())) {
                            resetAuth();
                        } else if ("EMAIL_UNOCCUPIED".equals(e.getTag())) {
                            preferences().putString(KEY_CODE, code);
                            state = AuthState.SIGN_UP;
                            callback.onResult(AuthState.SIGN_UP);
                            return;
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

    public Command<AuthState> signUp(final String name, final Sex sex, final String avatarPath){
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUp(preferences().getString(KEY_TRANSACTION_HASH), name, sex), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        onLoggedIn(callback, response);
                        if (avatarPath != null) {
                            modules().getProfile().changeAvatar(avatarPath);
                        }
                    }

                    @Override
                    public void onError(final RpcException e) {
                        if ("EMAIL_CODE_EXPIRED".equals(e.getTag())) {
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

    public Command<AuthState> requestValidateCode(final String code){
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestValidateCode(preferences().getString(KEY_TRANSACTION_HASH), code), new RpcCallback<ResponseAuth>() {
                    @Override
                    public void onResult(ResponseAuth response) {
                        onLoggedIn(callback, response);
                    }

                    @Override
                    public void onError(final RpcException e) {
                        if ("PHONE_CODE_EXPIRED".equals(e.getTag()) || "EMAIL_CODE_EXPIRED".equals(e.getTag())) {
                            resetAuth();
                        } else if ("PHONE_NUMBER_UNOCCUPIED".equals(e.getTag()) || "EMAIL_UNOCCUPIED".equals(e.getTag())) {
                            preferences().putString(KEY_CODE, code);
                            state = AuthState.SIGN_UP;
                            callback.onResult(AuthState.SIGN_UP);
                            return;
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

    public void resetModule() {
        // Clearing authentication
        state = AuthState.AUTH_START;
        myUid = 0;
        preferences().putBool(KEY_AUTH, false);
        preferences().putInt(KEY_AUTH_UID, 0);
        preferences().putLong(KEY_PHONE, 0);
        preferences().putString(KEY_SMS_HASH, null);
        preferences().putInt(KEY_SMS_CODE, 0);
    }

    public String getEmail() {
        return preferences().getString(KEY_EMAIL);
    }


}