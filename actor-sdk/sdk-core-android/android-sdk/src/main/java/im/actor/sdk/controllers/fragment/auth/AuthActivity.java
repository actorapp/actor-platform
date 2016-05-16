package im.actor.sdk.controllers.fragment.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.squareup.okhttp.internal.http.HttpTransport;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.HashMap;

import im.actor.core.AuthState;
import im.actor.core.api.ApiSex;
import im.actor.core.api.rpc.RequestSignUp;
import im.actor.core.entity.Sex;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.network.RpcTimeoutException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.*;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.intents.WebServiceUtil;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AuthActivity extends BaseFragmentActivity {

    public static final String AUTH_TYPE_KEY = "auth_type";
    public static final String SIGN_TYPE_KEY = "sign_type";
    public static final int AUTH_TYPE_PHONE = 1;
    public static final int AUTH_TYPE_EMAIL = 2;
    public static final int AUTH_TYPE_CUSTOM = 3;
    public static final int AUTH_TYPE_USERNAME = 4;
    public static final int SIGN_TYPE_IN = 3;
    public static final int SIGN_TYPE_UP = 4;
    private static final int OAUTH_DIALOG = 1;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AuthState state;
    private int authType = AUTH_TYPE_PHONE;
    private int signType;
    private BaseAuthFragment signFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signType = getIntent().getIntExtra(SIGN_TYPE_KEY, SIGN_TYPE_IN);
        if (savedInstanceState == null) {
            updateState();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messenger().getAuthState() == AuthState.LOGGED_IN) {
            finish();
        }

        updateState();
    }

    public void updateState() {
        updateState(messenger().getAuthState());
    }

    private void updateState(AuthState state) {
        if (this.state != null && this.state == state) {
            return;
        }

        // if we show the next fragment when app is in background and not visible , app crashes!
        // e.g when the GSM data is off and after trying to send code we go to settings to turn on, app is going invisible and ...
        if (state != AuthState.LOGGED_IN && getIsResumed() == false)
            return;

        this.state = state;

        switch (state) {
            case AUTH_START:
                signFragment = ActorSDK.sharedActor().getDelegatedFragment(ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), new SignUserNameFragment(), BaseAuthFragment.class);
//                signFragment = ActorSDK.sharedActor().getDelegatedFragment(ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), new SignPhoneFragment(), BaseAuthFragment.class);

                if (signFragment instanceof SignPhoneFragment) {
                    authType = AUTH_TYPE_PHONE;
                } else if (signFragment instanceof SignUserNameFragment) {
                    authType = AUTH_TYPE_USERNAME;
                } else {
                    authType = AUTH_TYPE_CUSTOM;
                }
                showFragment(signFragment, false, false);
                break;
            case AUTH_PHONE:
                signFragment = new SignPhoneFragment();
                showFragment(signFragment, false, false);
                authType = AUTH_TYPE_PHONE;
                break;
            case CODE_VALIDATION_PHONE:
                Fragment signInFragment = new SignInFragment();
                Bundle args = new Bundle();
                args.putString("authType", SignInFragment.AUTH_TYPE_PHONE);
                signInFragment.setArguments(args);
                showFragment(signInFragment, false, false);
                break;
            case PASSWORD_VALIDATION:
                Fragment signInPasswordFragment = new SignInPasswordFragment();
                Bundle args2 = new Bundle();
                args2.putString("authType", SignInPasswordFragment.AUTH_TYPE_USERNAME);
                signInPasswordFragment.setArguments(args2);
                showFragment(signInPasswordFragment, false, false);
                break;
            case SIGN_UP:
//                showFragment(new SignUpFragment(), false, false);
                executeAuth(new Command<AuthState>() {
                    @Override
                    public void start(final CommandCallback<AuthState> callback) {
                        HashMap<String, String> par = new HashMap<String, String>();
                        par.put("oaUserName", messenger().getAuthNickName());
                        par.put("password", messenger().getAuthPassword());
                        WebServiceUtil.webServiceRun(messenger().getAuthWebServiceIp(), par, "validatePassword", new PasswordHandler(callback));
                    }
                }, "webValidatePassword");
                break;
            case LOGGED_IN:
                finish();
                startActivity(new Intent(this, ActorMainActivity.class));
                break;
            case BATCHSIGNUP:
//                finish();
//                startActivity(new Intent(this, ActorMainActivity.class));
                executeAuth(new Command<AuthState>() {
                    @Override
                    public void start(final CommandCallback<AuthState> callback) {
                        HashMap<String, String> par = new HashMap<String, String>();
                        par.put("oaUserName", messenger().getAuthNickName());
                        WebServiceUtil.webServiceRun(messenger().getAuthWebServiceIp(), par, "syncUser", new SignUpHandeler(callback, messenger().getAuthPassword()));
                    }
                }, "webSyncUser");

                break;
        }
    }

    class PasswordHandler extends Handler {
        CommandCallback<AuthState> callback;

        public PasswordHandler(CommandCallback<AuthState> callback) {
            this.callback = callback;
        }

        public PasswordHandler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String datasource = b.getString("datasource");
            try {
                JSONObject jo = new JSONObject(datasource);
                String result = jo.getString("result").trim();
                if ("false".equals(result)) {
//                    AuthState statePas = AuthState.PASSWORD_VALIDATION;
//                    state = statePas;
                    im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                        @Override
                        public void run() {
                            RpcException e = new RpcException("PASSWORD_INVALID", 400, "密码错误，请重新输入", false, null);
                            callback.onError(e);
                        }
                    });
                } else if ("true".equals(result)) {
                    executeAuth(messenger().signUp(messenger().getAuthZHName(), Sex.UNKNOWN, null, "11111111"), "SignUp");
                }
//
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }
    }

    class SignUpHandeler extends Handler {
        CommandCallback<AuthState> callback;
        String password;

        public SignUpHandeler(CommandCallback<AuthState> callback, String password) {
            this.callback = callback;
            this.password = password;
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
                String result = jo.getString("result").trim();
                if ("false".equals(result)) {
                    im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                        @Override
                        public void run() {
                            RpcException e = new RpcException("UserName init error", 400, "用户初始化出错，可能账号输错", false, null);
                            callback.onError(e);
                        }
                    });
                } else if ("true".equals(result)) {
                    executeAuth(messenger().validatePassword(password), "Send_next_Password");
                }
            } catch (Exception e) {
                e.printStackTrace();
                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        RpcException e = new RpcException("UserName init error", 400, "用户初始化出错，可能账号输错", false, null);
                        callback.onError(e);
                    }
                });
            }
        }
    }

    public void executeAuth(final Command<AuthState> command, final String action) {
        if (!"Send_next_Password".equals(action) && !"webSyncUser".equals(action) && !"Request code".equals(action) && !"webValidatePassword".equals(action) && !"SignUp".equals(action)) {
            dismissProgress();
            progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading...");
            progressDialog.show();
        }
        command.start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(final AuthState res) {
                if (res == AuthState.SIGN_UP || res == AuthState.BATCHSIGNUP ) {
                    updateState(res);
                } else {
                    if (dismissProgress()) {
                        updateState(res);
                    }
                }

            }

            @Override
            public void onError(final Exception e) {
                dismissProgress();
                boolean canTryAgain = false;
                String message = getString(R.string.error_unknown);
                String tag = "UNKNOWN";
                if (e instanceof RpcException) {
                    RpcException re = (RpcException) e;
                    tag = re.getTag();
                    if (re instanceof RpcInternalException) {
                        message = getString(R.string.error_unknown);
                        canTryAgain = true;
                    } else if (re instanceof RpcTimeoutException) {
                        message = getString(R.string.error_connection);
                        canTryAgain = true;
                    } else {
                        if ("PHONE_CODE_EXPIRED".equals(re.getTag())) {
                            message = getString(R.string.auth_error_code_expired);
                            canTryAgain = false;
                        } else if ("PHONE_CODE_INVALID".equals(re.getTag())) {
                            message = getString(R.string.auth_error_code_invalid);
                            canTryAgain = false;
                        } else if ("FAILED_GET_OAUTH2_TOKEN".equals(re.getTag())) {
                            message = getString(R.string.auth_error_failed_get_oauth2_token);
                            canTryAgain = false;
                        } else if ("PASSWORD_INVALID".equals(re.getTag())) {
                            message = getString(R.string.auth_error_password_invalid);
                            canTryAgain = false;
                        }  else {
                            message = re.getMessage();
                            canTryAgain = re.isCanTryAgain();
                        }
                    }
                }

                try {
                    if (canTryAgain) {
                        new AlertDialog.Builder(AuthActivity.this)
                                .setMessage(message)
                                .setPositiveButton(R.string.dialog_try_again, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                        executeAuth(command, action);
                                    }
                                })
                                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                        updateState(messenger().getAuthState());
                                    }
                                }).setCancelable(false)
                                .show()
                                .setCanceledOnTouchOutside(false);
                    } else {
                        new AlertDialog.Builder(AuthActivity.this)
                                .setMessage(message)
                                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissAlert();
                                        updateState(messenger().getAuthState());
                                        if (state == AuthState.SIGN_UP) {
                                            AuthState statePas = AuthState.PASSWORD_VALIDATION;
                                            state = statePas;
                                        }
                                    }
                                })
                                .setCancelable(false)
                                .show()
                                .setCanceledOnTouchOutside(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }


    public void startEmailAuth() {
        updateState(AuthState.AUTH_EMAIL);
    }

    public void startPhoneAuth() {
        updateState(AuthState.AUTH_PHONE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        dismissProgress();
        dismissAlert();
    }

    private boolean dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
            return true;
        }
        return false;
    }

    private void dismissAlert() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

}



