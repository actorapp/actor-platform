package im.actor.model.modules;

import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 03.03.15.
 */
public class Settings extends BaseModule {

    private static final String KEY_NOTIFICATION_TONES = "settings_notification_tones";
    private static final String KEY_NOTIFICATION_SOUND = "settings_notification_sounds";
    private static final String KEY_NOTIFICATION_VIBRATION = "settings_notification_vibration";
    private static final String KEY_NOTIFICATION_TEXT = "settings_notification_text";
    private static final String KEY_NOTIFICATION_CHAT = "settings_notification_chat_";
    private static final String KEY_CHAT_SEND_BY_ENTER = "settings_chat_send_by_enter";

    public Settings(Modules modules) {
        super(modules);
    }

    // Notifications

    public boolean isConversationTonesEnabled() {
        return preferences().getBool(KEY_NOTIFICATION_TONES, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        preferences().putBool(KEY_NOTIFICATION_TONES, val);
    }

    public boolean isNotificationSoundEnabled() {
        return preferences().getBool(KEY_NOTIFICATION_SOUND, true);
    }

    public void changeNotificationSoundEnabled(boolean val) {
        preferences().putBool(KEY_NOTIFICATION_SOUND, val);
    }

    public boolean isVibrationEnabled() {
        return preferences().getBool(KEY_NOTIFICATION_VIBRATION, true);
    }

    public void changeNotificationVibrationEnabled(boolean val) {
        preferences().putBool(KEY_NOTIFICATION_VIBRATION, val);
    }

    public boolean isShowNotificationsText() {
        return preferences().getBool(KEY_NOTIFICATION_TEXT, true);
    }

    public void changeShowNotificationTextEnabled(boolean val) {
        preferences().putBool(KEY_NOTIFICATION_TEXT, val);
    }

    // Chat settings

    public boolean isSendByEnterEnabled() {
        return preferences().getBool(KEY_CHAT_SEND_BY_ENTER, true);
    }

    public void changeSendByEnter(boolean val) {
        preferences().putBool(KEY_CHAT_SEND_BY_ENTER, val);
    }

    // Peer settings

    public boolean isNotificationsEnabled(Peer peer) {
        return preferences().getBool(KEY_NOTIFICATION_CHAT + peer.getUnuqueId(), true);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        preferences().putBool(KEY_NOTIFICATION_CHAT + peer.getUnuqueId(), val);
    }
}
