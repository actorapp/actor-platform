package im.actor.sdk.controllers.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.core.entity.Sex;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.SelectorFactory;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignUpFragment extends BaseAuthFragment {



    private EditText firstNameEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        v.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        keyboardHelper = new KeyboardHelper(getActivity());
        TextView buttonConfirm = (TextView) v.findViewById(R.id.button_confirm_sms_code_text);
        buttonConfirm.setTypeface(Fonts.medium());
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        buttonConfirm.setBackgroundDrawable(states);
        buttonConfirm.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());

        firstNameEditText = (EditText) v.findViewById(R.id.et_first_name_enter);
        firstNameEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        firstNameEditText.setHintTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        final View sendConfirmCodeButton = v.findViewById(R.id.button_confirm_sms_code);
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ((TextView) v.findViewById(R.id.sign_up_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());


        sendConfirmCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!firstNameEditText.getText().toString().isEmpty()) {
                    startAuth(firstNameEditText.getText().toString().trim());
                }
            }
        });
        v.findViewById(R.id.divider).setBackgroundColor(style.getDividerColor());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_profile_title);
        focus(firstNameEditText);
        keyboardHelper.setImeVisibility(firstNameEditText, true);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.sign_up, menu);
    }
}