/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.entity.PeerEntity;
import im.actor.core.entity.PeerTypeEntity;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.settings.SettingsSyncActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;

public class SettingsModule extends AbsModule {

    private final String STORAGE_PREFIX = "app.settings.";

    private final String KEY_NOTIFICATION_TONES;
    private final String KEY_CHAT_SEND_BY_ENTER;

    private final String KEY_NOTIFICATION_ENABLED;
    private final String KEY_NOTIFICATION_SOUND;
    private final String KEY_NOTIFICATION_SOUND_ENABLED;
    private final String KEY_NOTIFICATION_VIBRATION;
    private final String KEY_NOTIFICATION_IN_APP_ENABLED;
    private final String KEY_NOTIFICATION_IN_APP_SOUND;
    private final String KEY_NOTIFICATION_IN_APP_VIBRATION;
    private final String KEY_NOTIFICATION_TEXT;
    private final String KEY_NOTIFICATION_CHAT_PREFIX;
    private final String KEY_MARKDOWN_ENABLED;

    private final String KEY_RENAME_HINT_SHOWN;

    private ActorRef settingsSync;

    public SettingsModule(ModuleContext context) {
        super(context);

        String configKey;
        switch (context.getConfiguration().getAppCategory()) {
            case ANDROID:
                configKey = "android";
                break;
            case IOS:
                configKey = "ios";
                break;
            case WEB:
                configKey = "web";
                break;
            default:
            case GENERIC:
                configKey = "generic";
                break;
        }
        String deviceTypeKey;
        switch (context.getConfiguration().getDeviceCategory()) {
            case DESKTOP:
                deviceTypeKey = "desktop";
                break;
            case MOBILE:
                deviceTypeKey = "mobile";
                break;
            default:
            case UNKNOWN:
                deviceTypeKey = "generic";
                break;
        }

        // App specific settings
        KEY_NOTIFICATION_TONES = "app." + configKey + ".tones_enabled";
        KEY_CHAT_SEND_BY_ENTER = "app." + configKey + ".send_by_enter";
        KEY_MARKDOWN_ENABLED = "app." + configKey + ".use_markdown";
        KEY_NOTIFICATION_SOUND = "account.notification.sound";

        // Category specific settings
        KEY_NOTIFICATION_ENABLED = "category." + deviceTypeKey + ".notification.enabled";
        KEY_NOTIFICATION_SOUND_ENABLED = "category." + deviceTypeKey + ".notification.sound.enabled";
        KEY_NOTIFICATION_VIBRATION = "category." + deviceTypeKey + ".notification.vibration.enabled";
        KEY_NOTIFICATION_TEXT = "category." + deviceTypeKey + ".notification.show_text";
        KEY_NOTIFICATION_CHAT_PREFIX = "category." + deviceTypeKey + ".notification.chat.";

        KEY_NOTIFICATION_IN_APP_ENABLED = "category." + deviceTypeKey + ".in_app.enabled";
        KEY_NOTIFICATION_IN_APP_SOUND = "category." + deviceTypeKey + ".in_app.sound.enabled";
        KEY_NOTIFICATION_IN_APP_VIBRATION = "category." + deviceTypeKey + ".in_app.vibration.enabled";

        // Hints
        KEY_RENAME_HINT_SHOWN = "hint.contact.rename";
    }

    public void run() {
        settingsSync = ActorSystem.system().actorOf(Props.create(SettingsSyncActor.class, new ActorCreator<SettingsSyncActor>() {
            @Override
            public SettingsSyncActor create() {
                return new SettingsSyncActor(context());
            }
        }), "actor/settings");
    }

    public void onUpdatedSetting(String key, String value) {
        writeValue(key, value);
    }

    // Sound Effects

    public boolean isConversationTonesEnabled() {
        return loadValue(KEY_NOTIFICATION_TONES, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_TONES, val);
    }

    // Notifications

    public boolean isNotificationsEnabled() {
        return loadValue(KEY_NOTIFICATION_ENABLED, true);
    }

    public void changeNotificationsEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_ENABLED, val);
    }

    public boolean isNotificationSoundEnabled() {
        return loadValue(KEY_NOTIFICATION_SOUND_ENABLED, true);
    }

    public void changeNotificationSoundEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_SOUND_ENABLED, val);
    }

    public String getNotificationSound() {
        return readValue(KEY_NOTIFICATION_SOUND);
    }

    public void changeNotificationSound(String sound) {
        changeValue(KEY_NOTIFICATION_SOUND, sound);
    }

    public boolean isVibrationEnabled() {
        return loadValue(KEY_NOTIFICATION_VIBRATION, true);
    }

    public void changeNotificationVibrationEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_VIBRATION, val);
    }

    public boolean isShowNotificationsText() {
        return loadValue(KEY_NOTIFICATION_TEXT, true);
    }

    public void changeShowNotificationTextEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_TEXT, val);
    }

    // In-App notifications

    public boolean isInAppEnabled() {
        return loadValue(KEY_NOTIFICATION_IN_APP_ENABLED, true);
    }

    public void changeInAppEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_IN_APP_ENABLED, val);
    }

    public boolean isInAppSoundEnabled() {
        return loadValue(KEY_NOTIFICATION_IN_APP_SOUND, true);
    }

    public void changeInAppSoundEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_IN_APP_SOUND, val);
    }

    public boolean isInAppVibrationEnabled() {
        return loadValue(KEY_NOTIFICATION_IN_APP_VIBRATION, true);
    }

    public void changeInAppVibrationEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_IN_APP_VIBRATION, val);
    }

    // Chat settings

    public boolean isSendByEnterEnabled() {
        return loadValue(KEY_CHAT_SEND_BY_ENTER, true);
    }

    public void changeSendByEnter(boolean val) {
        changeValue(KEY_CHAT_SEND_BY_ENTER, val);
    }

    public boolean isMarkdownEnabled() {
        return loadValue(KEY_MARKDOWN_ENABLED, false);
    }

    public void changeMarkdown(boolean val) {
        changeValue(KEY_MARKDOWN_ENABLED, val);
    }

    // Peer settings

    public boolean isNotificationsEnabled(PeerEntity peer) {
        return loadValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", true);
    }

    public void changeNotificationsEnabled(PeerEntity peer, boolean val) {
        changeValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", val);
    }

    public String getNotificationSound(PeerEntity peer) {
        return readValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".sound");
    }

    public void changeNotificationSound(PeerEntity peer, String sound) {
        changeValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".sound", sound);
    }

    // Hint

    public boolean isRenameHintShown() {
        boolean res = loadValue(KEY_RENAME_HINT_SHOWN, false);
        if (!res) {
            changeValue(KEY_RENAME_HINT_SHOWN, true);
        }
        return res;
    }

    // Private

    private String getChatKey(PeerEntity peer) {
        if (peer.getPeerType() == PeerTypeEntity.PRIVATE) {
            return "PRIVATE_" + peer.getPeerId();
        } else if (peer.getPeerType() == PeerTypeEntity.GROUP) {
            return "GROUP_" + peer.getPeerId();
        } else {
            throw new RuntimeException("Unsupported peer");
        }
    }

    private boolean loadValue(String key, boolean defaultVal) {
        String sValue = readValue(key);
        boolean res = defaultVal;
        if (sValue != null) {
            if ("true".equals(sValue)) {
                res = true;
            } else if ("false".equals(sValue)) {
                res = false;
            }
        }
        return res;
    }

    private void changeValue(String key, boolean val) {
        String sVal = val ? "true" : "false";
        changeValue(key, sVal);
    }

    private void changeValue(String key, String val) {
        writeValue(key, val);
        settingsSync.send(new SettingsSyncActor.ChangeSettings(key, val));
    }

    private void writeValue(String key, String val) {
        preferences().putString(STORAGE_PREFIX + key, val);
    }

    private String readValue(String key) {
        return preferences().getString(STORAGE_PREFIX + key);
    }

    public void resetModule() {
        // TODO: Implement
    }
}
