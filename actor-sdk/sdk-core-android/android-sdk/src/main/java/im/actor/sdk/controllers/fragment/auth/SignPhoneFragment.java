package im.actor.sdk.controllers.fragment.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Devices;
import im.actor.sdk.util.country.Country;
import im.actor.sdk.util.country.Countries;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.view.SelectorFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class SignPhoneFragment extends BaseAuthFragment {

    private static final int REQUEST_COUNTRY = 0;

    private Countries countryDb;

    private Button countrySelectButton;
    private EditText countryCodeEditText;
    private BackspaceKeyEditText phoneNumberEditText;

    private boolean ignoreNextCodeChange;

    private KeyboardHelper keyboardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_phone, container, false);

        TextView buttonContinue = (TextView) v.findViewById(R.id.button_continue_text);
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getMainColor(), getActivity());
        buttonContinue.setBackground(states);
        buttonContinue.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        buttonContinue.setTypeface(Fonts.medium());
        ((TextView) v.findViewById(R.id.button_why)).setTypeface(Fonts.medium());
        ((TextView) v.findViewById(R.id.button_why)).setTextColor(ActorSDK.sharedActor().style.getMainColor());

        keyboardHelper = new KeyboardHelper(getActivity());

        initView(v);

        countryDb = Countries.getInstance();

        String deviceCountry = Devices.getDeviceCountry();
        if (!TextUtils.isEmpty(deviceCountry)) {
            Country country = countryDb.getCountryByShortName(deviceCountry);
            setCountryName(country);
            if (country != null) {
                countryCodeEditText.setText(country.phoneCode);
                focusPhone();
            } else {
                focusCode();
            }
        } else {
            setCountryName(null);
            countryCodeEditText.setText("");
            focusCode();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();


        setTitle(R.string.auth_phone_title);

        if (TextUtils.isEmpty(countryCodeEditText.getText())) {
            focusCode();
        } else {
            focusPhone();
        }
        keyboardHelper.setImeVisibility(phoneNumberEditText, true);
    }

    private void initView(View v) {
        ((TextView) v.findViewById(R.id.phone_sign_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        countrySelectButton = (Button) v.findViewById(R.id.button_country_select);
        countrySelectButton.setTextColor(ActorSDK.sharedActor().style.getMainColor());
        onClick(countrySelectButton, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardHelper.setImeVisibility(phoneNumberEditText, false);
                startActivityForResult(new Intent(getActivity(), PickCountryActivity.class), REQUEST_COUNTRY);
            }
        });

        countryCodeEditText = (EditText) v.findViewById(R.id.tv_country_code);
        countryCodeEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        countryCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                final Activity a = getActivity();
                if (a != null) {

                    final String str = s.toString();
                    if (str.length() == 4 && countryDb != null) {
                        if (countryDb.getCountryByPhoneCode(str) != null) {
                            focusPhone();
                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 3)) != null) {
                            countryCodeEditText.setText(str.substring(0, 3));
                            phoneNumberEditText.setText(str.substring(3, 4));
                            focusPhone();
                            return;
                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 2)) != null) {
                            countryCodeEditText.setText(str.substring(0, 2));
                            phoneNumberEditText.setText(str.substring(2, 4));
                            focusPhone();
                            return;
                        } else if (countryDb.getCountryByPhoneCode(str.substring(0, 1)) != null) {
                            countryCodeEditText.setText(str.substring(0, 1));
                            phoneNumberEditText.setText(str.substring(1, 4));
                            focusPhone();
                            return;
                        }
                    }

                    if (!ignoreNextCodeChange) {
                        if (TextUtils.isEmpty(s)) {
                            countrySelectButton.setText(R.string.auth_phone_country_title);
                        } else {
                            if (countryDb != null) {
                                final Country country = countryDb.getCountryByPhoneCode(s.toString());
                                if (country == null) {
                                    countrySelectButton.setText(R.string.auth_phone_error_invalid_country);
                                } else {
                                    setCountryName(country);
                                }
                            }
                        }
                    } else {
                        ignoreNextCodeChange = false;
                    }
                }
            }
        });


        phoneNumberEditText = (BackspaceKeyEditText) v.findViewById(R.id.tv_phone_number);
        phoneNumberEditText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phoneNumberEditText.setBackspaceListener(new BackspaceKeyEditText.BackspacePressListener() {
            @Override
            public boolean onBackspacePressed() {
                if (phoneNumberEditText.getText().length() == 0) {
                    focusCode();
                    return false;
                } else {
                    return true;
                }
            }
        });

        phoneNumberEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_GO) {
                    requestCode();
                    return true;
                }
                return false;
            }
        });
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

        countryCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    focusPhone();
                    return true;
                }
                return false;
            }
        });

        v.findViewById(R.id.button_why).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.auth_phone_why_description)
                        .setPositiveButton(R.string.auth_phone_why_done, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
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
        final String ACTION = "Request code";


        if (countryCodeEditText.getText().toString().trim().length() == 0 ||
                phoneNumberEditText.getText().toString().trim().length() == 0) {
            String message = getString(R.string.auth_error_empty_phone);
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_phone)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }

        String rawPhoneN = countryCodeEditText.getText().toString().replaceAll("[^0-9]", "") +
                phoneNumberEditText.getText().toString().replaceAll("[^0-9]", "");

        if (rawPhoneN.length() == 0) {
            String message = getString(R.string.auth_error_empty_phone);
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.auth_error_empty_phone)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .show();
            return;
        }

        executeAuth(messenger().requestStartPhoneAuth(Long.parseLong(rawPhoneN)), ACTION);
    }

    private void focusCode() {
        focus(countryCodeEditText);
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
                countryCodeEditText.setText(country.phoneCode);
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

}
