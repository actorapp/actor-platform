package im.actor.sdk.controllers.auth;

import android.app.AlertDialog;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.core.AuthState;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignInFragment extends BaseAuthFragment {

    private EditText signIdEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);

        TextView buttonCotinueText = (TextView) v.findViewById(R.id.button_continue_text);
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        buttonCotinueText.setBackgroundDrawable(states);
        buttonCotinueText.setTypeface(Fonts.medium());
        buttonCotinueText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());

        keyboardHelper = new KeyboardHelper(getActivity());

        v.findViewById(R.id.divider).setBackgroundColor(style.getDividerColor());

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

        //TODO track sign_in auth open
        //messenger().trackAuthPhoneOpen();

        setTitle(R.string.sign_in_title);

        focussignId();

        keyboardHelper.setImeVisibility(signIdEditText, true);
    }

    private void initView(View v) {

        ActorStyle style = ActorSDK.sharedActor().style;
        TextView hint = (TextView) v.findViewById(R.id.sign_in_login_hint);
        hint.setTextColor(style.getTextSecondaryColor());
        signIdEditText = (EditText) v.findViewById(R.id.tv_sign_in);
        signIdEditText.setTextColor(style.getTextPrimaryColor());
        signIdEditText.setHighlightColor(style.getMainColor());

        signIdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_GO) {
                    requestCode();
                    return true;
                }
                return false;
            }
        });

        int availableAuthType = ActorSDK.sharedActor().getAuthType();
        String savedAuthId = messenger().getPreferences().getString("sign_in_auth_id");
        signIdEditText.setText(savedAuthId);
        boolean needSuggested = savedAuthId == null || savedAuthId.isEmpty();
        if (((availableAuthType & AuthActivity.AUTH_TYPE_PHONE) == AuthActivity.AUTH_TYPE_PHONE) && ((availableAuthType & AuthActivity.AUTH_TYPE_EMAIL) == AuthActivity.AUTH_TYPE_EMAIL)) {
            //both hints set phone + email by default
            if (needSuggested) {
                setSuggestedEmail(signIdEditText);
            }
        } else if ((availableAuthType & AuthActivity.AUTH_TYPE_PHONE) == AuthActivity.AUTH_TYPE_PHONE) {
            hint.setText(getString(R.string.sign_in_hint_phone_only));
            signIdEditText.setHint(getString(R.string.sign_in_edit_text_hint_phone_only));
            signIdEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        } else if ((availableAuthType & AuthActivity.AUTH_TYPE_EMAIL) == AuthActivity.AUTH_TYPE_EMAIL) {
            hint.setText(getString(R.string.sign_in_hint_email_only));
            signIdEditText.setHint(getString(R.string.sign_in_edit_text_hint_email_only));
            if (needSuggested) {
                setSuggestedEmail(signIdEditText);
            }
        }


        Button singUp = (Button) v.findViewById(R.id.button_sign_up);
        singUp.setTextColor(style.getTextSecondaryColor());
        onClick(singUp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUp();
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
        String message = getString(R.string.auth_error_wrong_auth_id);
        if (signIdEditText.getText().toString().trim().length() == 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }

        String rawId = signIdEditText.getText().toString();

        if (rawId.contains("@")) {
            startEmailAuth(rawId);
        } else {
            try {
                startPhoneAuth(Long.parseLong(rawId.replace("+", "")));
            } catch (Exception e) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(message)
                        .setPositiveButton(R.string.dialog_ok, null)
                        .show();
                return;
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.sign_in, menu);
    }


    private void focussignId() {
        focus(signIdEditText);
    }

}
