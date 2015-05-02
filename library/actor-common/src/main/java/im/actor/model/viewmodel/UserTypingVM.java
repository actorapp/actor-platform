/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * User's typing ViewModel
 */
public class UserTypingVM {
    private int uid;
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
