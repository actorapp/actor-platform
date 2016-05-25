package im.actor.sdk.controllers.auth;

import android.app.AlertDialog;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import im.actor.core.AuthState;
import im.actor.core.api.ApiPhoneActivationType;
import im.actor.core.entity.AuthMode;
import im.actor.core.entity.AuthStartRes;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.json.JSONObject;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.intents.WebServiceUtil;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignInForNickNameFragment extends BaseAuthFragment {

    private EditText signIdEditText;
    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_username, container, false);

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
                isNeedSignUp(rawId);
//                startNickNameAuth(rawId);
//                startPhoneAuth(Long.parseLong(rawId.replace("+", "")));

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

    private void isNeedSignUp(final String nickName) {
        AuthActivity authActivity = ((AuthActivity) getActivity());
        ((AuthActivity) getActivity()).showProgress(1);
        Promise<AuthStartRes> promise = new Promise<>(new PromiseFunc<AuthStartRes>() {

            @Override
            public void exec(@NotNull PromiseResolver<AuthStartRes> resolver) {
                HashMap<String, String> par = new HashMap<String, String>();
                par.put("username", nickName);
                WebServiceUtil.webServiceRun("http://220.189.207.21:8405", par, "isUserNeedSignUp", new IsNeedSignUpHandeler("http://220.189.207.21:8405", resolver));
            }
        });
        promise.then(new Consumer<AuthStartRes>() {
            @Override
            public void apply(AuthStartRes authStartRes) {
                String rawId = signIdEditText.getText().toString();
                startNickNameAuth(rawId);
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                authActivity.handleAuthError(e);
            }
        });
    }

    class IsNeedSignUpHandeler extends Handler {
        String ip;
        PromiseResolver<AuthStartRes> resolver;

        public IsNeedSignUpHandeler(String ip, PromiseResolver<AuthStartRes> resolver) {
            this.ip = ip;
            this.resolver = resolver;
        }

        public IsNeedSignUpHandeler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            final String ACTION = "Request code";
            String datasource = b.getString("datasource");
            String rawPhoneN = signIdEditText.getText().toString();
            try {
                JSONObject jo = new JSONObject(datasource);
                String result = jo.getString("result");
                if ("true".equals(result)) {
                    String name = jo.getString("name");
                    messenger().getPreferences().putString("auth_zhname", name);
                    ApiPhoneActivationType activationType = ApiPhoneActivationType.CODE;
                    resolver.result(new AuthStartRes(
                            "",
                            AuthMode.fromApi(activationType),
                            true));
                } else {
                     String errStr = jo.getString("description");
                    RpcException e = new RpcException("Error", 400, errStr, false, null);
                    resolver.error(e);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                RpcException e2 = new RpcException("Error", 400, "用户名错误", false, null);
                resolver.error(e2);
            }

        }
    }

}
