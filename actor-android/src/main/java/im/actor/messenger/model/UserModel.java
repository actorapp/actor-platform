package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class UserModel {
    private final int uid;
    private final long accessHash;

    private final ValueModel<String> name;
    private final ValueModel<Avatar> avatar;
    private final ValueModel<UserPresence> persistence;

    private User raw;

    public UserModel(User user) {
        this.raw = user;

        this.uid = user.getUid();
        this.accessHash = user.getAccessHash();

        this.name = new ValueModel<String>("users." + uid + ".name", raw.getName());
        this.persistence = new ValueModel<UserPresence>("users." + uid + ".presence", new UserPresence(UserPresence.State.UNKNOWN));
        this.avatar = new ValueModel<Avatar>("users." + uid + ".avatar", user.getAvatar());
    }

    public void update(User user) {
        this.raw = user;
        name.change(raw.getName());
        avatar.change(raw.getAvatar());
    }

    public int getId() {
        return uid;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public String getName() {
        return name.getValue();
    }

    public Sex getSex() {
        return raw.getSex();
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
}
