package im.actor.model.viewmodel;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.entity.Avatar;
import im.actor.model.entity.Group;
import im.actor.model.entity.GroupMember;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 23.02.15.
 */
public class GroupVM extends BaseValueModel<Group> {

    private int id;
    private long hash;
    private long creatorId;
    private ValueModel<Avatar> avatar;
    private ValueModel<String> name;
    private ValueModel<Boolean> isMember;
    private ValueModel<List<GroupMember>> members;
    private ValueModel<Integer> presence;

    private ArrayList<ModelChangedListener<GroupVM>> listeners = new ArrayList<ModelChangedListener<GroupVM>>();

    public GroupVM(Group rawObj) {
        super(rawObj);
        this.id = rawObj.getGroupId();
        this.hash = rawObj.getAccessHash();
        this.creatorId = rawObj.getAdminId();
        this.name = new ValueModel<String>("group." + id + ".title", rawObj.getTitle());
        this.avatar = new ValueModel<Avatar>("group." + id + ".avatar", rawObj.getAvatar());
        this.isMember = new ValueModel<Boolean>("group." + id + ".isMember", rawObj.isMember());
        this.members = new ValueModel<List<GroupMember>>("group." + id + ".members", rawObj.getMembers());
        this.presence = new ValueModel<Integer>("group." + id + ".presence", 0);
    }

    public int getId() {
        return id;
    }

    public long getHash() {
        return hash;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public ValueModel<String> getName() {
        return name;
    }

    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    public ValueModel<Boolean> isMember() {
        return isMember;
    }

    public ValueModel<List<GroupMember>> getMembers() {
        return members;
    }

    public ValueModel<Integer> getPresence() {
        return presence;
    }

    @Override
    protected void updateValues(Group rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getTitle());
        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= isMember.change(rawObj.isMember());

        // TODO: Better members equals checking
        isChanged |= members.change(rawObj.getMembers());

        if (isChanged) {
            notifyChange();
        }
    }

    // We expect that subscribe will be called only on UI Thread
    public void subscribe(ModelChangedListener<GroupVM> listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    // We expect that subscribe will be called only on UI Thread
    public void unsubscribe(ModelChangedListener<GroupVM> listener) {
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThread().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<GroupVM> l : listeners.toArray(new ModelChangedListener[0])) {
                    l.onChanged(GroupVM.this);
                }
            }
        });
    }
}