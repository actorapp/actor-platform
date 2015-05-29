/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.ArrayList;

import im.actor.model.ApiConfiguration;
import im.actor.model.AuthState;
import im.actor.model.api.rpc.RequestSendAuthCode;
import im.actor.model.api.rpc.RequestSignIn;
import im.actor.model.api.rpc.RequestSignUp;
import im.actor.model.api.rpc.ResponseAuth;
import im.actor.model.api.rpc.ResponseSendAuthCode;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.ContactRecordType;
import im.actor.model.entity.User;
import im.actor.model.log.Log;
import im.actor.model.modules.updates.internal.LoggedIn;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class Auth extends BaseModule {

    private static final String KEY_DEVICE_HASH = "device_hash";

    private static final String KEY_AUTH = "auth_yes";
    private static final String KEY_AUTH_UID = "auth_uid";
    private static final String KEY_PHONE = "auth_phone";
    private static final String KEY_SMS_HASH = "auth_sms_hash";
    private static final String KEY_SMS_CODE = "auth_sms_code";

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
        }));
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
                modules().getAnalytics().trackCodeRequest(phone);

                request(new RequestSendAuthCode(phone, apiConfiguration.getAppId(),
                                apiConfiguration.getAppKey()),
                        new RpcCallback<ResponseSendAuthCode>() {
                            @Override
                            public void onResult(final ResponseSendAuthCode response) {
                                Log.d("AUTH", "OnResult: " + response);
                                Log.d("AUTH", "Callback: " + callback);
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

    public Command<AuthState> signUp(final String firstName, final String avatarPath, final boolean isSilent) {
        return new Command<AuthState>() {
            @Override
            public void start(final CommandCallback<AuthState> callback) {
                request(new RequestSignUp(preferences().getLong(KEY_PHONE, 0),
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

    public void resetAuth() {
        state = AuthState.AUTH_START;
    }

    public long getPhone() {
        return preferences().getLong(KEY_PHONE, 0);
    }
}