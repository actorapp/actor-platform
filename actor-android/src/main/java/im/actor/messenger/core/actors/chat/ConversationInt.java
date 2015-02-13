package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.concurrency.Future;

import java.util.List;

import im.actor.api.scheme.HistoryMessage;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;
import im.actor.messenger.storage.scheme.messages.types.AbsMessage;

/**
 * Created by ex3ndr on 08.09.14.
 */
public interface ConversationInt {

    // History

    public void onHistoryLoaded(List<HistoryMessage> history);

    // In messages

    public void onInMessage(long rid, int senderId, long date, AbsMessage message);

    // Out messages

    public void onOutText(long rid, String text, boolean isEncrypted);

    public Future<MessageModel> onStartUpload(AbsFileMessage content);

    public void onUploaded(long rid, FileLocation fileLocation);


    public void onMessageTryAgain(long rid);

    public void onMessageSent(long rid, long date);

    public void onMessageError(long rid);

    public void onMessageMarkReceived(long rid);

    public void onMessageMarkRead(long rid);

    public void onMessageDownloaded(long rid);

    public void onMessageUnDownloaded(long rid);

    public void clearChat();

    public void deleteChat();

    public void deleteMessage(long[] rids);
}
