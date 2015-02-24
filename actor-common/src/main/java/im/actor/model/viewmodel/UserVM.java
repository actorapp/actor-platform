package im.actor.model.viewmodel;

import java.util.ArrayList;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class UserVM extends BaseValueModel<User> {
    private int id;
    private long hash;
    private ValueModel<String> name;
    private ValueModel<Avatar> avatar;
    private Sex sex;
    private ValueModel<Boolean> isContact;
    private ValueModel<UserPresence> presence;
    private ArrayList<ModelChangedListener<UserVM>> listeners = new ArrayList<ModelChangedListener<UserVM>>();

    public UserVM(User user) {
        super(user);

        id = user.getUid();
        hash = user.getAccessHash();
        sex = user.getSex();
        name = new ValueModel<String>("user." + id + ".name", user.getName());
        avatar = new ValueModel<Avatar>("user." + id + ".avatar", user.getAvatar());
        isContact = new ValueModel<Boolean>("user." + id + ".contact", false);
        presence = new ValueModel<UserPresence>("user." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
    }

    @Override
    protected void updateValues(User rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getName());
        isChanged |= avatar.change(rawObj.getAvatar());
        if (isChanged) {
            notifyChange();
        }
    }

    public int getId() {
        return id;
    }

    public long getHash() {
        return hash;
    }

    public ValueModel<String> getName() {
        return name;
    }

    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    public Sex getSex() {
        return sex;
    }

    public ValueModel<Boolean> isContact() {
        return isContact;
    }

    public ValueModel<UserPresence> getPresence() {
        return presence;
    }

    // We expect that subscribe will be called only on UI Thread
    public void subscribe(ModelChangedListener<UserVM> listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    // We expect that subscribe will be called only on UI Thread
    public void unsubscribe(ModelChangedListener<UserVM> listener) {
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<UserVM> l : listeners.toArray(new ModelChangedListener[0])) {
                    l.onChanged(UserVM.this);
                }
            }
        });
    }
}
