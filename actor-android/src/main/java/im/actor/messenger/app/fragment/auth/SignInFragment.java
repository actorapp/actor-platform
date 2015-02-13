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

import im.actor.api.ApiRequestException;
import im.actor.messenger.R;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.KeyboardHelper;

import static im.actor.messenger.core.Core.auth;
import static im.actor.messenger.core.auth.AuthModel.AuthProcessState.*;

public class SignInFragment extends BaseAuthFragment {

    private EditText smsCodeEnterEditText;
    private View progress;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        keyboardHelper = new KeyboardHelper(getActivity());
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        progress = v.findViewById(R.id.progressContainer);
        progress.setVisibility(View.GONE);

        ((TextView) v.findViewById(R.id.button_confirm_sms_code_text)).setTypeface(Fonts.medium());
        ((TextView) v.findViewById(R.id.button_edit_phone)).setTypeface(Fonts.medium());

        String phoneNumber = "+" + auth().getAuthProcessState().getPhoneNumber();
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
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.auth_code_change)
                        .setPositiveButton(R.string.auth_code_change_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth().resetAuth();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
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
            auth().sendCode(code);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_code_title);
    }

    @Override
    protected void onState(int stateId, Throwable t, boolean isAnimated) {
        if (stateId == STATE_CODE_SENDING) {
            progress.setVisibility(View.VISIBLE);
            keyboardHelper.setImeVisibility(smsCodeEnterEditText, false);
            hideError();
        } else if (stateId == STATE_CODE_SEND_ERROR) {
            progress.setVisibility(View.VISIBLE);
            keyboardHelper.setImeVisibility(smsCodeEnterEditText, false);
            if (t instanceof ApiRequestException) {
                if ("PHONE_CODE_EXPIRED".equals(((ApiRequestException) t).getErrorTag())) {
                    showError(-1, t);
                }
            }
            showError(stateId, t);
        } else if (stateId == STATE_REQUESTED_SMS) {
            progress.setVisibility(View.GONE);
            smsCodeEnterEditText.requestFocus();
            keyboardHelper.setImeVisibility(smsCodeEnterEditText, true);
            hideError();
        } else {
            hideError();
            rawNavigate(stateId, isAnimated);
        }
    }

    @Override
    protected void onErrorRepeat(int stateId) {
        if (stateId == STATE_CODE_SEND_ERROR) {
            auth().tryAgainCodeSend();
        }
    }

    @Override
    protected void onErrorCancel(int stateId) {
        if (stateId == STATE_CODE_SEND_ERROR) {
            auth().resetCodeSend();
        } else if (stateId == -1) {
            auth().resetAuth();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        keyboardHelper.setImeVisibility(smsCodeEnterEditText, false);
    }
}
