/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

public enum ContentType {
    TEXT(2), NONE(1),
    DOCUMENT(3),
    DOCUMENT_PHOTO(4),
    DOCUMENT_VIDEO(5),
    SERVICE(6),
    SERVICE_ADD(7),
    SERVICE_KICK(8),
    SERVICE_LEAVE(9),
    SERVICE_REGISTERED(10),
    SERVICE_CREATED(11),
    SERVICE_JOINED(16),
    SERVICE_TITLE(12),
    SERVICE_AVATAR(13),
    SERVICE_AVATAR_REMOVED(14),
    UNKNOWN_CONTENT(15);

    int value;

    ContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ContentType fromValue(int value) {
        switch (value) {
            default:
            case 1:
                return NONE;
            case 2:
                return TEXT;
            case 3:
                return DOCUMENT;
            case 4:
                return DOCUMENT_PHOTO;
            case 5:
                return DOCUMENT_VIDEO;
            case 6:
                return SERVICE;
            case 7:
                return SERVICE_ADD;
            case 8:
                return SERVICE_KICK;
            case 9:
                return SERVICE_LEAVE;
            case 10:
                return SERVICE_REGISTERED;
            case 11:
                return SERVICE_CREATED;
            case 12:
                return SERVICE_TITLE;
            case 13:
                return SERVICE_AVATAR;
            case 14:
                return SERVICE_AVATAR_REMOVED;
            case 16:
                return SERVICE_JOINED;
        }
    }
}
