package im.actor.core.analytics;

import im.actor.core.api.ApiStringValue;
import im.actor.core.entity.Peer;

public class AllEvents {


    public static Event APP_VISIBLE(boolean isVisible) {
        return new Event("app_visible", isVisible ? "visible" : "invisible", "App Visible changed", null);
    }

    /**
     * Authentication pages and actions
     */
    public static class Auth {

        // Auth Phone input

        public static final Page AUTH_PHONE = new Page("auth", "Auth Phone number", "phone");

        public static final Page AUTH_PICK_COUNTRY = new Page("auth", "Auth Pick country", "country");

        public static Event AUTH_COUNTRY_PICKED(String iso2) {
            if (iso2 != null) {
                return new Event("auth", "country_picked", "Country Picked", new ApiStringValue(iso2.toLowerCase()));
            } else {
                return new Event("auth", "country_picked", "Country Picked", null);
            }
        }

        public static Event AUTH_PHONE_TYPED(String value) {
            return new Event("auth", "phone_type", "Phone typing", new ApiStringValue(value));
        }

        // Auth Code

        public static final Page AUTH_CODE = new Page("auth", "Auth code enter", "code");

        public static Event AUTH_CODE_TYPED(String value) {
            return new Event("auth", "code_type", "Code typing", new ApiStringValue(value));
        }

        // Auth SignUp

        public static final Page AUTH_SIGNUP = new Page("auth", "Auth Signup", "signup");

        public static Event AUTH_SIGNUP_NAME_TYPED(String value) {
            return new Event("auth", "name_type", "Name typing", new ApiStringValue(value));
        }
    }

    /**
     * Main page tracking
     */
    public static class Main {

        public static final Page CONTACTS = new Page("contacts", "Main Contacts list", "contacts");

        public static final Page RECENT = new Page("recent", "Main Recent chats list", "main");

        public static final Page SETTINGS = new Page("settings", "Main Settings", "main");
    }

    /**
     * Tracking settings page
     */
    public static class Settings {

        public static final Page PRIVACY = new Page("settings", "Privacy Settings", "privacy");

        public static final Page NOTIFICATIONS = new Page("settings", "Notification Settings", "notifications");
    }

    /**
     * Tracking chat pages
     */
    public static class Chat {

        public static Page view(Peer peer) {
            return new Page("conversation", "Conversation", peer.toIdString(), null);
        }

        public static Event MESSAGE_SENT(Peer peer) {
            return new Event("message_sent", peer.toIdString(), "Text message sent", null);
        }

        public static Event DOCUMENT_SENT(Peer peer) {
            return new Event("document_sent", peer.toIdString(), "Document message sent", null);
        }

        public static Event PICTURE_SENT(Peer peer) {
            return new Event("document_sent", peer.toIdString(), "Picture message sent", null);
        }
    }

    /**
     * Tracking user profile pages
     */
    public static class Profile {
        public static Page view(int uid) {
            return new Page("profile_info", "User Info", "" + uid, null);
        }
    }

    /**
     * Tracking group pages
     */
    public static class Group {
        public static Page view(int gid) {
            return new Page("group_info", "Group Info", "" + gid, null);
        }
    }
}