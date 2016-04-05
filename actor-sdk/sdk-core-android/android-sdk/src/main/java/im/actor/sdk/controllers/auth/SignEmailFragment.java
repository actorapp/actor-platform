package im.actor.sdk.controllers.auth;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.AuthState;
import im.actor.runtime.actors.ActorRef;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignEmailFragment extends BaseAuthFragment {

    private EditText emailEditText;
    private KeyboardHelper keyboardHelper;
    private String rawEmail;
    private ImageView logo;
    ActorRef logoActor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_email, container, false);

        TextView buttonCotinueText = (TextView) v.findViewById(R.id.button_continue_text);
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        buttonCotinueText.setBackgroundDrawable(states);
        buttonCotinueText.setTypeface(Fonts.medium());
        buttonCotinueText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());

        keyboardHelper = new KeyboardHelper(getActivity());

        initView(v);

//        Get domain logo

//        logoActor = ActorSystem.system().actorOf(Props.create(LogoActor.class, new ActorCreator<LogoActor>() {
//            @Override
//            public LogoActor create() {
//                return new LogoActor();
//            }
//        }), "actor/logo_actor");
//
//        logoActor.send(new LogoActor.AddCallback(new LogoActor.LogoCallBack() {
//            @Override
//            public void onDownloaded(final Drawable logoDrawable) {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (logoDrawable != null) {
//                            logo.setImageDrawable(logoDrawable);
//                            logo.measure(0, 0);
//                            expand(logo, logo.getMeasuredHeight());
//                        } else {
//                            expand(logo, 0);
//                        }
//                    }
//                });
//            }
//        }));

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

        ((TextView) v.findViewById(im.actor.sdk.R.id.button_why)).setTypeface(Fonts.medium());
        ((TextView) v.findViewById(im.actor.sdk.R.id.button_why)).setTextColor(ActorSDK.sharedActor().style.getMainColor());
        v.findViewById(im.actor.sdk.R.id.button_why).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.auth_email_why_description)
                        .setPositiveButton(im.actor.sdk.R.string.auth_phone_why_done, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });

        ((TextView) v.findViewById(R.id.email_login_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        emailEditText = (EditText) v.findViewById(R.id.tv_email);
        emailEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        emailEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        String email = messenger().getAuthEmail();
        if (email != null && !email.isEmpty()) {
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
//                logoActor.send(new LogoActor.OnInput(emailEditText.getText().toString()));
                //TODO trackAuthEmailType
                //messenger().trackAuthPhoneType(emailEditText.getText().toString());
            }
        });

        Button switchToPhone = (Button) v.findViewById(R.id.button_switch_to_phone);
        switchToPhone.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        onClick(switchToPhone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToPhone();
            }
        });
        if ((ActorSDK.sharedActor().getAuthType() & AuthActivity.AUTH_TYPE_PHONE) == AuthActivity.AUTH_TYPE_PHONE) {
            switchToPhone.setVisibility(View.VISIBLE);
        } else {
            switchToPhone.setVisibility(View.GONE);
        }


        onClick(v, R.id.button_continue, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode();
            }
        });

    }

    private void requestCode() {
        final String ACTION = "Request code email";


        if (emailEditText.getText().toString().trim().length() == 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_email)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }

        rawEmail = emailEditText.getText().toString();

        startEmailAuth(rawEmail);
    }

    private void focusEmail() {
        focus(emailEditText);
    }

}
