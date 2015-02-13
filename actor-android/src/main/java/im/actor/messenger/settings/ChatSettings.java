package im.actor.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.droidkit.mvvm.preferences.PreferenceBoolean;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.core.AppContext;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class ChatSettings {
    private static final Object LOCK = new Object();
    private static ChatSettings settings;

    public static ChatSettings getInstance() {
        if (settings == null) {
            synchronized (LOCK) {
                if (settings == null) {
                    settings = new ChatSettings(AppContext.getContext());
                }
            }
        }
        return settings;
    }

    private SharedPreferences preferences;
    private PreferenceBoolean isSendByEnter;

    private ChatSettings(Context context) {
        preferences = context.getSharedPreferences("chat.ini", Context.MODE_PRIVATE);
        isSendByEnter = new PreferenceBoolean("chat.send_by_enter", preferences, BuildConfig.ENABLE_CHROME);
    }

    public PreferenceBoolean sendByEnterValue() {
        return isSendByEnter;
    }

    public boolean isSendByEnter() {
        return isSendByEnter.getValue();
    }
}
