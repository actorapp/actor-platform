package im.actor.messenger.app.fragment.auth;

import android.app.AlertDialog;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.model.modules.Auth;

import static im.actor.messenger.app.Core.messenger;

public class SignEmailFragment extends BaseAuthFragment {

    private EditText emailEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_email, container, false);

        ((TextView) v.findViewById(R.id.button_continue_text)).setTypeface(Fonts.medium());

        keyboardHelper = new KeyboardHelper(getActivity());

        initView(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO track email auth open
        //messenger().trackAuthPhoneOpen();

        setTitle(R.string.auth_email_title);

        focusEmail();

        keyboardHelper.setImeVisibility(emailEditText, true);
    }

    private void initView(View v) {

        emailEditText = (EditText) v.findViewById(R.id.tv_email);
        emailEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        String email = messenger().getPreferences().getString(Auth.KEY_EMAIL);
        if(email!=null && !email.isEmpty()){
            emailEditText.setText(email);
        }
        emailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_GO) {
                    requestCode();
                    return true;
                }
                return false;
            }
        });
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //TODO trackAuthEmailType
                //messenger().trackAuthPhoneType(emailEditText.getText().toString());
            }
        });

        onClick(v, R.id.button_continue, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode();
            }
        });
    }

    private void requestCode() {
        final String ACTION = "Request code email";

        messenger().trackCodeRequest();

        if (emailEditText.getText().toString().trim().length() == 0) {
            String message = getString(R.string.auth_error_empty_email);
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_email)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            messenger().trackActionError(ACTION, "LOCAL_EMPTY_EMAIL", message);
            return;
        }

        String rawEmail = emailEditText.getText().toString();

        if (rawEmail.length() == 0) {
            String message = getString(R.string.auth_error_empty_email);
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_email)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            messenger().trackActionError(ACTION, "LOCAL_INCORRECT_EMAIL", message);
            return;
        }
        executeAuth(messenger().requestStartEmailAuth(rawEmail), ACTION);
    }

    private void focusEmail() {
        focus(emailEditText);
    }

}
