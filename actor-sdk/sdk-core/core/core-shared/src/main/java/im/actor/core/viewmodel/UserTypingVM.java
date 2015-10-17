/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.mvvm.ValueModel;

/**
 * User's typing ViewModel
 */
public class UserTypingVM {
    @Property("nonatomic, readonly")
    private int uid;
    @Property("nonatomic, readonly")
    private ValueModel<Boolean> userTyping;

    public UserTypingVM(int uid) {
        this.uid = uid;
        this.userTyping = new ValueModel<Boolean>("user." + uid + ".typing", false);
    }

    public int getUid() {
        return uid;
    }

    public ValueModel<Boolean> getTyping() {
        return userTyping;
    }
}
