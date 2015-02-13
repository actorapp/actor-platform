package im.actor.messenger.app.fragment.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import im.actor.api.ApiRequestException;
import im.actor.messenger.R;
import im.actor.messenger.app.activity.TakePhotoActivity;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.auth.AuthModel;

import static im.actor.messenger.core.Core.auth;

public class SignUpFragment extends BaseAuthFragment {

    private static final int REQUEST_AVATAR = 1;

    private String avatarPath;

    private View progress;
    private EditText firstNameEditText;
    private KeyboardHelper keyboardHelper;
    private AvatarView avatarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        keyboardHelper = new KeyboardHelper(getActivity());
        progress = v.findViewById(R.id.progressContainer);
        progress.setVisibility(View.GONE);
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
                auth().sendSignUp(firstNameEditText.getText().toString().trim(), avatarPath);
            }
        });

        return v;
    }

    @Override
    protected void onState(int stateId, Throwable t, boolean isAnimated) {
        if (stateId == AuthModel.AuthProcessState.STATE_SIGN_UP) {
            goneView(progress, isAnimated);
            keyboardHelper.setImeVisibility(firstNameEditText, true);
            hideError();
            focus(firstNameEditText);
        } else if (stateId == AuthModel.AuthProcessState.STATE_SIGNING) {
            showView(progress, isAnimated);
            keyboardHelper.setImeVisibility(firstNameEditText, false);
            hideError();
        } else if (stateId == AuthModel.AuthProcessState.STATE_SIGNING_ERROR) {
            showView(progress, isAnimated);
            keyboardHelper.setImeVisibility(firstNameEditText, false);
            if (t instanceof ApiRequestException) {
                if ("PHONE_CODE_EXPIRED".equals(((ApiRequestException) t).getErrorTag())) {
                    showError(-1, t);
                } else {
                    showError(stateId, t);
                }
            } else {
                showError(stateId, t);
            }
        } else {
            rawNavigate(stateId, isAnimated);
        }
    }

    @Override
    protected void onErrorRepeat(int stateId) {
        if (stateId == AuthModel.AuthProcessState.STATE_SIGNING_ERROR) {
            auth().tryAgainSignup();
        }
    }

    @Override
    protected void onErrorCancel(int stateId) {
        if (stateId == -1) {
            auth().resetAuth();
        } else if (stateId == AuthModel.AuthProcessState.STATE_SIGNING_ERROR) {
            auth().resetSignup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.auth_profile_title);
        focus(firstNameEditText);
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