package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import im.actor.messenger.storage.SimpleStorage;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.users.User;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class UserModel {
    private final int id;
    private final long accessHash;
    private final long phone;

    private final ValueModel<String> name;
    private final ValueModel<Avatar> avatar;
    private final ValueModel<UserPresence> persistence;

    private final ValueModel<Boolean> isContact;

    private User raw;


    public UserModel(User user) {
        this.raw = user;

        this.id = user.getId();
        this.accessHash = user.getAccessHash();
        this.phone = user.getPhone();
        this.name = new ValueModel<String>("users." + id + ".name", raw.getName());
        this.persistence = new ValueModel<UserPresence>("users." + id + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        this.avatar = new ValueModel<Avatar>("users." + id + ".avatar", user.getAvatar());
        this.isContact = new ValueModel<Boolean>("users." + id + ".isContact", SimpleStorage.getContactsMap().contains((long) id));
    }

    public void update(User user) {
        this.raw = user;
        name.change(raw.getName());
        avatar.change(raw.getAvatar());
    }

    public User getRaw() {
        return raw;
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public long getPhone() {
        return phone;
    }

    public String getName() {
        return name.getValue();
    }

    public ValueModel<String> getNameModel() {
        return name;
    }

    public ValueModel<UserPresence> getPresence() {
        return persistence;
    }

    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    public ValueModel<Boolean> getContactModel() {
        return isContact;
    }
}
