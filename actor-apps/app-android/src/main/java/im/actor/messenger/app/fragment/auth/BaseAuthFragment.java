package im.actor.messenger.app.fragment.auth;

import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.model.AuthState;
import im.actor.model.concurrency.Command;

/**
 * Created by ex3ndr on 31.08.14.
 */
public abstract class BaseAuthFragment extends BaseFragment {

    public BaseAuthFragment() {

    }

    protected void setTitle(int resId) {
        ((TextView) getActivity().findViewById(R.id.toolbar_title)).setText(resId);
    }

    protected void setTitle(String title) {
        ((TextView) getActivity().findViewById(R.id.toolbar_title)).setText(title);
    }

    protected void setSubtitle(int resId) {
        ((TextView) getActivity().findViewById(R.id.toolbar_subtitle)).setText(resId);
    }

    protected void setSubtitle(String title) {
        ((TextView) getActivity().findViewById(R.id.toolbar_subtitle)).setText(title);
    }

    protected void setSubtitle(Spanned title) {
        ((TextView) getActivity().findViewById(R.id.toolbar_subtitle)).setText(title);
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
