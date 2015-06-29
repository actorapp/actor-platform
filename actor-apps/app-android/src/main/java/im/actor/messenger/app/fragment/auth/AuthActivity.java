package im.actor.messenger.app.fragment.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.model.AuthState;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;
import im.actor.model.network.RpcTimeoutException;

import static im.actor.messenger.app.Core.messenger;

public class AuthActivity extends BaseFragmentActivity {

    private static final int OAUTH_DIALOG = 1;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AuthState state;
    public static final String AUTH_TYPE_KEY = "auth_type";
    public static final int AUTH_TYPE_PHONE = 1;
    public static final int AUTH_TYPE_EMAIL = 2;
    private int authType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authType = getIntent().getIntExtra(AUTH_TYPE_KEY, AUTH_TYPE_PHONE);
        if (savedInstanceState == null) {
            updateState();
        }
    }

    @Override
    public void onBackPressed() {
        messenger().trackBackPressed();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            messenger().trackUpPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messenger().getAuthState() == AuthState.LOGGED_IN) {
            finish();
        }
    }

    public void updateState() {
        updateState(messenger().getAuthState());
    }

    private void updateState(AuthState state) {
        if (this.state != null && this.state == state) {
            return;
        }
        this.state = state;

        switch (state) {
            case AUTH_START:
                if (authType == AUTH_TYPE_EMAIL) {
                    showFragment(new SignEmailFragment(), false, false);
                } else if (authType == AUTH_TYPE_PHONE) {
                    showFragment(new SignPhoneFragment(), false, false);
                }
                break;
            case CODE_VALIDATION_PHONE:
            case CODE_VALIDATION_EMAIL:
                if ((state == AuthState.CODE_VALIDATION_EMAIL && authType == AUTH_TYPE_PHONE) || (state == AuthState.CODE_VALIDATION_PHONE && authType == AUTH_TYPE_EMAIL)) {
                    updateState(AuthState.AUTH_START);
                    break;
                }
                Fragment signInFragment = new SignInFragment();
                Bundle args = new Bundle();
                args.putString("authType", state == AuthState.CODE_VALIDATION_EMAIL ? SignInFragment.AUTH_TYPE_EMAIL : SignInFragment.AUTH_TYPE_PHONE);
                signInFragment.setArguments(args);
                showFragment(signInFragment, false, false);
                break;
            case GET_OAUTH_PARAMS:
                executeAuth(messenger().requestGetOAuthParams(), "get_oauth_params");
                break;
            case COMPLETE_OAUTH:
                if (authType == AUTH_TYPE_PHONE) {
                    updateState(AuthState.AUTH_START);
                    break;
                }

                showFragment(new SignEmailFragment(), false, false);

                startActivityForResult(new Intent(this, OAuthDialogActivity.class), OAUTH_DIALOG);
                break;
            case SIGN_UP:
                showFragment(new SignUpFragment(), false, false);
                break;
            case LOGGED_IN:
                messenger().trackAuthSuccess();
                finish();
                startActivity(new Intent(this, MainActivity.class));
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
                dismissProgress();
                messenger().trackActionSuccess(action);
                updateState(res);
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

                messenger().trackActionError(action, tag, message);

                if (canTryAgain) {
                    new AlertDialog.Builder(AuthActivity.this)
                            .setMessage(message)
                            .setPositiveButton(R.string.dialog_try_again, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    messenger().trackActionTryAgain(action);
                                    dismissAlert();
                                    executeAuth(command, action);
                                }
                            })
                            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    messenger().trackActionCancel(action);
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
                                    messenger().trackActionCancel(action);
                                    dismissAlert();
                                    updateState(messenger().getAuthState());
                                }
                            })
                            .setCancelable(false)
                            .show()
                            .setCanceledOnTouchOutside(false);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OAUTH_DIALOG:
                if (resultCode == RESULT_OK && data != null) {
                    executeAuth(messenger().requestCompleteOAuth(data.getStringExtra("code")), "Sign in");
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        dismissProgress();
        dismissAlert();
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void dismissAlert() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}



