/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

public class PhoneBookPhone {
    @Property("readonly, nonatomic")
    private long id;
    @Property("readonly, nonatomic")
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
