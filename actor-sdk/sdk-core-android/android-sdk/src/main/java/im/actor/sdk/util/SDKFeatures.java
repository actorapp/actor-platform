package im.actor.sdk.util;


import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import im.actor.core.modules.push.PushRegisterActor;

public class SDKFeatures {

    private static boolean isGoogleMapsSupported;
    public static boolean isGoogleMapsSupported(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if(resultCode != ConnectionResult.SUCCESS){
            isGoogleMapsSupported = false;
        }else if(googleApiAvailability.isUserResolvableError(resultCode)){
            isGoogleMapsSupported = false;
        }
        return isGoogleMapsSupported;
    }

    static {
        try {
//            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
//            int resultCode = googleApiAvailability.isGooglePlayServicesAvailable();
            Class.forName("com.google.android.gms.maps.GoogleMap");
            isGoogleMapsSupported = true;
        } catch (ClassNotFoundException e) {
            isGoogleMapsSupported = false;
        }
    }
}
