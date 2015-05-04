package im.actor.messenger.app.fragment.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.messenger.R;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.KeyboardHelper;

import static im.actor.messenger.app.Core.messenger;

public class SignInFragment extends BaseAuthFragment {

    private EditText smsCodeEnterEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        keyboardHelper = new KeyboardHelper(getActivity());
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);

        ((TextView) v.findViewById(R.id.button_confirm_sms_code_text)).setTypeface(Fonts.medium());
        ((TextView) v.findViewById(R.id.button_edit_phone)).setTypeface(Fonts.medium());

        String phoneNumber = "+" + messenger().getAuthPhone();
        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
            phoneNumber = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        ((TextView) v.findViewById(R.id.sendHint)).setText(
                Html.fromHtml(getString(R.string.auth_code_hint).replace("{0}", "<b>" + phoneNumber + "</b>"))
        );

        smsCodeEnterEditText = (EditText) v.findViewById(R.id.et_sms_code_enter);
        smsCodeEnterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    sendCode();
                }
                messenger().trackAuthCodeType(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        smsCodeEnterEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    sendCode();
                    return true;
                }
                return false;
            }
        });

        onClick(v, R.id.button_confirm_sms_code, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode();
            }
        });

        onClick(v, R.id.button_edit_phone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().trackAuthCodeWrongNumber();
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.auth_code_change)
                        .setPositiveButton(R.string.auth_code_change_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messenger().trackAuthCodeWrongNumberChange();
                                messenger().resetAuth();
                                updateState();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messenger().trackAuthCodeWrongNumberCancel();
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });

        return v;
    }

    private void sendCode() {
        String text = smsCodeEnterEditText.getText().toString().trim();
        if (text.length() > 0) {
            int code;
            try {
                code = Integer.parseInt(smsCodeEnterEditText.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            executeAuth(messenger().sendCode(code), "Send Code");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_code_title);
        keyboardHelper.setImeVisibility(smsCodeEnterEditText, true);
        focus(smsCodeEnterEditText);
        messenger().trackAuthCodeOpen();
    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().trackAuthCodeClosed();
    }
}
