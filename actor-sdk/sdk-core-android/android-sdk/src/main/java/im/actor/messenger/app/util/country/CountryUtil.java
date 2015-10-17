package im.actor.messenger.app.util.country;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class CountryUtil {

    public static String getDeviceCountry(final Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String country = tm.getSimCountryIso();

        if (TextUtils.isEmpty(country)) {
            country = tm.getNetworkCountryIso();
        }

        if (TextUtils.isEmpty(country)) {
            country = context.getResources().getConfiguration().locale.getCountry();
        }

        return country;
    }

}
