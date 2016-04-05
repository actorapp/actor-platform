package im.actor.sdk.controllers.auth;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Pattern;

import im.actor.core.entity.AuthRes;
import im.actor.core.entity.Sex;
import im.actor.runtime.Log;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public abstract class BaseAuthFragment extends BaseFragment {

    private static final int PERMISSIONS_REQUEST_ACCOUNT = 1;
    private EditText edittextToFill;
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

    protected void setSuggestedEmail(EditText et) {
        edittextToFill = et;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                    PERMISSIONS_REQUEST_ACCOUNT);

        } else {
            et.setText(getSuggestedEmailChecked());
        }
    }


    private String getSuggestedEmailChecked() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }

        return null;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCOUNT && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (edittextToFill != null) {
                edittextToFill.setText(getSuggestedEmailChecked());
            }
        }
    }
}
