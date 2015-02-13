package im.actor.messenger.core.actors.groups;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedFuture;

import im.actor.api.scheme.Avatar;
import im.actor.api.scheme.Group;
import im.actor.api.scheme.GroupOutPeer;
import im.actor.api.scheme.Member;
import im.actor.api.scheme.User;
import im.actor.api.scheme.UserOutPeer;
import im.actor.api.scheme.rpc.ResponseCreateGroup;
import im.actor.api.scheme.rpc.ResponseSeqDate;
import im.actor.api.scheme.updates.UpdateGroupInvite;
import im.actor.api.scheme.updates.UpdateGroupTitleChanged;
import im.actor.api.scheme.updates.UpdateGroupUserAdded;
import im.actor.api.scheme.updates.UpdateGroupUserKick;
import im.actor.messenger.api.ApiConversion;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.chat.ChatActionsActor;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.chat.DialogsActor;
import im.actor.messenger.model.*;
import im.actor.messenger.storage.scheme.groups.GroupInfo;
import im.actor.messenger.storage.scheme.groups.GroupMember;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.storage.scheme.messages.types.GroupAdd;
import im.actor.messenger.storage.scheme.messages.types.GroupAvatar;
import im.actor.messenger.storage.scheme.messages.types.GroupCreated;
import im.actor.messenger.storage.scheme.messages.types.GroupKick;
import im.actor.messenger.storage.scheme.messages.types.GroupLeave;
import im.actor.messenger.storage.scheme.messages.types.GroupTitle;
import im.actor.messenger.util.RandomUtil;

import java.util.*;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class GroupsActor extends TypedActor<GroupsInt> implements GroupsInt {

    private static final TypedActorHolder<GroupsInt> holder = new TypedActorHolder<GroupsInt>(GroupsInt.class,
            GroupsActor.class, "updates", "group_updates");

    public static GroupsInt groupUpdates() {
        return holder.get();
    }

    public GroupsActor() {
        super(GroupsInt.class);
    }

    @Override
    public Future<Boolean> onUpdateGroups(List<Group> groupsList) {
        for (Group g : groupsList) {
            GroupModel groupModel = groups().get(g.getId());
            GroupInfo groupInfo = ApiConversion.convert(g);
            if (groupModel != null) {
                boolean isChanged = false;

                if (!areSame(groupInfo.getAvatar(), groupModel.getRaw().getAvatar())) {
                    DialogsActor.dialogs().onGroupChangedAvatar(g.getId(), groupInfo.getAvatar());
                    isChanged = true;
                }

                if (!groupInfo.getTitle().equals(groupModel.getRaw().getTitle())) {
                    DialogsActor.dialogs().onGroupChangedTitle(g.getId(), groupInfo.getTitle());
                    isChanged = true;
                }

                if (!areSame(groupInfo.getMembers(), groupModel.getRaw().getMembers())) {
                    isChanged = true;
                }

                if (groupInfo.getGroupState() != groupModel.getRaw().getGroupState()) {
                    isChanged = true;
                }

                if (isChanged) {
                    groups().put(g.getId(), groupInfo);
                }
            } else {
                groups().put(g.getId(), groupInfo);
            }
        }
        return result(true);
    }

    @Override
    public void onInvite(int chatId, long rid, int inviterId, long date) {
        GroupModel groupModel = groups().get(chatId);

        if (groupModel == null) {
            return;
        }

        if (groupModel.getState() == GroupState.DELETED_PENDING) {
            return;
        }

        groups().put(chatId, groupModel.getRaw().updateState(GroupState.JOINED));

        if (inviterId == myUid()) {
            ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                    .onInMessage(rid, inviterId, date, new GroupCreated(groupModel.getTitle()));
        } else {
            ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                    .onInMessage(rid, inviterId, date, new GroupAdd(myUid()));
        }
    }

    @Override
    public void onTitleChanged(int chatId, long rid, int uid, String title, long date) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        if (!groupInfo.getTitle().equals(title)) {
            groups().put(chatId, groupInfo.getRaw().editTitle(title));
        }

        DialogsActor.dialogs().onGroupChangedTitle(chatId, title);

        ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                .onInMessage(rid, uid, date, new GroupTitle(title));
    }

    @Override
    public void onAvatarChanged(int chatId, long rid, int uid, Avatar avatar, long date) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        im.actor.messenger.storage.scheme.avatar.Avatar avatar1 = ApiConversion.convert(avatar);

        if (!areSame(avatar1, groupInfo.getRaw().getAvatar())) {
            groups().put(chatId, groupInfo.getRaw().editAvatar(avatar1));
        }

        DialogsActor.dialogs().onGroupChangedAvatar(chatId, avatar1);

        ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                .onInMessage(rid, uid, date, new GroupAvatar(avatar1));
    }

    @Override
    public void onUserLeave(int chatId, long rid, int uid, long date) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        if (uid == myUid()) {
            groups().put(chatId, groupInfo.getRaw()
                    .updateState(GroupState.DELETED)
                    .updateMembers(new ArrayList<GroupMember>()));
        } else {
            groups().put(chatId, groupInfo.getRaw()
                    .updateMembers(removeItem(groupInfo.getRaw().getMembers(), uid)));
        }

        ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                .onInMessage(rid, uid, date, new GroupLeave());
    }

    @Override
    public void onUserKicked(int chatId, long rid, int uid, int kicker, long date) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        if (uid == myUid()) {
            groups().put(chatId, groupInfo.getRaw()
                    .updateState(GroupState.KICKED)
                    .updateMembers(new ArrayList<GroupMember>()));
        } else {
            groups().put(chatId,
                    groupInfo.getRaw()
                            .updateMembers(removeItem(groupInfo.getRaw().getMembers(), uid)));
        }

        ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                .onInMessage(rid, kicker, date, new GroupKick(uid));
    }

    @Override
    public void onUserAdded(int chatId, long rid, int uid, int adder, long date) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        groups().put(chatId,
                groupInfo.getRaw()
                        .updateMembers(addItem(groupInfo.getRaw().getMembers(), uid, adder, date)));

        ConversationActor.conv(DialogType.TYPE_GROUP, chatId)
                .onInMessage(rid, adder, date, new GroupAdd(uid));
    }

    @Override
    public Future<Integer> createGroup(final String title, int[] uid) {
        final TypedFuture future = future();
        final long rid = RandomUtil.randomId();
        List<UserOutPeer> peers = new ArrayList<UserOutPeer>();
        for (int u : uid) {
            UserModel user = users().get(u);
            peers.add(new UserOutPeer(u, user.getAccessHash()));
        }
        ask(requests().createGroup(rid, title, peers), new FutureCallback<ResponseCreateGroup>() {
            @Override
            public void onResult(ResponseCreateGroup result) {

                List<Group> groups = new ArrayList<Group>();
                List<Member> members = new ArrayList<Member>();
                for (Integer i : result.getUsers()) {
                    members.add(new Member(i, myUid(), result.getDate()));
                }
                groups.add(new Group(result.getGroupPeer().getGroupId(), result.getGroupPeer().getAccessHash(),
                        title, null, true, myUid(), members, result.getDate()));

                // Small hack for saving group info out of order
                if (groups().get(result.getGroupPeer().getGroupId()) == null) {
                    onUpdateGroups(groups);
                }

                future.doComplete(result.getGroupPeer().getGroupId());

                system().actorOf(SequenceActor.sequence()).send(
                        new SequenceActor.SeqFatUpdate(result.getSeq(), result.getState(),
                                new UpdateGroupInvite(result.getGroupPeer().getGroupId(), rid,
                                        myUid(), result.getDate()),
                                new ArrayList<User>(),
                                groups)
                );
            }

            @Override
            public void onError(Throwable throwable) {
                future.doError(throwable);
            }
        });
        return future;
    }

    @Override
    public Future<Boolean> editGroupName(final int chatId, final String title) {
        GroupModel groupInfo = groups().get(chatId);
        final TypedFuture<Boolean> future = future();
        final long rid = RandomUtil.randomId();
        ask(requests().editGroupTitle(new GroupOutPeer(chatId, groupInfo.getAccessHash()), rid, title),
                new FutureCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate result) {
                        system().actorOf(SequenceActor.sequence()).send(
                                new SequenceActor.SeqUpdate(
                                        result.getSeq(),
                                        result.getState(),
                                        new UpdateGroupTitleChanged(chatId, rid, myUid(), title, result.getDate())));
                        future.doComplete(true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.doError(throwable);
                    }
                });
        return future;
    }

    @Override
    public Future<Boolean> addUser(final int chatId, final int uid) {

        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null) {
            return result(false);
        }

        UserModel user = users().get(uid);
        if (user == null) {
            return result(false);
        }

        final TypedFuture<Boolean> res = future();
        final long rid = RandomUtil.randomId();
        ask(requests().inviteUser(new GroupOutPeer(chatId, groupInfo.getAccessHash()), rid,
                        new UserOutPeer(uid, user.getAccessHash())),
                new FutureCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate result) {
                        system().actorOf(SequenceActor.sequence()).send(
                                new SequenceActor.SeqUpdate(
                                        result.getSeq(),
                                        result.getState(),
                                        new UpdateGroupUserAdded(chatId, rid, uid, myUid(), result.getDate())));
                        res.doComplete(true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        res.doError(throwable);
                    }
                });
        return res;
    }

    @Override
    public Future<Boolean> kickUser(final int chatId, final int uid) {

        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null) {
            return result(false);
        }

        UserModel user = users().get(uid);
        if (user == null) {
            return result(false);
        }

        final TypedFuture<Boolean> future = future();
        final long randomId = RandomUtil.randomId();

        ask(requests().kickUser(new GroupOutPeer(chatId, groupInfo.getAccessHash()), randomId, new UserOutPeer(uid, user.getAccessHash())),
                new FutureCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate result) {
                        system().actorOf(SequenceActor.sequence()).send(
                                new SequenceActor.SeqUpdate(
                                        result.getSeq(),
                                        result.getState(),
                                        new UpdateGroupUserKick(chatId, randomId, uid, myUid(), result.getDate())));
                        future.doComplete(true);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        future.doError(throwable);
                    }
                });

        return future;
    }

    @Override
    public void leaveChat(int chatId) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() != GroupState.JOINED) {
            return;
        }

        groups().put(chatId, groupInfo.getRaw().updateState(GroupState.DELETED_PENDING));

        system().actorOf(GroupLeaveActor.class, "pending_group")
                .send(new GroupLeaveActor.LeaveChat(chatId, groupInfo.getAccessHash()));

        ChatActionsActor.actions().deleteChat(DialogType.TYPE_GROUP, chatId);
    }

    @Override
    public void deleteChat(int chatId) {
        GroupModel groupInfo = groups().get(chatId);
        if (groupInfo == null || groupInfo.getState() == GroupState.DELETED_PENDING ||
                groupInfo.getState() == GroupState.DELETED) {
            return;
        }

        if (groupInfo.getState() == GroupState.JOINED) {
            system().actorOf(GroupLeaveActor.class, "pending_group")
                    .send(new GroupLeaveActor.LeaveChat(chatId, groupInfo.getAccessHash()));
            groups().put(chatId, groupInfo.getRaw().updateState(GroupState.DELETED_PENDING));
        } else {
            groups().put(chatId, groupInfo.getRaw().updateState(GroupState.DELETED));
        }

        ChatActionsActor.actions().deleteChat(DialogType.TYPE_GROUP, chatId);
    }

    //////// TOOLS

    private static List<GroupMember> addItem(List<GroupMember> uids, int uid, int inviterId, long date) {
        ArrayList<GroupMember> res = new ArrayList<GroupMember>(uids);
        boolean founded = false;
        for (GroupMember g : res) {
            if (g.getUid() == uid) {
                founded = true;
                break;
            }
        }
        if (!founded) {
            res.add(new GroupMember(uid, inviterId, date));
        }
        return res;
    }

    private static List<GroupMember> removeItem(List<GroupMember> uids, int uid) {
        ArrayList<GroupMember> res = new ArrayList<GroupMember>();
        for (GroupMember u : uids) {
            if (u.getUid() != uid) {
                res.add(u);
            }
        }
        return res;
    }

    private static boolean isEmptyEq(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a != null && b == null) {
            return false;
        }
        if (b != null && a == null) {
            return false;
        }

        return true;
    }

    private static boolean areSame(im.actor.messenger.storage.scheme.avatar.Avatar a, im.actor.messenger.storage.scheme.avatar.Avatar b) {
        if (a == b) {
            return true;
        }

        if (!isEmptyEq(a, b))
            return false;

        if (isEmptyEq(a.getSmallImage(), b.getSmallImage())) {
            return false;
        }

        if (a.getSmallImage() != null && b.getSmallImage() != null && a.getSmallImage().getFileLocation().getFileId() != b.getSmallImage().getFileLocation().getFileId()) {
            return false;
        }

        return true;
    }

    private static boolean areSame(List<GroupMember> a, List<GroupMember> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (GroupMember a1 : a) {
            boolean isFounded = false;
            for (GroupMember b1 : b) {
                if (a1.equals(b1)) {
                    isFounded = true;
                    break;
                }
            }
            if (isFounded) {
                return false;
            }
        }

        for (GroupMember b1 : b) {
            boolean isFounded = false;
            for (GroupMember a1 : a) {
                if (a1.equals(b1)) {
                    isFounded = true;
                    break;
                }
            }
            if (isFounded) {
                return false;
            }
        }

        return true;
    }
}