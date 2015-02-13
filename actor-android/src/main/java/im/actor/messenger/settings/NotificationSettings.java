package im.actor.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.droidkit.mvvm.preferences.PreferenceBoolean;
import com.droidkit.mvvm.ValueModel;
import im.actor.messenger.core.AppContext;

import java.util.HashMap;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class NotificationSettings {

    private static final Object LOCK = new Object();
    private static NotificationSettings settings;

    public static NotificationSettings getInstance() {
        if (settings == null) {
            synchronized (LOCK) {
                if (settings == null) {
                    settings = new NotificationSettings(AppContext.getContext());
                }
            }
        }
        return settings;
    }

    private SharedPreferences notificationPreferences;

    private PreferenceBoolean isInAppSoundEnabled;
    private PreferenceBoolean showTitles;
    private PreferenceBoolean isNotificationsSoundsEnabled;
    private PreferenceBoolean isNotificationsVibrateEnabled;
    private final HashMap<Long, PreferenceBoolean> conversationsEnabled;

    private NotificationSettings(Context context) {
        notificationPreferences = context.getSharedPreferences("notifications.ini", Context.MODE_PRIVATE);
        conversationsEnabled = new HashMap<Long, PreferenceBoolean>();
        isInAppSoundEnabled = new PreferenceBoolean("notifications.in_app.enabled", notificationPreferences, true);
        isNotificationsSoundsEnabled = new PreferenceBoolean("notifications.sound.enabled", notificationPreferences, true);
        isNotificationsVibrateEnabled = new PreferenceBoolean("notifications.vibration.enabled", notificationPreferences, true);
        showTitles = new PreferenceBoolean("notifications.titles.enabled", notificationPreferences, true);

    }

    public PreferenceBoolean showTitlesValue() {
        return showTitles;
    }

    public ValueModel<Boolean> inAppSoundValue() {
        return isInAppSoundEnabled;
    }

    public ValueModel<Boolean> vibrateValue() {
        return isNotificationsVibrateEnabled;
    }

    public ValueModel<Boolean> soundValue() {
        return isNotificationsSoundsEnabled;
    }

    public ValueModel<Boolean> convValue(long id) {
        synchronized (conversationsEnabled) {
            if (conversationsEnabled.containsKey(id)) {
                return conversationsEnabled.get(id);
            }
            PreferenceBoolean val = new PreferenceBoolean("notifications." + id + ".enabled", notificationPreferences, true);
            conversationsEnabled.put(id, val);
            return val;
        }
    }

    public boolean isInAppEnabled() {
        return isInAppSoundEnabled.getValue();
    }

    public boolean isVibrationEnabled() {
        return isNotificationsVibrateEnabled.getValue();
    }

    public boolean isSoundsEnabled() {
        return isNotificationsSoundsEnabled.getValue();
    }

    public boolean isShowTitles() {
        return showTitles.getValue();
    }
}
