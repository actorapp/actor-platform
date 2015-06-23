package im.actor.messenger.app.fragment.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import im.actor.messenger.R;
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

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AuthState state;
    private String authType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authType = getIntent().getStringExtra("auth_type");
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
                if (authType != null && authType.equals("auth_type_email")) {
                    showFragment(new SignEmailFragment(), false, false);
                } else {
                    showFragment(new SignPhoneFragment(), false, false);
                }
                break;
            case CODE_VALIDATION:
                showFragment(new SignInFragment(), false, false);
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



