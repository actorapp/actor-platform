/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.ContactRecord;
import im.actor.core.entity.ContactRecordType;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.generics.ArrayListUserPhone;
import im.actor.core.viewmodel.generics.AvatarValueModel;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.StringValueModel;
import im.actor.core.viewmodel.generics.UserPhoneValueModel;
import im.actor.core.viewmodel.generics.UserPresenceValueModel;
import im.actor.runtime.Runtime;
import im.actor.runtime.annotations.MainThread;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ModelChangedListener;
import im.actor.runtime.mvvm.ValueModelCreator;
import im.actor.runtime.threading.AbsTimerCompat;

/**
 * User View Model
 */
public class UserVM extends BaseValueModel<User> {

    private static final long PRESENCE_UPDATE_DELAY = 60 * 1000L;

    public static ValueModelCreator<User, UserVM> CREATOR(final ModuleContext modules) {
        return new ValueModelCreator<User, UserVM>() {
            @Override
            public UserVM create(User baseValue) {
                return new UserVM(baseValue, modules);
            }
        };
    }

    private int id;
    private boolean isBot;
    @NotNull
    private StringValueModel name;
    @NotNull
    private StringValueModel localName;
    @NotNull
    private StringValueModel serverName;
    @NotNull
    private StringValueModel nick;
    @NotNull
    private StringValueModel about;
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
    public UserVM(@NotNull User user, @NotNull ModuleContext modules) {
        super(user);

        id = user.getUid();
        sex = user.getSex();
        isBot = user.isBot();
        name = new StringValueModel("user." + id + ".name", user.getName());
        localName = new StringValueModel("user." + id + ".local_name", user.getLocalName());
        serverName = new StringValueModel("user." + id + ".server_name", user.getServerName());
        nick = new StringValueModel("user." + id + ".nick", user.getNick());
        about = new StringValueModel("user." + id + ".about", user.getAbout());
        avatar = new AvatarValueModel("user." + id + ".avatar", user.getAvatar());
        isContact = new BooleanValueModel("user." + id + ".contact", modules.getContactsModule().isUserContact(id));
        presence = new UserPresenceValueModel("user." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        phones = new UserPhoneValueModel("user." + id + ".phones", buildPhones(user.getRecords()));

        // Notify about presence change every minute as text representation can change
        presenceTimer = im.actor.runtime.Runtime.createTimer(new Runnable() {
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
        isChanged |= localName.change(rawObj.getLocalName());
        isChanged |= serverName.change(rawObj.getServerName());
        isChanged |= nick.change(rawObj.getNick());
        isChanged |= about.change(rawObj.getAbout());
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
     * Get User Local Name Value Model
     *
     * @return ValueModel of String
     */
    @NotNull
    @ObjectiveCName("getLocalNameModel")
    public StringValueModel getLocalName() {
        return localName;
    }

    /**
     * Get User Server Name Value Model
     *
     * @return ValueModel of String
     */
    @NotNull
    @ObjectiveCName("getServerNameModel")
    public StringValueModel getServerName() {
        return serverName;
    }

    /**
     * Get User nick Value Model
     *
     * @return ValueModel of String
     */
    @NotNull
    @ObjectiveCName("getNickModel")
    public StringValueModel getNick() {
        return nick;
    }

    /**
     * Get User about Value Model
     *
     * @return ValueModel of String
     */
    @NotNull
    @ObjectiveCName("getAboutModel")
    public StringValueModel getAbout() {
        return about;
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

    /**
     * Subscribe to UserVM updates
     *
     * @param listener UserVM changed listener
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:")
    public void subscribe(@NotNull ModelChangedListener<UserVM> listener) {
        Runtime.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Subscribe to UserVM updates
     *
     * @param listener UserVM changed listener
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:withNotify:")
    public void subscribe(@NotNull ModelChangedListener<UserVM> listener, boolean notify) {
        Runtime.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (notify) {
            listener.onChanged(this);
        }
    }

    /**
     * Unsubscribe from UserVM
     *
     * @param listener UserVM changed listener
     */
    @MainThread
    @ObjectiveCName("unsubscribeWithListener:")
    public void unsubscribe(@NotNull ModelChangedListener<UserVM> listener) {
        Runtime.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<UserVM> l : listeners.toArray(new ModelChangedListener[listeners.size()])) {
                    l.onChanged(UserVM.this);
                }
            }
        });
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
}
