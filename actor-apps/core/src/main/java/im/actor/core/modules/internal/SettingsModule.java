/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.settings.SettingsSyncActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;

public class SettingsModule extends AbsModule {

    private final String STORAGE_PREFIX = "app.settings.";

    private final String KEY_SOUND_EFFECTS;
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

    private final String KEY_NOTIFICATION_GROUP_ENABLED;
    private final String KEY_NOTIFICATION_GROUP_ONLY_MENTIONS;

    private final String KEY_MARKDOWN_ENABLED;

    private final String KEY_RENAME_HINT_SHOWN;

    private final String KEY_WALLPAPPER;

    private ActorRef settingsSync;

    public SettingsModule(ModuleContext context) {
        super(context);

        String platformType;
        switch (context.getConfiguration().getPlatformType()) {
            case ANDROID:
                platformType = "android";
                break;
            case IOS:
                platformType = "ios";
                break;
            case WEB:
                platformType = "web";
                break;
            default:
            case GENERIC:
                platformType = "generic";
                break;
        }

        String deviceType;
        switch (context.getConfiguration().getDeviceCategory()) {
            case TABLET:
                deviceType = "tablet";
                break;
            case DESKTOP:
                deviceType = "desktop";
                break;
            case MOBILE:
                deviceType = "mobile";
                break;
            default:
                deviceType = "generic";
                break;
        }

        // Platform+Device specific settings
        KEY_SOUND_EFFECTS = "app." + platformType + "." + deviceType + ".tones_enabled";
        KEY_CHAT_SEND_BY_ENTER = "app." + platformType + "." + deviceType + ".send_by_enter";
        KEY_MARKDOWN_ENABLED = "app." + platformType + "." + deviceType + ".use_markdown";

        // Device-type notification settings
        KEY_NOTIFICATION_ENABLED = "category." + deviceType + ".notification.enabled";
        KEY_NOTIFICATION_SOUND_ENABLED = "category." + deviceType + ".notification.sound.enabled";
        KEY_NOTIFICATION_VIBRATION = "category." + deviceType + ".notification.vibration.enabled";
        KEY_NOTIFICATION_TEXT = "category." + deviceType + ".notification.show_text";
        KEY_NOTIFICATION_CHAT_PREFIX = "category." + deviceType + ".notification.chat.";

        KEY_NOTIFICATION_IN_APP_ENABLED = "category." + deviceType + ".in_app.enabled";
        KEY_NOTIFICATION_IN_APP_SOUND = "category." + deviceType + ".in_app.sound.enabled";
        KEY_NOTIFICATION_IN_APP_VIBRATION = "category." + deviceType + ".in_app.vibration.enabled";

        // Account-wide notification settings
        KEY_NOTIFICATION_SOUND = "account.notification.sound";
        KEY_NOTIFICATION_GROUP_ENABLED = "account.notifications.group.enabled";
        KEY_NOTIFICATION_GROUP_ONLY_MENTIONS = "account.notifications.group.mentions";

        // Hints
        KEY_RENAME_HINT_SHOWN = "hint.contact.rename";

        KEY_WALLPAPPER = "wallpaper.uri";
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
        return loadValue(KEY_SOUND_EFFECTS, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        changeValue(KEY_SOUND_EFFECTS, val);
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

    // Group Notifications

    public boolean isGroupNotificationsEnabled() {
        return loadValue(KEY_NOTIFICATION_GROUP_ENABLED, true);
    }

    public void changeGroupNotificationsEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_GROUP_ENABLED, val);
    }

    public boolean isGroupNotificationsOnlyMentionsEnabled() {
        return loadValue(KEY_NOTIFICATION_GROUP_ONLY_MENTIONS, false);
    }

    public void changeGroupNotificationsOnlyMentionsEnabled(boolean val) {
        changeValue(KEY_NOTIFICATION_GROUP_ONLY_MENTIONS, val);
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

    public boolean isNotificationsEnabled(Peer peer) {
        return loadValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", true);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        changeValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", val);
    }

    public String getNotificationSound(Peer peer) {
        return readValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".sound");
    }

    public void changeNotificationSound(Peer peer, String sound) {
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

    // Wallpaper

    public String getSelectedWallpapper() {
        return readValue(KEY_WALLPAPPER);
    }

    public void changeSelectedWallpapper(String uri) {
        changeValue(KEY_WALLPAPPER, uri);
    }

    // Private

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
