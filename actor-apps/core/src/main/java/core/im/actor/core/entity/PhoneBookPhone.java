/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

public class PhoneBookPhone {
    private long id;
    private long number;

    public PhoneBookPhone(long id, long number) {
        this.id = id;
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public long getNumber() {
        return number;
    }
}
