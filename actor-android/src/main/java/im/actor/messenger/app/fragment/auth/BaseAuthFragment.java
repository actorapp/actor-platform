package im.actor.messenger.app.fragment.auth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import com.droidkit.actors.tasks.AskTimeoutException;
import com.droidkit.engine.event.NotificationCenter;
import com.droidkit.engine.event.NotificationListener;

import im.actor.api.ApiRequestException;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.AuthActivity;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.core.Core;

import static im.actor.messenger.core.auth.AuthModel.AuthProcessState.*;

/**
 * Created by ex3ndr on 31.08.14.
 */
public abstract class BaseAuthFragment extends BaseCompatFragment implements NotificationListener {

    private boolean isAnimationLock = false;

    private Dialog errorDialog;

    @Override
    public void onResume() {
        super.onResume();
        isAnimationLock = true;
        NotificationCenter.getInstance().addListener(Core.AUTH_STATE, this);
        isAnimationLock = false;
    }

    @Override
    public void onNotification(int i, long i2, Object[] objects) {
        if (i == Core.AUTH_STATE) {
            int state = (Integer) objects[0];
            Throwable t = null;
            if (objects.length >= 2) {
                t = (Throwable) objects[1];
            }
            onState(state, t, !isAnimationLock);
        }
    }

    public void showError(final int stateId, Throwable t) {
        hideError();
        boolean canTryAgain = false;
        String message = getString(R.string.error_unknown);
        if (t instanceof AskTimeoutException) {
            message = getString(R.string.error_connection);
            canTryAgain = true;
        } else if (t instanceof ApiRequestException) {
            ApiRequestException e = (ApiRequestException) t;
            if ("PHONE_CODE_EXPIRED".equals(e.getErrorTag())) {
                message = getString(R.string.auth_error_code_expired);
                canTryAgain = false;
            } else if ("PHONE_CODE_INVALID".equals(e.getErrorTag())) {
                message = getString(R.string.auth_error_code_invalid);
                canTryAgain = false;
            } else {
                message = e.getErrorUserMessage();
                canTryAgain = e.isCanTryAgain();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setCancelable(true);
        if (canTryAgain) {
            builder.setPositiveButton(R.string.dialog_try_again, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onErrorRepeat(stateId);
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onErrorCancel(stateId);
                }
            });
        } else {
            builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onErrorCancel(stateId);
                }
            });
        }

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onErrorCancel(stateId);
            }
        });

        errorDialog = builder.show();
        errorDialog.setCanceledOnTouchOutside(true);
        errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                errorDialog = null;
            }
        });
    }

    public void hideError() {
        if (errorDialog != null) {
            errorDialog.dismiss();
            errorDialog = null;
        }
    }

    protected void onState(int stateId, Throwable t, boolean isAnimated) {

    }

    protected void onErrorRepeat(int stateId) {

    }

    protected void onErrorCancel(int stateId) {

    }

    protected void setTitle(int resId) {
        ((BaseBarFragmentActivity) getActivity()).getSupportActionBar().setTitle(resId);
    }

    protected void setTitle(String title) {
        ((BaseBarFragmentActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void rawNavigate(int state, boolean isAnimated) {
        switch (state) {
            case STATE_REQUESTED_SMS:
            case STATE_CODE_SENDING:
            case STATE_CODE_SEND_ERROR:
                ((BaseBarFragmentActivity) getActivity()).showFragment(new SignInFragment(), false, isAnimated);
                break;
            case STATE_SIGNING_ERROR:
            case STATE_SIGN_UP:
            case STATE_SIGNING:
                ((BaseBarFragmentActivity) getActivity()).showFragment(new SignUpFragment(), false, isAnimated);
                break;
            default:
            case STATE_START:
            case STATE_REQUESTING_SMS:
            case STATE_REQUESTING_SMS_ERROR:
                ((BaseBarFragmentActivity) getActivity()).showFragment(new SignPhoneFragment(), false, isAnimated);
                break;
            case STATE_SIGNED:
                FragmentActivity activity = getActivity();
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationCenter.getInstance().removeListener(this);
        hideError();
    }

    protected void focus(final EditText editText) {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                editText.setSelection(editText.getText().length());
            }
        });
    }
}
