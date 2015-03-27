package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class UserTypingVM {
    private int uid;
    private ValueModel<Boolean> userTyping;

    public UserTypingVM(int uid) {
        this.uid = uid;
        this.userTyping = new ValueModel<Boolean>("user." + uid + ".typing", false);
    }

    public void onTypingStart() {
        userTyping.change(true);
    }

    public void onTypingEnd() {
        userTyping.change(false);
    }

    public ValueModel<Boolean> getTyping() {
        return userTyping;
    }
}
