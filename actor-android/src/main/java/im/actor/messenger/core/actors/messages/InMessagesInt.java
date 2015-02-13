package im.actor.messenger.core.actors.messages;

import java.util.List;

import im.actor.api.scheme.MessageContent;
import im.actor.api.scheme.Peer;

/**
 * Created by ex3ndr on 08.10.14.
 */
public interface InMessagesInt {

    public void onEncryptedMessage(Peer peer, int senderUid, long date, byte[] messageKey, byte[] message);

    public void onMessage(Peer peer, int senderUid, long date, long rid, MessageContent content);

    public void onMessageSent(Peer peer, long rid, long date);

    public void onMessageReceived(Peer peer, long date);

    public void onMessageRead(Peer peer, long date);

    public void onMessageReadByMe(Peer peer, long date);

    public void onMessageEncryptedReceived(Peer peer, long rid);

    public void onMessageEncryptedRead(Peer peer, long rid);

    public void onMessageEncryptedReadByMe(Peer peer, long rid);

    public void onMessageDeleted(Peer peer, List<Long> rid);

    public void onChatDelete(Peer peer);

    public void onChatClear(Peer peer);
}
