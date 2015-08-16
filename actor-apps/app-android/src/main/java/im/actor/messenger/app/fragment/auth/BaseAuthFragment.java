package im.actor.messenger.app.fragment.auth;

import android.widget.EditText;

import im.actor.core.AuthState;
import im.actor.core.viewmodel.Command;
import im.actor.messenger.app.activity.BaseFragmentActivity;
import im.actor.messenger.app.fragment.BaseFragment;

/**
 * Created by ex3ndr on 31.08.14.
 */
public abstract class BaseAuthFragment extends BaseFragment {

    public BaseAuthFragment() {

    }

    protected void setTitle(int resId) {
        ((BaseFragmentActivity) getActivity()).getSupportActionBar().setTitle(resId);
    }

    protected void setTitle(String title) {
        ((BaseFragmentActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void executeAuth(Command<AuthState> command, String action) {
        ((AuthActivity) getActivity()).executeAuth(command, action);
    }

    protected void updateState() {
        ((AuthActivity) getActivity()).updateState();
    }

    protected void startEmailAuth() {
        ((AuthActivity) getActivity()).startEmailAuth();
    }

    protected void startPhoneAuth() {
        ((AuthActivity) getActivity()).startPhoneAuth();
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
