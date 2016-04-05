package im.actor.sdk.controllers.auth;

import android.widget.EditText;

import im.actor.core.AuthState;
import im.actor.core.entity.AuthCodeRes;
import im.actor.core.entity.AuthRes;
import im.actor.core.entity.AuthStartRes;
import im.actor.core.entity.Sex;
import im.actor.core.viewmodel.Command;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public abstract class BaseAuthFragment extends BaseFragment {

    public BaseAuthFragment() {

    }

    protected void setTitle(int resId) {
        ((BaseFragmentActivity) getActivity()).getSupportActionBar().setTitle(resId);
    }

    protected void setTitle(String title) {
        ((BaseFragmentActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    protected void startSignIn() {
        ((AuthActivity) getActivity()).startSignIn();
    }

    protected void startSignUp() {
        ((AuthActivity) getActivity()).startSignUp();
    }

    protected void startPhoneAuth(long phone) {
        ((AuthActivity) getActivity()).startPhoneAuth(messenger().doStartPhoneAuth(phone), phone);
    }

    protected void startEmailAuth(String email) {
        ((AuthActivity) getActivity()).startEmailAuth(messenger().doStartEmailAuth(email), email);
    }

    protected void validateCode(String code) {
        AuthActivity activity = (AuthActivity) getActivity();
        activity.validateCode(messenger().doValidateCode(code, activity.getTransactionHash()), code);
    }

    protected void signUp(String name, Sex sex) {
        AuthActivity activity = (AuthActivity) getActivity();
        Promise<AuthRes> promise = messenger().doSignup(name, sex, activity.getTransactionHash());
        ((AuthActivity) getActivity()).signUp(promise, name, sex);
    }

    protected void switchToEmail() {
        ((AuthActivity) getActivity()).switchToEmailAuth();
    }

    protected void switchToPhone() {
        ((AuthActivity) getActivity()).switchToPhoneAuth();
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
