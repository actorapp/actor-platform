/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

public enum MessageState {
    PENDING(1),
    SENT(2),
    ERROR(5),
    UNKNOWN(6);

    int value;

    MessageState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageState fromValue(int value) {
        switch (value) {
            case 1:
                return PENDING;
            case 2:
            case 3:
            case 4:
                return SENT;
            case 5:
                return ERROR;
            default:
            case 6:
                return UNKNOWN;
        }
    }
}
