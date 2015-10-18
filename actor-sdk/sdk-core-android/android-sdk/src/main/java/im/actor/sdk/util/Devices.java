package im.actor.sdk.util;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import im.actor.runtime.android.AndroidContext;

import static im.actor.sdk.util.Strings.capitalize;

public class Devices {

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getDeviceCountry() {
        TelephonyManager tm = (TelephonyManager) AndroidContext.getContext().getSystemService(Context.TELEPHONY_SERVICE);

        String country = tm.getSimCountryIso();

        if (android.text.TextUtils.isEmpty(country)) {
            country = tm.getNetworkCountryIso();
        }

        if (android.text.TextUtils.isEmpty(country)) {
            country = AndroidContext.getContext().getResources().getConfiguration().locale.getCountry();
        }

        return country;
    }
}
