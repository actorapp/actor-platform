package im.actor.sdk.controllers.fragment.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import im.actor.core.AuthState;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.network.RpcTimeoutException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsFragment;
import im.actor.sdk.controllers.fragment.settings.BaseActorSettingsActivity;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AuthActivity extends BaseFragmentActivity {

    public static final String AUTH_TYPE_KEY = "auth_type";
    public static final String SIGN_TYPE_KEY = "sign_type";
    public static final int AUTH_TYPE_PHONE = 1;
    public static final int AUTH_TYPE_EMAIL = 2;
    public static final int AUTH_TYPE_CUSTOM = 3;
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

                signFragment = ActorSDK.sharedActor().getDelegatedFragment(ActorSDK.sharedActor().getDelegate().getAuthStartIntent(), new SignPhoneFragment(), BaseAuthFragment.class);

                if (signFragment instanceof SignPhoneFragment) {
                    authType = AUTH_TYPE_PHONE;
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
            case CODE_VALIDATION_CUSTOM:
            case CODE_VALIDATION_PHONE:

                Fragment signInFragment = new SignInFragment();
                Bundle args = new Bundle();
                args.putString("authType", SignInFragment.AUTH_TYPE_PHONE);
                if (state == AuthState.CODE_VALIDATION_CUSTOM) {
                    args.putString("authType", SignInFragment.AUTH_TYPE_CUSTOM);
                    args.putString("authId", signFragment.getAuthId());
                    args.putString("authHint", signFragment.getHintText());
                }
                signInFragment.setArguments(args);
                showFragment(signInFragment, false, false);
                break;
            case SIGN_UP:
                showFragment(new SignUpFragment(), false, false);
                break;
            case LOGGED_IN:
                finish();
                startActivity(new Intent(this, ActorMainActivity.class));
                break;
        }
    }

    public void executeAuth(final Command<AuthState> command, final String action) {
        dismissProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading...");
        progressDialog.show();
        command.start(new CommandCallback<AuthState>() {
            @Override
            public void onResult(final AuthState res) {
                if(dismissProgress()){
                    updateState(res);
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
                        } else {
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



