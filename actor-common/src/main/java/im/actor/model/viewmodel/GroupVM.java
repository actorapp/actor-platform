package im.actor.model.viewmodel;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Group;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 23.02.15.
 */
public class GroupVM extends BaseValueModel<Group> {

    private int id;
    private long hash;
    private ValueModel<String> name;
    private ValueModel<Avatar> avatar;

    public GroupVM(Group rawObj) {
        super(rawObj);
        this.id = rawObj.getGroupId();
        this.hash = rawObj.getAccessHash();
        this.name = new ValueModel<String>("group." + id + ".title", rawObj.getTitle());
        this.avatar = new ValueModel<Avatar>("group." + id + ".avatar", rawObj.getAvatar());
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

    @Override
    protected void updateValues(Group rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getTitle());
    }
}