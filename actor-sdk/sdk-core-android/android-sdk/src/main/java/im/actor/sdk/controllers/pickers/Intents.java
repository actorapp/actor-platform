package im.actor.sdk.controllers.pickers;

import android.content.Context;
import android.content.Intent;

import im.actor.sdk.BuildConfig;
import im.actor.sdk.controllers.pickers.file.FilePickerActivity;


/**
 * Created by ex3ndr on 14.10.14.
 */
public class Intents {
    public static Intent pickFile(Context context) {
        return new Intent(context, FilePickerActivity.class);
    }


    public static Intent pickLocation(Context context) {
        Intent intent = new Intent("im.actor.pickLocation_" + context.getPackageName());
        return intent;
    }


}
