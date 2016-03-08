/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.ApiSex;

public enum Sex {
    UNKNOWN(1), MALE(2), FEMALE(3);

    private int value;

    Sex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NotNull
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

    public ApiSex toApi() {
        switch (this) {
            case FEMALE:
                return ApiSex.FEMALE;
            case MALE:
                return ApiSex.MALE;
            default:
            case UNKNOWN:
                return ApiSex.UNKNOWN;
        }
    }
}
