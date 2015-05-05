/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

public enum Sex {
    UNKNOWN(1), MALE(2), FEMALE(3);

    private int value;

    Sex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Sex fromValue(int value) {
        switch (value) {
            default:
            case 1:
                return UNKNOWN;
            case 2:
                return MALE;
            case 3:
                return FEMALE;
        }
    }
}
