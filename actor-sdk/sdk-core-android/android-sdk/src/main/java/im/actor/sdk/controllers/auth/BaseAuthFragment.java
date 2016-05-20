package im.actor.sdk.controllers.auth;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import im.actor.core.entity.AuthRes;
import im.actor.core.entity.Sex;
import im.actor.runtime.promise.Promise;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.view.BaseUrlSpan;
import im.actor.sdk.view.CustomClicableSpan;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public abstract class BaseAuthFragment extends BaseFragment {

    private static final int PERMISSIONS_REQUEST_ACCOUNT = 1;
    public static final boolean USE_SUGGESTED_EMAIL = false;
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
        messenger().getPreferences().putString("sign_in_auth_id", Long.toString(phone));
        ((AuthActivity) getActivity()).startPhoneAuth(messenger().doStartPhoneAuth(phone), phone);
    }

    protected void startEmailAuth(String email) {
        messenger().getPreferences().putString("sign_in_auth_id", email);
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

    protected void startAuth(String name) {
        ((AuthActivity) getActivity()).startAuth(name);
    }

    protected void switchToEmail() {
        ((AuthActivity) getActivity()).switchToEmailAuth();
    }

    protected void switchToPhone() {
        ((AuthActivity) getActivity()).switchToPhoneAuth();
    }

    protected void setSuggestedEmail(EditText et) {
        if (USE_SUGGESTED_EMAIL) {
            edittextToFill = et;
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSIONS_REQUEST_ACCOUNT);

            } else {
                et.setText(getSuggestedEmailChecked());
            }
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

    protected void setTosAndPrivacy(TextView tv) {
        ActorSDK actorSDK = ActorSDK.sharedActor();

        String tosUrl = actorSDK.getTosUrl();
        String tosText = actorSDK.getTosText();
        boolean tosUrlAvailable = tosUrl != null && !tosUrl.isEmpty();
        boolean tosTextAvailable = tosText != null && !tosText.isEmpty();
        boolean tosAvailable = tosUrlAvailable || tosTextAvailable;

        String privacyUrl = actorSDK.getPrivacyUrl();
        String privacyText = actorSDK.getPrivacyText();
        boolean privacyUrlAvailable = privacyUrl != null && !privacyUrl.isEmpty();
        boolean privacyTextAvailable = privacyText != null && !privacyText.isEmpty();
        boolean ppAvailable = privacyUrlAvailable || privacyTextAvailable;

        boolean tosOrPrivacyAvailable = tosAvailable || ppAvailable;

        if (!tosOrPrivacyAvailable) {
            tv.setVisibility(View.GONE);
            return;
        }

        String text;
        SpannableStringBuilder builder;
        if (tosAvailable && ppAvailable) {
            text = getString(R.string.auth_tos_privacy);
            builder = new SpannableStringBuilder(text);

            findAndHilightTos(builder, text, tosUrlAvailable);
            findAndHilightPrivacy(builder, text, privacyUrlAvailable);
        } else if (tosAvailable) {
            text = getString(R.string.auth_tos);
            builder = new SpannableStringBuilder(text);
            findAndHilightTos(builder, text, tosUrlAvailable);
        } else {
            text = getString(R.string.auth_privacy);
            builder = new SpannableStringBuilder(text);

            tv.setText(getString(R.string.auth_privacy));
            findAndHilightPrivacy(builder, text, privacyUrlAvailable);
        }
        builder.append(" ".concat(getString(R.string.auth_find_by_diclamer)));
        tv.setText(builder);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void findAndHilightTos(SpannableStringBuilder builder, String text, boolean urlAvailable) {
        String tosIndex = getString(R.string.auth_tos_index);
        int index = text.indexOf(tosIndex);
        ClickableSpan span;
        if (urlAvailable) {
            span = new BaseUrlSpan(ActorSDK.sharedActor().getTosUrl(), false);
        } else {
            span = new CustomClicableSpan(new CustomClicableSpan.SpanClickListener() {
                @Override
                public void onClick() {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.auth_tos_index)
                            .setMessage(ActorSDK.sharedActor().getTosText())
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }
        builder.setSpan(span, index, index + tosIndex.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void findAndHilightPrivacy(SpannableStringBuilder builder, String text, boolean urlAvailable) {
        String ppIndex = getString(R.string.auth_privacy_index);
        int index = text.indexOf(ppIndex);
        ClickableSpan span;
        if (urlAvailable) {
            span = new BaseUrlSpan(ActorSDK.sharedActor().getPrivacyUrl(), false);
        } else {
            span = new CustomClicableSpan(new CustomClicableSpan.SpanClickListener() {
                @Override
                public void onClick() {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.auth_privacy_index)
                            .setMessage(ActorSDK.sharedActor().getPrivacyText())
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }
        builder.setSpan(span, index, index + ppIndex.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.sign_in) {
            startSignIn();
            return true;
        } else if (i == R.id.sign_up) {
            startSignUp();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
