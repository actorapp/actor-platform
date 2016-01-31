package im.actor.sdk.controllers.fragment.auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignUpFragment extends BaseAuthFragment {

    private static final int REQUEST_AVATAR = 1;

    private String avatarPath;

    private EditText firstNameEditText;
    private KeyboardHelper keyboardHelper;
    private AvatarView avatarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        v.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        if (savedInstanceState != null) {
            avatarPath = savedInstanceState.getString("avatarPath", null);
        }

        keyboardHelper = new KeyboardHelper(getActivity());
        avatarView = (AvatarView) v.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(96), 24);
        avatarView.getHierarchy().setPlaceholderImage(R.drawable.circle_placeholder);

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

        v.findViewById(R.id.pickAvatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intents.pickAvatar(avatarPath != null, getActivity()), REQUEST_AVATAR);
            }
        });

        sendConfirmCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAuth(messenger().signUp(firstNameEditText.getText().toString().trim(), Sex.UNKNOWN, avatarPath), "SignUp");
            }
        });

        if (avatarPath != null) {
            avatarView.bindRaw(avatarPath);
        }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AVATAR) {
            if (resultCode == Activity.RESULT_OK) {
                int res = data.getIntExtra(Intents.EXTRA_RESULT, Intents.RESULT_IMAGE);
                if (res == Intents.RESULT_IMAGE) {
                    avatarPath = data.getStringExtra(Intents.EXTRA_IMAGE);
                    avatarView.bindRaw(avatarPath);
                } else if (res == Intents.RESULT_DELETE) {
                    avatarPath = null;
                    avatarView.unbind();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (avatarPath != null) {
            outState.putString("avatarPath", avatarPath);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}