package im.actor.messenger.app.fragment.auth;

import android.widget.EditText;

import im.actor.messenger.app.activity.AuthActivity;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.model.AuthState;
import im.actor.model.concurrency.Command;

/**
 * Created by ex3ndr on 31.08.14.
 */
public abstract class BaseAuthFragment extends BaseCompatFragment {

    public BaseAuthFragment() {

    }

    protected void setTitle(int resId) {
        ((BaseBarFragmentActivity) getActivity()).getSupportActionBar().setTitle(resId);
    }

    protected void setTitle(String title) {
        ((BaseBarFragmentActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void execute(Command<AuthState> command) {
        ((AuthActivity) getActivity()).execute(command);
    }

    protected void updateState() {
        ((AuthActivity) getActivity()).updateState();
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
