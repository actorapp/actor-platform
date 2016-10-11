/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.settings;

import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.events.SettingsChanged;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.eventbus.EventBus;

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
    private final String KEY_PRIVACY;
    private final String KEY_CHAT_TEXT_SIZE;

    private final String KEY_ANIMATION_AUTO_PLAY;

    private final String KEY_DOC_AUTO_DOWNLOAD;
    private final String KEY_IMAGE_AUTO_DOWNLOAD;
    private final String KEY_VIDEO_AUTO_DOWNLOAD;
    private final String KEY_ANIMATION_AUTO_DOWNLOAD;
    private final String KEY_AUDIO_AUTO_DOWNLOAD;

    private final String KEY_NOTIFICATION_PEER_SOUND;

    private ActorRef settingsSync;

    private EventBus eventBus;

    public SettingsModule(ModuleContext context) {
        super(context);

        eventBus = context.getEvents();

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
        KEY_SOUND_EFFECTS = "app." + platformType + "" + deviceType + ".tones_enabled";
        KEY_CHAT_SEND_BY_ENTER = "app." + platformType + "" + deviceType + ".send_by_enter";
        KEY_MARKDOWN_ENABLED = "app." + platformType + "" + deviceType + ".use_markdown";
        KEY_CHAT_TEXT_SIZE = "app." + platformType + "." + deviceType + ".text_size";
        KEY_NOTIFICATION_PEER_SOUND = "app." + platformType + "." + deviceType + ".notification.chat.sound.";

        // Device-type notification settings
        KEY_NOTIFICATION_ENABLED = "category." + deviceType + ".notification.enabled";
        KEY_NOTIFICATION_SOUND_ENABLED = "category." + deviceType + ".notification.sound.enabled";
        KEY_NOTIFICATION_VIBRATION = "category." + deviceType + ".notification.vibration.enabled";
        KEY_NOTIFICATION_TEXT = "category." + deviceType + ".notification.show_text";
        KEY_NOTIFICATION_CHAT_PREFIX = "category." + deviceType + ".notification.chat.";

        KEY_NOTIFICATION_IN_APP_ENABLED = "category." + deviceType + ".in_app.enabled";
        KEY_NOTIFICATION_IN_APP_SOUND = "category." + deviceType + ".in_app.sound.enabled";
        KEY_NOTIFICATION_IN_APP_VIBRATION = "category." + deviceType + ".in_app.vibration.enabled";

        KEY_ANIMATION_AUTO_PLAY = "category." + deviceType + ".auto_play.enabled";

        KEY_ANIMATION_AUTO_DOWNLOAD = "category." + deviceType + ".auto_download_animation.enabled";
        KEY_VIDEO_AUTO_DOWNLOAD = "category." + deviceType + ".auto_download_video.enabled";
        KEY_IMAGE_AUTO_DOWNLOAD = "category." + deviceType + ".auto_download_image.enabled";
        KEY_AUDIO_AUTO_DOWNLOAD = "category." + deviceType + ".auto_download_audio.enabled";
        KEY_DOC_AUTO_DOWNLOAD = "category." + deviceType + ".auto_download_doc.enabled";

        // Account-wide notification settings
        KEY_NOTIFICATION_SOUND = "account.notification.sound";
        KEY_NOTIFICATION_GROUP_ENABLED = "account.notifications.group.enabled";
        KEY_NOTIFICATION_GROUP_ONLY_MENTIONS = "account.notifications.group.mentions";

        // Hints
        KEY_RENAME_HINT_SHOWN = "hint.contact.rename";

        KEY_WALLPAPPER = "wallpaper.uri";
        KEY_PRIVACY = "privacy.last_seen";

    }

    public void run() {
        settingsSync = ActorSystem.system().actorOf("actor/settings", () -> new SettingsSyncActor(context()));
    }


    // Sound Effects

    public boolean isConversationTonesEnabled() {
        return getBooleanValue(KEY_SOUND_EFFECTS, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        setBooleanValue(KEY_SOUND_EFFECTS, val);
    }

    // Notifications

    public boolean isNotificationsEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_ENABLED, true);
    }

    public void changeNotificationsEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_ENABLED, val);
    }

    public boolean isNotificationSoundEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_SOUND_ENABLED, true);
    }

    public void changeNotificationSoundEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_SOUND_ENABLED, val);
    }

    public String getNotificationSound() {
        return readValue(KEY_NOTIFICATION_SOUND);
    }

    public void changeNotificationSound(String sound) {
        setStringValue(KEY_NOTIFICATION_SOUND, sound);
    }

    public String getNotificationPeerSound(Peer peer) {
        return readValue(KEY_NOTIFICATION_PEER_SOUND + getChatKey(peer));
    }

    public void changeNotificationPeerSound(Peer peer, String sound) {
        setStringValue(KEY_NOTIFICATION_PEER_SOUND + getChatKey(peer), sound);
    }

    public boolean isVibrationEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_VIBRATION, true);
    }

    public void changeNotificationVibrationEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_VIBRATION, val);
    }

    public boolean isShowNotificationsText() {
        return getBooleanValue(KEY_NOTIFICATION_TEXT, true);
    }

    public void changeShowNotificationTextEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_TEXT, val);
    }

    // Group Notifications

    public boolean isGroupNotificationsEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_GROUP_ENABLED, true);
    }

    public void changeGroupNotificationsEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_GROUP_ENABLED, val);
    }

    public boolean isGroupNotificationsOnlyMentionsEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_GROUP_ONLY_MENTIONS, false);
    }

    public void changeGroupNotificationsOnlyMentionsEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_GROUP_ONLY_MENTIONS, val);
    }

    // In-App notifications

    public boolean isInAppEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_IN_APP_ENABLED, true);
    }

    public void changeInAppEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_IN_APP_ENABLED, val);
    }

    public boolean isInAppSoundEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_IN_APP_SOUND, true);
    }

    public void changeInAppSoundEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_IN_APP_SOUND, val);
    }

    public boolean isInAppVibrationEnabled() {
        return getBooleanValue(KEY_NOTIFICATION_IN_APP_VIBRATION, true);
    }

    public void changeInAppVibrationEnabled(boolean val) {
        setBooleanValue(KEY_NOTIFICATION_IN_APP_VIBRATION, val);
    }

    // Chat settings

    public boolean isSendByEnterEnabled() {
        return getBooleanValue(KEY_CHAT_SEND_BY_ENTER, true);
    }

    public void changeSendByEnter(boolean val) {
        setBooleanValue(KEY_CHAT_SEND_BY_ENTER, val);
    }

    public boolean isMarkdownEnabled() {
        return getBooleanValue(KEY_MARKDOWN_ENABLED, false);
    }

    public void changeMarkdown(boolean val) {
        setBooleanValue(KEY_MARKDOWN_ENABLED, val);
    }

    public int getTextSize() {
        return getInt(KEY_CHAT_TEXT_SIZE, 15);
    }

    public void changeTextSize(int textSize) {
        setInt(KEY_CHAT_TEXT_SIZE, textSize);
    }

    // Auto download settings

    public boolean isImageAutoDownloadEnabled() {
        return getBooleanValue(KEY_IMAGE_AUTO_DOWNLOAD, true);
    }

    public void setImageAutoDownloadEnabled(boolean enabled) {
        setBooleanValue(KEY_IMAGE_AUTO_DOWNLOAD, enabled);
    }

    public boolean isAnimationAutoDownloadEnabled() {
        return getBooleanValue(KEY_ANIMATION_AUTO_DOWNLOAD, true);
    }

    public void setAnimationAutoDownloadEnabled(boolean enabled) {
        setBooleanValue(KEY_ANIMATION_AUTO_DOWNLOAD, enabled);
    }

    public boolean isVideoAutoDownloadEnabled() {
        return getBooleanValue(KEY_VIDEO_AUTO_DOWNLOAD, false);
    }

    public void setVideoAutoDownloadEnabled(boolean enabled) {
        setBooleanValue(KEY_VIDEO_AUTO_DOWNLOAD, enabled);
    }

    public boolean isDocAutoDownloadEnabled() {
        return getBooleanValue(KEY_DOC_AUTO_DOWNLOAD, true);
    }

    public void setDocAutoDownloadEnabled(boolean enabled) {
        setBooleanValue(KEY_DOC_AUTO_DOWNLOAD, enabled);
    }

    public boolean isAudioAutoDownloadEnabled() {
        return getBooleanValue(KEY_AUDIO_AUTO_DOWNLOAD, true);
    }

    public void setAudioAutoDownloadEnabled(boolean enabled) {
        setBooleanValue(KEY_AUDIO_AUTO_DOWNLOAD, enabled);
    }

    // Peer settings

    public boolean isNotificationsEnabled(Peer peer) {
        return getBooleanValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", true);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        setBooleanValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".enabled", val);
    }

    public String getNotificationSound(Peer peer) {
        return readValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".sound");
    }

    public void changeNotificationSound(Peer peer, String sound) {
        setStringValue(KEY_NOTIFICATION_CHAT_PREFIX + getChatKey(peer) + ".sound", sound);
    }

    // Hint

    public boolean isRenameHintShown() {
        boolean res = getBooleanValue(KEY_RENAME_HINT_SHOWN, false);
        if (!res) {
            setBooleanValue(KEY_RENAME_HINT_SHOWN, true);
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

    //Privacy
    public String getPrivacy() {
        String privacy = readValue(KEY_PRIVACY);
        return privacy != null ? privacy : "always";
    }

    public void setPrivacy(String privacy) {
        changeValue(KEY_PRIVACY, privacy);
    }

    //Animation

    public boolean isAnimationAutoPlayEnabled() {
        return getBooleanValue(KEY_ANIMATION_AUTO_PLAY, true);
    }

    public void setAnimationAutoPlayEnabled(boolean val) {
        setBooleanValue(KEY_ANIMATION_AUTO_PLAY, val);
    }

    // Common

    public boolean getBooleanValue(String key, boolean defaultVal) {
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

    public boolean getBooleanValue(String key) {
        return getBooleanValue(key, false);
    }

    public void setBooleanValue(String key, boolean val) {
        String sVal = val ? "true" : "false";
        changeValue(key, sVal);
    }

    public String getStringValue(String key, String defaultVal) {
        String sValue = readValue(key);
        if (sValue == null) {
            return defaultVal;
        }
        return sValue;
    }

    private void setInt(String key, int val) {
        changeValue(key, Integer.toString(val));
    }

    private int getInt(String key, int defaultVal) {
        String sValue = readValue(key);
        int res = defaultVal;
        if (sValue != null) {
            try {
                res = Integer.parseInt(sValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private byte[] getBytes(String key) {
        return preferences().getBytes(STORAGE_PREFIX + key);
    }

    private void setBytes(String key, byte[] val) {
        preferences().putBytes(STORAGE_PREFIX + key, val);
    }


    public String getStringValue(String key) {
        return getStringValue(key, null);
    }

    public void setStringValue(String key, String val) {
        changeValue(key, val);
    }

    // Sync methods

    private void changeValue(String key, String val) {
        String s = readValue(key);
        if (s == null && val == null) {
            return;
        }
        if (s != null && val != null && s.equals(val)) {
            return;
        }
        settingsSync.send(new SettingsSyncActor.ChangeSettings(key, val));
        onUpdatedSetting(key, val);
        notifySettingsChanged();
    }

    private String readValue(String key) {
        return preferences().getString(STORAGE_PREFIX + key);
    }

    public void onUpdatedSetting(String key, String value) {
        preferences().putString(STORAGE_PREFIX + key, value);
    }

    public void notifySettingsChanged() {
        eventBus.post(new SettingsChanged());
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

    public void resetModule() {
        // TODO: Implement
    }
}
