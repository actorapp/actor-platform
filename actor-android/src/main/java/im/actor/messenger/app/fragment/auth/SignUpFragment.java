package im.actor.messenger.app.fragment.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.KeyboardHelper;

import static im.actor.messenger.core.Core.messenger;

public class SignUpFragment extends BaseAuthFragment {

    private static final int REQUEST_AVATAR = 1;

    private String avatarPath;

    private EditText firstNameEditText;
    private KeyboardHelper keyboardHelper;
    private AvatarView avatarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        keyboardHelper = new KeyboardHelper(getActivity());
        avatarView = (AvatarView) v.findViewById(R.id.avatar);
        avatarView.setEmptyDrawable(new AvatarDrawable("?", 0, 0, getActivity()));
        if (avatarPath != null) {
            avatarView.bindUploading(avatarPath);
        }
        ((TextView) v.findViewById(R.id.button_confirm_sms_code_text)).setTypeface(Fonts.medium());

        firstNameEditText = (EditText) v.findViewById(R.id.et_first_name_enter);
        final View sendConfirmCodeButton = v.findViewById(R.id.button_confirm_sms_code);

        v.findViewById(R.id.pickAvatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Intents.pickAvatar(avatarPath != null, getActivity()), REQUEST_AVATAR);
            }
        });

        sendConfirmCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().signUp(firstNameEditText.getText().toString().trim(), avatarPath, false);
            }
        });

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
        if (requestCode == REQUEST_AVATAR && resultCode == Activity.RESULT_OK) {
            int res = data.getIntExtra(Intents.EXTRA_RESULT, Intents.RESULT_IMAGE);
            if (res == Intents.RESULT_IMAGE) {
                avatarPath = data.getStringExtra(Intents.EXTRA_IMAGE);
                avatarView.bindUploading(avatarPath);
            } else if (res == Intents.RESULT_DELETE) {
                avatarPath = null;
                avatarView.unbind();
            }
        }
    }
}