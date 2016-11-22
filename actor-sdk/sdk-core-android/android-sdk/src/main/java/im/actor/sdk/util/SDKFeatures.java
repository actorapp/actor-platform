package im.actor.sdk.util;


public class SDKFeatures {

    private static boolean isGoogleMapsSupported;

    public static boolean isGoogleMapsSupported() {
        return isGoogleMapsSupported;
    }

    static {
        try {
            Class.forName("com.google.android.gms.maps.GoogleMap");
            isGoogleMapsSupported = true;
        } catch (ClassNotFoundException e) {
            isGoogleMapsSupported = false;
        }
    }
}
