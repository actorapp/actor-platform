/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.modules.settings.SettingsSyncActor;

public class Settings extends BaseModule {

    private final String STORAGE_PREFIX = "app.tones_enabled";

    private final String KEY_NOTIFICATION_TONES;
    private final String KEY_CHAT_SEND_BY_ENTER;

    private final String KEY_NOTIFICATION_SOUND;
    private final String KEY_NOTIFICATION_VIBRATION;
    private final String KEY_NOTIFICATION_TEXT;
    private final String KEY_NOTIFICATION_CHAT_PREFIX;

    private ActorRef settingsSync;

    public Settings(Modules modules) {
        super(modules);

        String configKey;
        switch (modules.getConfiguration().getAppCategory()) {
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
        switch (modules.getConfiguration().getDeviceCategory()) {
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

        // Category specific settings
        KEY_NOTIFICATION_SOUND = "category." + deviceTypeKey + ".notification.sound.enabled";
        KEY_NOTIFICATION_VIBRATION = "category." + deviceTypeKey + ".notification.vibration.enabled";
        KEY_NOTIFICATION_TEXT = "category." + deviceTypeKey + ".notification.show_text";
        KEY_NOTIFICATION_CHAT_PREFIX = "category." + deviceTypeKey + ".notification.chat.";
    }

    public void run() {
        settingsSync = ActorSystem.system().actorOf(Props.create(SettingsSyncActor.class, new ActorCreator<SettingsSyncActor>() {
            @Override
            public SettingsSyncActor create() {
                return new SettingsSyncActor(modules());
            }
        }), "actor/settings");
    }

    public void onUpdatedSetting(String key, String value) {
        writeValue(key, value);
    }

    // Notifications

    public boolean isConversationTonesEnabled() {
        return loadValue(KEY_NOTIFICATION_TONES, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_TONES, val);
    }

    public boolean isNotificationSoundEnabled() {
        return loadValue(KEY_NOTIFICATION_SOUND, true);
    }

    public void changeNotificationSoundEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_SOUND, val);
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

    // Chat settings

    public boolean isSendByEnterEnabled() {
        return loadValue(KEY_CHAT_SEND_BY_ENTER, true);
    }

    public void changeSendByEnter(boolean val) {
        changeValue(KEY_CHAT_SEND_BY_ENTER, val);
    }

    // Peer settings

    public boolean isNotificationsEnabled(Peer peer) {
        return loadValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", true);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        changeValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", val);
    }

    private String getChatKey(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            return "PRIVATE_" + peer.getPeerId();
        } else if (peer.getPeerType() == PeerType.GROUP) {
            return "GROUP_" + peer.getPeerId();
        } else {
            throw new RuntimeException("Unsupported peer");
        }
    }

    private boolean loadValue(String key, boolean defaultVal) {
        String sValue = readValue(key);
        if ("true".equals(sValue)) {
            return true;
        } else if ("false".equals(sValue)) {
            return false;
        } else {
            return defaultVal;
        }
    }

    private void changeValue(String key, boolean val) {
        String sVal = val ? "true" : "false";
        writeValue(key, sVal);
        settingsSync.send(new SettingsSyncActor.ChangeSettings(key, sVal));
    }

    private void writeValue(String key, String val) {
        preferences().putString(STORAGE_PREFIX + key, val);
    }

    private String readValue(String key) {
        return preferences().getString(STORAGE_PREFIX + key);
    }
}
