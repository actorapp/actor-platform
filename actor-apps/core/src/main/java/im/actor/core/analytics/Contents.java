package im.actor.core.analytics;

import im.actor.core.entity.Peer;

public class Contents {

    public static final ContentPage CONTACTS = new ContentPage("contacts", "Main Contacts list", "contacts");

    public static final ContentPage RECENT = new ContentPage("recent", "Main Recent chats list", "main");

    public static final ContentPage SETTINGS = new ContentPage("settings", "Main Settings", "main");

    public static final ContentPage SETTINGS_PRIVACY = new ContentPage("settings", "Privacy Settings", "privacy");

    public static final ContentPage SETTINGS_NOTIFICATIONS = new ContentPage("settings", "Notification Settings", "notifications");

    public static ContentPage contentForChat(Peer peer) {
        return new ContentPage("conversation", "Conversation", peer.toIdString(), null);
    }

    public static ContentPage profileInfo(int uid) {
        return new ContentPage("profile_info", "User Info", "" + uid, null);
    }

    public static ContentPage groupInfo(int gid) {
        return new ContentPage("group_info", "Group Info", "" + gid, null);
    }
}