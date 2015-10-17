package im.actor.messenger.app.fragment.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.core.AuthState;

/**
 * Created by korka on 03.07.15.
 */
public class ChooseAuthTypeFragment extends BaseAuthFragment {
    private int signType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        signType = getArguments().getInt(AuthActivity.SIGN_TYPE_KEY);
        View v = inflater.inflate(R.layout.fragment_choose_auth_type, container, false);

        String emailButtonText = getString(signType == AuthActivity.SIGN_TYPE_IN ? R.string.choose_auth_type_sign_in : R.string.choose_auth_type_sign_up).concat(" ").concat(getString(R.string.choose_auth_type_using_email));
        ((TextView) v.findViewById(R.id.button_continue_email_text)).setText(emailButtonText);

        String phoneButtonText = getString(signType == AuthActivity.SIGN_TYPE_IN ? R.string.choose_auth_type_sign_in : R.string.choose_auth_type_sign_up).concat(" ").concat(getString(R.string.choose_auth_type_using_tel));
        ((TextView) v.findViewById(R.id.button_continue_phone_text)).setText(phoneButtonText);

        ((FrameLayout) v.findViewById(R.id.button_continue_phone)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhoneAuth();
            }
        });


        ((FrameLayout) v.findViewById(R.id.button_continue_email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmailAuth();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.choose_auth_type_title);
    }
}
