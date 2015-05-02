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

    private static final String KEY_NOTIFICATION_TONES = "app.tones_enabled";

    private static final String KEY_NOTIFICATION_SOUND = "sync.notification.sound.enabled";
    private static final String KEY_NOTIFICATION_VIBRATION = "sync.notification.vibration.enabled";
    private static final String KEY_NOTIFICATION_TEXT = "sync.notification.show_text";
    private static final String KEY_NOTIFICATION_CHAT = "sync.notification.chat.";

    private static final String KEY_CHAT_SEND_BY_ENTER = "app.send_by_enter";

    private ActorRef settingsSync;

    public Settings(Modules modules) {
        super(modules);
    }

    public void run() {
        settingsSync = ActorSystem.system().actorOf(Props.create(SettingsSyncActor.class, new ActorCreator<SettingsSyncActor>() {
            @Override
            public SettingsSyncActor create() {
                return new SettingsSyncActor(modules());
            }
        }), "actor/settings");
    }

    // Notifications

    public boolean isConversationTonesEnabled() {
        return loadValue(KEY_NOTIFICATION_TONES, true);
    }

    public void changeConversationTonesEnabled(boolean val) {
        saveValue(KEY_NOTIFICATION_TONES, val);
    }

    public boolean isNotificationSoundEnabled() {
        return loadValue(KEY_NOTIFICATION_SOUND, true);
    }

    public void changeNotificationSoundEnabled(boolean val) {
        saveValue(KEY_NOTIFICATION_SOUND, val);
    }

    public boolean isVibrationEnabled() {
        return loadValue(KEY_NOTIFICATION_VIBRATION, true);
    }

    public void changeNotificationVibrationEnabled(boolean val) {
        saveValue(KEY_NOTIFICATION_VIBRATION, val);
    }

    public boolean isShowNotificationsText() {
        return loadValue(KEY_NOTIFICATION_TEXT, true);
    }

    public void changeShowNotificationTextEnabled(boolean val) {
        saveValue(KEY_NOTIFICATION_TEXT, val);
    }

    // Chat settings

    public boolean isSendByEnterEnabled() {
        return loadValue(KEY_CHAT_SEND_BY_ENTER, true);
    }

    public void changeSendByEnter(boolean val) {
        saveValue(KEY_CHAT_SEND_BY_ENTER, val);
    }

    // Peer settings

    public boolean isNotificationsEnabled(Peer peer) {
        return loadValue(KEY_NOTIFICATION_CHAT + getChatKey(peer) + ".enabled", true);
    }

    public void changeNotificationsEnabled(Peer peer, boolean val) {
        saveValue(KEY_NOTIFICATION_CHAT + getChatKey(peer) + ".enabled", val);
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
        String sValue = preferences().getString(key);
        if ("true".equals(sValue)) {
            return true;
        } else if ("false".equals(sValue)) {
            return true;
        } else {
            return defaultVal;
        }
    }

    private void saveValue(String key, boolean val) {
        String sVal = val ? "true" : "false";
        preferences().putString(key, sVal);
        settingsSync.send(new SettingsSyncActor.ChangeSettings(key, sVal));
    }
}
