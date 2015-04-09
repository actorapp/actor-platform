package im.actor.model.viewmodel;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.model.annotation.MainThread;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Group;
import im.actor.model.entity.GroupMember;
import im.actor.model.mvvm.BaseValueModel;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Group View Model
 */
public class GroupVM extends BaseValueModel<Group> {

    private int id;
    private long hash;
    private long creatorId;
    private ValueModel<Avatar> avatar;
    private ValueModel<String> name;
    private ValueModel<Boolean> isMember;
    private ValueModel<HashSet<GroupMember>> members;
    private ValueModel<Integer> presence;

    private ArrayList<ModelChangedListener<GroupVM>> listeners = new ArrayList<ModelChangedListener<GroupVM>>();

    /**
     * <p>INTERNAL API</p>
     * Create Group View Model
     *
     * @param rawObj initial value of Group
     */
    public GroupVM(Group rawObj) {
        super(rawObj);
        this.id = rawObj.getGroupId();
        this.hash = rawObj.getAccessHash();
        this.creatorId = rawObj.getAdminId();
        this.name = new ValueModel<String>("group." + id + ".title", rawObj.getTitle());
        this.avatar = new ValueModel<Avatar>("group." + id + ".avatar", rawObj.getAvatar());
        this.isMember = new ValueModel<Boolean>("group." + id + ".isMember", rawObj.isMember());
        this.members = new ValueModel<HashSet<GroupMember>>("group." + id + ".members", new HashSet<GroupMember>(rawObj.getMembers()));
        this.presence = new ValueModel<Integer>("group." + id + ".presence", 0);
    }

    /**
     * Get Group Id
     *
     * @return Group Id
     */
    public int getId() {
        return id;
    }

    /**
     * Get Group Access Hash
     *
     * @return Group Access Hash
     */
    public long getHash() {
        return hash;
    }

    /**
     * Get Group creator user id
     *
     * @return creator user id
     */
    public long getCreatorId() {
        return creatorId;
    }

    /**
     * Get Group members count
     *
     * @return members count
     */
    public int getMembersCount() {
        return members.get().size();
    }

    /**
     * Get Name Value Model
     *
     * @return Value Model of String
     */
    public ValueModel<String> getName() {
        return name;
    }

    /**
     * Get Avatar Value Model
     *
     * @return Value Model of Avatar
     */
    public ValueModel<Avatar> getAvatar() {
        return avatar;
    }

    /**
     * Get membership Value Model
     *
     * @return Value Model of Boolean
     */
    public ValueModel<Boolean> isMember() {
        return isMember;
    }

    /**
     * Get members Value Model
     *
     * @return Value Model of HashSet of GroupMember
     */
    public ValueModel<HashSet<GroupMember>> getMembers() {
        return members;
    }

    /**
     * Get Online Value Model
     *
     * @return Value Model of Integer
     */
    public ValueModel<Integer> getPresence() {
        return presence;
    }

    @Override
    protected void updateValues(Group rawObj) {
        boolean isChanged = false;
        isChanged |= name.change(rawObj.getTitle());
        isChanged |= avatar.change(rawObj.getAvatar());
        isChanged |= isMember.change(rawObj.isMember());
        isChanged |= members.change(new HashSet<GroupMember>(rawObj.getMembers()));

        if (isChanged) {
            notifyChange();
        }
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    public void subscribe(ModelChangedListener<GroupVM> listener) {
        MVVMEngine.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Unsubscribe from GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    public void unsubscribe(ModelChangedListener<GroupVM> listener) {
        MVVMEngine.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        MVVMEngine.getMainThreadProvider().postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<GroupVM> l : listeners.toArray(new ModelChangedListener[0])) {
                    l.onChanged(GroupVM.this);
                }
            }
        });
    }
}