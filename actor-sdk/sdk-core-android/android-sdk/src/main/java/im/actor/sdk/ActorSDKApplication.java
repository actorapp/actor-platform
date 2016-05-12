package im.actor.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import im.actor.runtime.android.AndroidContext;

/**
 * Implementation of Application object that handles everything required for creating and
 * managing Actor SDK
 */
public class ActorSDKApplication extends Application {

    public static void setLanguage(Context context, String lang){
        //Language TEST
        Resources res = context.getResources();
        Configuration newConfig = new Configuration( res.getConfiguration() );
        Locale locale = new Locale(lang);
        if( locale == null ) {
            //Some Error
            return;
        }
        Locale.setDefault(locale);
        newConfig.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            newConfig.setLayoutDirection( locale );
        }
        res.updateConfiguration( newConfig, null );
    }

    public static String getCurrentLocale(Context context){
        String lang ;
        lang = context.getSharedPreferences("properties.ini", Context.MODE_PRIVATE).getString("language", "en");

        return lang;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setLanguage(this,getCurrentLocale(this));

        int id = android.os.Process.myPid();
        String myProcessName = getPackageName();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo procInfo : activityManager.getRunningAppProcesses()) {
            if (id == procInfo.pid) {
                myProcessName = procInfo.processName;
            }
        }

        // Protection on double start
        if (!myProcessName.endsWith(":actor_push")) {
            AndroidContext.setContext(this);
            onConfigureActorSDK();
            ActorSDK.sharedActor().createActor(this);
        }
    }

    /**
     * Override this method for implementing Actor SDK Implementation
     */
    public void onConfigureActorSDK() {

    }
}