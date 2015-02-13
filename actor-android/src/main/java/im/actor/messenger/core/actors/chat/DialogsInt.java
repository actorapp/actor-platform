package im.actor.messenger.core.actors.chat;

import java.util.List;

import im.actor.api.scheme.Dialog;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.messages.ConversationMessage;
import im.actor.messenger.storage.scheme.messages.MessageState;

/**
 * Created by ex3ndr on 14.09.14.
 */
public interface DialogsInt {

    // History

    public void onDialogsHistoryLoaded(List<Dialog> dialogs);

    // New messages

    public void onMessageArrived(int type, int id, ConversationMessage conversationMessage);

    public void onMessageStateChanged(int type, int id, long rid, MessageState messageState);

    // Message deletion

    public void onDeleteMessages(int type, int id, long[] rids, ConversationMessage first);


    public void onChatClear(int type, int id);

    public void onDialogDelete(int type, int id);

    public void onCounterChanged(int type, int id, int value);

    // User and group changed

    public void onUserChangedName(int uid, String name);

    public void onUserChangedAvatar(int uid, Avatar avatar);

    public void onGroupChangedTitle(int groupId, String title);

    public void onGroupChangedAvatar(int groupId, Avatar avatar);
}
