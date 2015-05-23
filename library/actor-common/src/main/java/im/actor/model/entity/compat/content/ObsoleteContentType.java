/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.compat.content;

public enum ObsoleteContentType {
    TEXT(1),
    DOCUMENT(2),
    DOCUMENT_PHOTO(3),
    DOCUMENT_VIDEO(4),
    SERVICE(5),
    SERVICE_CREATED(6),
    SERVICE_AVATAR(7),
    SERVICE_TITLE(8),
    SERVICE_ADDED(9),
    SERVICE_KICKED(10),
    SERVICE_LEAVE(11),
    SERVICE_REGISTERED(12);

    int value;

    ObsoleteContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
