/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.MainThread;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * User View Model
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
    private ValueModel<ArrayList<UserPhone>> phones;

    /**
     * <p>INTERNAL API</p>
     * Create User View Model
     *
     * @param user    Initial User value
     * @param modules modules reference
     */
    public UserVM(User user, Modules modules) {
        super(user);

        id = user.getUid();
        hash = user.getAccessHash();
        sex = user.getSex();
        name = new ValueModel<String>("user." + id + ".name", user.getName());
        avatar = new ValueModel<Avatar>("user." + id + ".avatar", user.getAvatar());
        isContact = new ValueModel<Boolean>("user." + id + ".contact", modules.getContactsModule().isUserContact(id));
        presence = new ValueModel<UserPresence>("user." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        phones = new ValueModel<ArrayList<UserPhone>>("user." + id + ".phones", buildPhones(user.getRecords()));
    }

    @Override
    protected void updateValues(User rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getName());
        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= phones.change(buildPhones(rawObj.getRecords()));

        if (isChanged) {
            notifyChange();
        }
    }

    /**
     * Get User Id
     *
     * @return user Id
     */
    public int getId() {
        return id;
    }

    /**
     * Get User Access Hash
     *
     * @return User Access Hash
     */
    public long getHash() {
        return hash;
    }

    /**
     * Get User Name Value Model
     *
     * @return ValueModel of String
     */
    public ValueModel<String> getName() {
        return name;
    }

    /**
     * Get User Avatar Value Model
     *
     * @return ValueModel of Avatar
     */
    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    /**
     * Get User Sex
     *
     * @return User Sex
     */
    public Sex getSex() {
        return sex;
    }

    /**
     * Get ValueModel of flag if user is in contact list
     *
     * @return ValueModel of Boolean
     */
    public ValueModel<Boolean> isContact() {
        return isContact;
    }

    /**
     * Get ValueModel of User Presence
     *
     * @return ValueModel of UserPresence
     */
    public ValueModel<UserPresence> getPresence() {
        return presence;
    }

    /**
     * Get Users Phone numbers
     *
     * @return ValueModel of ArrayList of UserPhone
     */
    public ValueModel<ArrayList<UserPhone>> getPhones() {
        return phones;
    }

    private ArrayList<UserPhone> buildPhones(List<ContactRecord> records) {
        ArrayList<UserPhone> res = new ArrayList<UserPhone>();
        for (ContactRecord r : records) {
            if (r.getRecordType() == ContactRecord.TYPE_PHONE) {
                res.add(new UserPhone(Long.parseLong(r.getRecordData()), r.getRecordTitle()));
            }
        }
        return res;
    }

    /**
     * Subscribe to UserVM updates
     *
     * @param listener UserVM changed listener
     */
    @MainThread
    public void subscribe(ModelChangedListener<UserVM> listener) {
        MVVMEngine.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Unsubscribe from UserVM
     *
     * @param listener UserVM changed listener
     */
    @MainThread
    public void unsubscribe(ModelChangedListener<UserVM> listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThreadProvider().postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<UserVM> l : listeners.toArray(new ModelChangedListener[0])) {
                    l.onChanged(UserVM.this);
                }
            }
        });
    }
}
