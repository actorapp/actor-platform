package im.actor.messenger.core.actors.groups;

import com.droidkit.actors.concurrency.Future;

import java.util.List;

import im.actor.api.scheme.Avatar;
import im.actor.api.scheme.Group;

/**
 * Created by ex3ndr on 08.10.14.
 */
public interface GroupsInt {

    public Future<Boolean> onUpdateGroups(List<Group> groups);

    public void onInvite(int chatId, long rid, int inviterId, long date);

    public void onUserLeave(int chatId, long rid, int uid, long date);

    public void onUserKicked(int chatId, long rid, int uid, int kicker, long date);

    public void onUserAdded(int chatId, long rid, int uid, int adder, long date);

    public void onTitleChanged(int chatId, long rid, int uid, String title, long date);

    public void onAvatarChanged(int chatId, long rid, int uid, Avatar avatar, long date);

    public Future<Integer> createGroup(String title, int[] uid);

    public Future<Boolean> editGroupName(int chatId, String title);

    public Future<Boolean> addUser(int chatId, int uid);

    public Future<Boolean> kickUser(int chatId, int uid);

    public void leaveChat(int chatId);

    public void deleteChat(int chatId);
}
