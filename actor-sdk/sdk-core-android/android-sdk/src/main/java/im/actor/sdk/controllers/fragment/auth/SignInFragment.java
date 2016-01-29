package im.actor.sdk.controllers.fragment.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignInFragment extends BaseAuthFragment {

    public static final String AUTH_TYPE_EMAIL = "auth_type_email";
    public static final String AUTH_TYPE_PHONE = "auth_type_phone";
    public static final String AUTH_TYPE_CUSTOM = "auth_type_custom";
    String authType;
    private EditText codeEnterEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        authType = getArguments().getString("authType");
        keyboardHelper = new KeyboardHelper(getActivity());
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);
        v.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        TextView buttonConfirm = (TextView) v.findViewById(R.id.button_confirm_sms_code_text);
        buttonConfirm.setTypeface(Fonts.medium());
        buttonConfirm.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        buttonConfirm.setBackgroundDrawable(states);
        ((TextView) v.findViewById(R.id.button_edit_phone)).setTypeface(Fonts.medium());
        ((TextView) v.findViewById(R.id.button_edit_phone)).setTextColor(ActorSDK.sharedActor().style.getMainColor());

        TextView sendHint = (TextView) v.findViewById(R.id.sendHint);
        sendHint.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        if(authType.equals(AUTH_TYPE_PHONE)){
            String phoneNumber = "+" + messenger().getAuthPhone();
            try {
                Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
                phoneNumber = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }

            sendHint.setText(
                    Html.fromHtml(getString(R.string.auth_code_phone_hint).replace("{0}", "<b>" + phoneNumber + "</b>"))
            );
        } else if (authType.equals(AUTH_TYPE_EMAIL)) {
            String email = messenger().getAuthEmail();
            sendHint.setText(
                    Html.fromHtml(getString(R.string.auth_code_email_hint).replace("{0}", "<b>" + email + "</b>"))
            );
        } else {
            String authId = getArguments().getString("authId");
            sendHint.setText(
                    Html.fromHtml(getArguments().getString("authHint"))
            );
        }

        codeEnterEditText = (EditText) v.findViewById(R.id.et_sms_code_enter);
        codeEnterEditText.addTextChangedListener(new TextWatcher() {
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
        codeEnterEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        Button editAuth = (Button) v.findViewById(R.id.button_edit_phone);
        if(authType.equals(AUTH_TYPE_EMAIL)){
            editAuth.setText(getString(R.string.auth_code_wrong_email));
        }
        onClick(v, R.id.button_edit_phone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(authType.equals(AUTH_TYPE_EMAIL)?R.string.auth_code_email_change:R.string.auth_code_phone_change)
                        .setPositiveButton(R.string.auth_code_change_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                messenger().resetAuth();
                                updateState();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });

        return v;
    }

    private void sendCode() {
        String text = codeEnterEditText.getText().toString().trim();
        if (text.length() > 0) {
            executeAuth(messenger().validateCode(text), "Send Code");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_code_title);
        keyboardHelper.setImeVisibility(codeEnterEditText, true);
        focus(codeEnterEditText);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
