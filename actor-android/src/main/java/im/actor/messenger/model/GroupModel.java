package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.groups.GroupInfo;
import im.actor.messenger.storage.scheme.groups.GroupMember;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.util.BoxUtil;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class GroupModel {
    private GroupInfo raw;
    private ValueModel<GroupState> stateModel;
    private ValueModel<GroupMember[]> usersModel;
    private ValueModel<int[]> onlineModel;
    private ValueModel<String> titleModel;
    private ValueModel<Avatar> avatarModel;

    public GroupModel(GroupInfo raw) {
        this.raw = raw;
        titleModel = new ValueModel<String>("group" + raw.getGroupId() + ".title", raw.getTitle());
        avatarModel = new ValueModel<Avatar>("group" + raw.getGroupId() + ".avatar", raw.getAvatar());
        stateModel = new ValueModel<GroupState>("group" + raw.getGroupId() + ".state", raw.getGroupState());
        usersModel = new ValueModel<GroupMember[]>("group" + raw.getGroupId() + ".users", raw.getMembers().toArray(new GroupMember[0]));
        onlineModel = new ValueModel<int[]>("group" + raw.getGroupId() + ".online", new int[]{raw.getMembers().size()});
    }

    public int getChatId() {
        return raw.getGroupId();
    }

    public long getAccessHash() {
        return raw.getAccessHash();
    }

    public String getTitle() {
        return raw.getTitle();
    }

    public int getUsersCount() {
        return raw.getMembers().size();
    }

    public GroupMember[] getUsers() {
        return raw.getMembers().toArray(new GroupMember[raw.getMembers().size()]);
    }

    public GroupInfo getRaw() {
        return raw;
    }

    public GroupState getState() {
        return stateModel.getValue();
    }

    public ValueModel<GroupState> getStateModel() {
        return stateModel;
    }

    public ValueModel<GroupMember[]> getUsersModel() {
        return usersModel;
    }

    public ValueModel<int[]> getOnlineModel() {
        return onlineModel;
    }

    public ValueModel<String> getTitleModel() {
        return titleModel;
    }

    public ValueModel<Avatar> getAvatarModel() {
        return avatarModel;
    }


    public synchronized void update(GroupInfo raw) {
        this.raw = raw;

        avatarModel.change(raw.getAvatar());
        titleModel.change(raw.getTitle());
        stateModel.change(raw.getGroupState());
        usersModel.change(raw.getMembers().toArray(new GroupMember[0]));
        int[] onlineVal = onlineModel.getValue();
        if (onlineVal.length > 1) {
            onlineModel.change(new int[]{raw.getMembers().size(), onlineVal[1]});
        } else {
            onlineModel.change(new int[]{raw.getMembers().size()});
        }
    }

    public synchronized void updateOnline(int count) {
        onlineModel.change(new int[]{raw.getMembers().size(), count});
    }
}
