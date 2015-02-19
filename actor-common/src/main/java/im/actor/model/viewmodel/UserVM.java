package im.actor.model.viewmodel;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.mvvm.BaseValueModel;
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

    public UserVM(User user) {
        super(user);

        id = user.getUid();
        hash = user.getAccessHash();
        sex = user.getSex();
        name = new ValueModel<String>("user." + id + ".name", user.getName());
        avatar = new ValueModel<Avatar>("user." + id + ".avatar", user.getAvatar());
    }

    @Override
    protected void updateValues(User rawObj) {
        name.change(rawObj.getName());
        avatar.change(rawObj.getAvatar());
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
}
