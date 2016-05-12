package im.actor.sdk.controllers.fragment.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import im.actor.core.AuthState;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.intents.WebServiceUtil;
import im.actor.sdk.util.Devices;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.util.country.Countries;
import im.actor.sdk.util.country.Country;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignUserNameFragment extends BaseAuthFragment {

    private static final int REQUEST_COUNTRY = 0;

    private Countries countryDb;

    private Button countrySelectButton;
    //    private EditText countryCodeEditText;
    private EditText phoneNumberEditText;

    private boolean ignoreNextCodeChange;

    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_username, container, false);

        TextView buttonContinue = (TextView) v.findViewById(R.id.button_continue_text);
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            buttonContinue.setBackground(states);
        } else {
            buttonContinue.setBackgroundDrawable(states);
        }
        buttonContinue.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        buttonContinue.setTypeface(Fonts.medium());
//        ((TextView) v.findViewById(R.id.button_why)).setTypeface(Fonts.medium());
//        ((TextView) v.findViewById(R.id.button_why)).setTextColor(ActorSDK.sharedActor().style.getMainColor());

        keyboardHelper = new KeyboardHelper(getActivity());

        initView(v);

        countryDb = Countries.getInstance();

//        String deviceCountry = Devices.getDeviceCountry();
//        if (!TextUtils.isEmpty(deviceCountry)) {
//            Country country = countryDb.getCountryByShortName(deviceCountry);
//            setCountryName(country);
//            if (country != null) {
//
////                countryCodeEditText.setText(country.phoneCode);
//                focusPhone();
//            } else {
//                focusCode();
//            }
//        } else {
//            setCountryName(null);
////            countryCodeEditText.setText("");
//            focusCode();
//        }


        //request Web


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();


        setTitle(R.string.auth_phone_title);

//        if (TextUtils.isEmpty(countryCodeEditText.getText())) {
//            focusCode();
//        } else {
//            focusPhone();
//        }
        keyboardHelper.setImeVisibility(phoneNumberEditText, true);
    }

    private void initView(View v) {
        ((TextView) v.findViewById(R.id.phone_sign_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
//        countrySelectButton = (Button) v.findViewById(R.id.button_country_select);
//        countrySelectButton.setTextColor(ActorSDK.sharedActor().style.getMainColor());
//        onClick(countrySelectButton, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                keyboardHelper.setImeVisibility(phoneNumberEditText, false);
//                startActivityForResult(new Intent(getActivity(), PickCountryActivity.class), REQUEST_COUNTRY);
//            }
//        });

//        countryCodeEditText = (EditText) v.findViewById(R.id.tv_country_code);
//        countryCodeEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
//        countryCodeEdit                                                                                                                                                                         Text.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//                final Activity a = getActivity();
//                if (a != null) {
//
//                    final String str = s.toString();
//                    if (str.length() == 4 && countryDb != null) {
//                        if (countryDb.getCountryByPhoneCode(str) != null) {
//                            focusPhone();
//                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 3)) != null) {
//                            countryCodeEditText.setText(str.substring(0, 3));
//                            phoneNumberEditText.setText(str.substring(3, 4));
//                            focusPhone();
//                            return;
//                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 2)) != null) {
//                            countryCodeEditText.setText(str.substring(0, 2));
//                            phoneNumberEditText.setText(str.substring(2, 4));
//                            focusPhone();
//                            return;
//                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 1)) != null) {
//                            countryCodeEditText.setText(str.substring(0, 1));
//                            phoneNumberEditText.setText(str.substring(1, 4));
//                            focusPhone();
//                            return;
//                        }
//                    }
//
//                    if (!ignoreNextCodeChange) {
//                        if (TextUtils.isEmpty(s)) {
//                            countrySelectButton.setText(R.string.auth_phone_country_title);
//                        } else {
//                            if (countryDb != null) {
//                                final Country country = countryDb.getCountryByPhoneCode(s.toString());
//                                if (country == null) {
//                                    countrySelectButton.setText(R.string.auth_phone_error_invalid_country);
//                                } else {
//                                    setCountryName(country);
//                                }
//                            }
//                        }
//                    } else {
//                        ignoreNextCodeChange = false;
//                    }
//                }
//            }
//        });


        phoneNumberEditText = (EditText) v.findViewById(R.id.tv_username_number);
        phoneNumberEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
//        phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
//        phoneNumberEditText.setBackspaceListener(new BackspaceKeyEditText.BackspacePressListener() {
//            @Override
//            public boolean onBackspacePressed() {
//                if (phoneNumberEditText.getText().length() == 0) {
//                    focusCode();
//                    return false;
//                } else {
//                    return true;
//                }
//            }
//        });

//        phoneNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == EditorInfo.IME_ACTION_GO) {
//                    requestCode();
//                    return true;
//                }
//                return false;
//            }
//        });
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
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

//        countryCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_GO) {
//                    focusPhone();
//                    return true;
//                }
//                return false;
//            }
//        });

//        v.findViewById(R.id.button_why).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new AlertDialog.Builder(getActivity())
//                        .setMessage(R.string.auth_phone_why_description)
//                        .setPositiveButton(R.string.auth_phone_why_done, null)
//                        .show()
//                        .setCanceledOnTouchOutside(true);
//            }
//        });

        onClick(v, R.id.button_continue, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestCode();
            }
        });
    }

    private void requestCode() {
        final String ACTION = "Request code";


//        if (countryCodeEditText.getText().toString().trim().length() == 0 ||
//                phoneNumberEditText.getText().toString().trim().length() == 0) {
//            String message = getString(R.string.auth_error_empty_phone);
//            new AlertDialog.Builder(getActivity())
//                    .setMessage(R.string.auth_error_empty_phone)
//                    .setPositiveButton(R.string.dialog_ok, null)
//                    .show();
//            return;
//        }

//        String rawPhoneN = countryCodeEditText.getText().toString().replaceAll("[^0-9]", "") +
//                phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");
        String rawPhoneN = phoneNumberEditText.getText().toString();
        if (rawPhoneN.length() == 0) {
            String message = getString(R.string.auth_error_empty_phone);
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_phone)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }
        isNeedSignUp(rawPhoneN);
//        Command<AuthState> command = messenger().requestStartUserNameAuth(rawPhoneN);
////        Sex sex = Sex.fromValue(1);
////        Command<AuthState> command =  messenger().validatePassword("pwd");
//        executeAuth(command, ACTION);

    }

    private void focusCode() {
//        focus(countryCodeEditText);
    }

    private void focusPhone() {
        focus(phoneNumberEditText);
    }

    private void setCountry(final Country country) {
        final Activity a = getActivity();
        if (a != null) {
            if (country != null) {
                ignoreNextCodeChange = true;
                setCountryName(country);

//                countryCodeEditText.setText(country.phoneCode);
            }
            focusPhone();
        }
    }

    private void setCountryName(final Country country) {
        if (country == null) {
            countrySelectButton.setText(getString(R.string.auth_phone_country_title));
        } else {
            countrySelectButton.setText(getString(country.fullNameRes));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COUNTRY && resultCode == Activity.RESULT_OK) {
            setCountry(new Country(data.getStringExtra("country_code"),
                    data.getStringExtra("country_shortname"),
                    data.getIntExtra("country_id", 0)));
        }
    }


    private void isNeedSignUp(String username) {
        HashMap<String, String> par = new HashMap<String, String>();
        par.put("username", username);
        WebServiceUtil.webServiceRun("http://220.189.207.21:8045", par, "isUserNeedSignUp", new IsNeedSignUpHandeler("http://192.168.1.183"));
    }


    class IsNeedSignUpHandeler extends Handler {
        String ip;

        public IsNeedSignUpHandeler(String ip) {
            this.ip = ip;
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
            String rawPhoneN = phoneNumberEditText.getText().toString();
            try {
                JSONObject jo = new JSONObject(datasource);
                String result = jo.getString("result");
                String name = jo.getString("name");
                messenger().getPreferences().putString("auth_zhname", name);
                if ("true".equals(result)) {
                    if ("login".equals(jo.getString("next").trim())) {
                        Command<AuthState> command = messenger().requestSignUp(rawPhoneN,name, ip);
                        executeAuth(command, ACTION);
                    } else {
                        Command<AuthState> command = messenger().requestStartUserNameAuth(rawPhoneN);
                        executeAuth(command, ACTION);
                    }

                } else {
                    final String errStr = jo.getString("description");
                    executeAuth(new Command<AuthState>() {
                        @Override
                        public void start(final CommandCallback<AuthState> callback) {
                            im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    RpcException e = new RpcException("ERROR", 400, errStr, false, null);
                                    callback.onError(e);
                                }
                            });
                        }
                    }, "error");
//                    Command<AuthState> command = messenger().requestStartUserNameAuth(rawPhoneN);
//                    executeAuth(command, ACTION);
                }
            } catch (Exception e) {
                e.printStackTrace();
//                Command<AuthState> command = messenger().requestStartUserNameAuth(rawPhoneN);
//                executeAuth(command, ACTION);
            }

        }
    }


}
