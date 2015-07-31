/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.annotation.MainThread;
import im.actor.model.concurrency.AbsTimerCompat;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.entity.ContactRecord;
import im.actor.model.entity.ContactRecordType;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.generics.ArrayListUserPhone;
import im.actor.model.mvvm.generics.AvatarValueModel;
import im.actor.model.mvvm.generics.BooleanValueModel;
import im.actor.model.mvvm.generics.StringValueModel;
import im.actor.model.mvvm.generics.UserPhoneValueModel;
import im.actor.model.mvvm.generics.UserPresenceValueModel;

/**
 * User View Model
 */
public class UserVM extends BaseValueModel<User> {

    private static final long PRESENCE_UPDATE_DELAY = 60 * 1000L;

    private int id;
    private boolean isBot;
    @NotNull
    private StringValueModel name;
    @NotNull
    private AvatarValueModel avatar;
    @NotNull
    private Sex sex;
    @NotNull
    private BooleanValueModel isContact;
    @NotNull
    private UserPresenceValueModel presence;
    private AbsTimerCompat presenceTimer;

    @NotNull
    private UserPhoneValueModel phones;

    @NotNull
    private ArrayList<ModelChangedListener<UserVM>> listeners = new ArrayList<ModelChangedListener<UserVM>>();

    /**
     * <p>INTERNAL API</p>
     * Create User View Model
     *
     * @param user    Initial User value
     * @param modules im.actor.android.modules reference
     */
    public UserVM(@NotNull User user, @NotNull Modules modules) {
        super(user);

        id = user.getUid();
        sex = user.getSex();
        isBot = user.isBot();
        name = new StringValueModel("user." + id + ".name", user.getName());
        avatar = new AvatarValueModel("user." + id + ".avatar", user.getAvatar());
        isContact = new BooleanValueModel("user." + id + ".contact", modules.getContactsModule().isUserContact(id));
        presence = new UserPresenceValueModel("user." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        phones = new UserPhoneValueModel("user." + id + ".phones", buildPhones(user.getRecords()));

        // Notify about presence change every minute as text representation can change
        presenceTimer = Environment.createTimer(new Runnable() {
            @Override
            public void run() {
                presence.forceNotify();
                presenceTimer.schedule(PRESENCE_UPDATE_DELAY);
            }
        });
        presenceTimer.schedule(PRESENCE_UPDATE_DELAY);
    }

    @Override
    protected void updateValues(@NotNull User rawObj) {
        boolean isChanged = name.change(rawObj.getName());
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
    @ObjectiveCName("getId")
    public int getId() {
        return id;
    }

    /**
     * Is User actually bot
     *
     * @return is User bot
     */
    @ObjectiveCName("isBot")
    public boolean isBot() {
        return isBot;
    }

    /**
     * Get User Name Value Model
     *
     * @return ValueModel of String
     */
    @NotNull
    @ObjectiveCName("getNameModel")
    public StringValueModel getName() {
        return name;
    }

    /**
     * Get User Avatar Value Model
     *
     * @return ValueModel of Avatar
     */
    @NotNull
    @ObjectiveCName("getAvatarModel")
    public AvatarValueModel getAvatar() {
        return avatar;
    }

    /**
     * Get User Sex
     *
     * @return User Sex
     */
    @NotNull
    @ObjectiveCName("getSex")
    public Sex getSex() {
        return sex;
    }

    /**
     * Get ValueModel of flag if user is in contact list
     *
     * @return ValueModel of Boolean
     */
    @NotNull
    @ObjectiveCName("isContactModel")
    public BooleanValueModel isContact() {
        return isContact;
    }

    /**
     * Get ValueModel of User Presence
     *
     * @return ValueModel of UserPresence
     */
    @NotNull
    @ObjectiveCName("getPresenceModel")
    public UserPresenceValueModel getPresence() {
        return presence;
    }

    /**
     * Get Users Phone numbers
     *
     * @return ValueModel of ArrayList of UserPhone
     */
    @NotNull
    @ObjectiveCName("getPhonesModel")
    public UserPhoneValueModel getPhones() {
        return phones;
    }

    @NotNull
    private ArrayListUserPhone buildPhones(@NotNull List<ContactRecord> records) {
        ArrayListUserPhone res = new ArrayListUserPhone();
        for (ContactRecord r : records) {
            if (r.getRecordType() == ContactRecordType.PHONE) {
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
    @ObjectiveCName("subscribeWithListener:")
    public void subscribe(@NotNull ModelChangedListener<UserVM> listener) {
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
    @ObjectiveCName("unsubscribeWithListener:")
    public void unsubscribe(@NotNull ModelChangedListener<UserVM> listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThreadProvider().postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<UserVM> l : listeners.toArray(new ModelChangedListener[listeners.size()])) {
                    l.onChanged(UserVM.this);
                }
            }
        });
    }
}
