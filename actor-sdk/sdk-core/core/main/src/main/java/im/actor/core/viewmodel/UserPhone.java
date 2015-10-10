/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

/**
 * User's Phone record
 */
public class UserPhone {
    @Property("nonatomic, readonly")
    private long phone;
    @Property("nonatomic, readonly")
    private String title;

    /**
     * <p>INTERNAL API</p>
     * Create User phone
     *
     * @param phone phone in international format
     * @param title title of phone
     */
    public UserPhone(long phone, String title) {
        this.phone = phone;
        this.title = title;
    }

    /**
     * Get Phone number
     *
     * @return phone number in international format
     */
    public long getPhone() {
        return phone;
    }

    /**
     * Get Phone title
     *
     * @return phone title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPhone userPhone = (UserPhone) o;

        if (phone != userPhone.phone) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (phone ^ (phone >>> 32));
    }
}
