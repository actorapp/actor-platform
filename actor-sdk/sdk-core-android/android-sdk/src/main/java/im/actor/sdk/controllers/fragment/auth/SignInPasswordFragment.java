package im.actor.sdk.controllers.fragment.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.util.HashMap;

import im.actor.core.AuthState;
import im.actor.core.api.ApiSex;
import im.actor.core.entity.Sex;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.intents.WebServiceUtil;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignInPasswordFragment extends BaseAuthFragment {

    public static final String AUTH_TYPE_EMAIL = "auth_type_email";
    public static final String AUTH_TYPE_PHONE = "auth_type_phone";
    public static final String AUTH_TYPE_CUSTOM = "auth_type_custom";
    public static final String AUTH_TYPE_USERNAME = "auth_type_username";
    String authType;
    private EditText codeEnterEditText;
    private KeyboardHelper keyboardHelper;
    String avatarPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        authType = getArguments().getString("authType");

        if (savedInstanceState != null) {
            avatarPath = savedInstanceState.getString("avatarPath", null);
        }
        keyboardHelper = new KeyboardHelper(getActivity());
        View v = inflater.inflate(R.layout.fragment_sign_passwordin, container, false);
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
        if (authType.equals(AUTH_TYPE_PHONE)) {
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
        } else if (authType.equals(AUTH_TYPE_USERNAME)) {
            String userName = messenger().getAuthUserName();
            sendHint.setText(
                    getString(R.string.auth_password_init).replace("{0}", "<b>" + userName + "</b>") );
            TextView sendName = (TextView) v.findViewById(R.id.sendUserName);
            sendName.setText(messenger().getAuthZHName());
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
//                if (s.length() == 6) {
//                    sendCode();
//                }
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
        if (authType.equals(AUTH_TYPE_EMAIL)) {
            editAuth.setText(getString(R.string.auth_code_wrong_email));
        }
        onClick(v, R.id.button_edit_phone, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(authType.equals(AUTH_TYPE_USERNAME) ? R.string.auth_code_username_change : R.string.auth_code_phone_change)
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
        final String text = codeEnterEditText.getText().toString().trim();
        if (text.length() > 0) {
//            executeAuth(messenger().signUp(text, null,"zs2860400q",avatarPath),"SignUp");
//            executeAuth(messenger().validatePassword(text),"Send Password");

            executeAuth(new Command<AuthState>() {
                @Override
                public void start(final CommandCallback<AuthState> callback) {
                    HashMap<String, String> par = new HashMap<String, String>();
                    par.put("oaUserName", messenger().getAuthUserName());
                    par.put("password", text);
                    WebServiceUtil.webServiceRun(messenger().getAuthWebServiceIp(), par, "validatePassword", new SignUpHandeler(callback, text));
                }
            }, "validatePassword");
//            executeAuth(messenger().validateCode(text), "Send Code");
        }
    }


    class SignUpHandeler extends Handler {
        CommandCallback<AuthState> callback;
        String password;

        public SignUpHandeler(CommandCallback<AuthState> callback, String password) {
            this.callback = callback;
            this.password = password;
        }

        public SignUpHandeler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String datasource = b.getString("datasource");
            try {
                JSONObject jo = new JSONObject(datasource);
                String result = jo.getString("result").trim();
                if ("false".equals(result)) {
                    im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                        @Override
                        public void run() {
                            RpcException e = new RpcException("PASSWORD ERROR", 400, "密码错误，请重新输入", false, null);
                            callback.onError(e);
                        }
                    });
                } else if ("true".equals(result)) {
                    executeAuth(messenger().validatePassword(password), "Send Password");

//                    Command<AuthState> command = messenger().requestStartUserNameAuth(name);
//                    executeAuth(command, "Request code");
//                    final String ACTION = "Request code";
//                    Command<AuthState> command = messenger().requestSignUp(name, messenger().getAuthWebServiceIp());
//                    executeAuth(command, ACTION);
//                    im.actor.runtime.Runtime.postToMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        callback.onResult(AuthState.batchSignUp);
//                    }
//                });
                }
//
            } catch (Exception e) {
                e.printStackTrace();
                executeAuth(messenger().validatePassword(password), "Send Password");
//                im.actor.runtime.Runtime.postToMainThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        RpcException e = new RpcException("PASSWORD ERROR", 400, "密码错误，请重新输入", false, null);
//                        callback.onError(e);
//                    }
//                });
            }
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
