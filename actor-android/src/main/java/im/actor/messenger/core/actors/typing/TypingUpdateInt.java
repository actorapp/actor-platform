package im.actor.messenger.core.actors.typing;

import im.actor.api.scheme.Peer;

/**
 * Created by ex3ndr on 10.10.14.
 */
public interface TypingUpdateInt {
    public void onTypingUpdate(Peer peer, int uid);

    public void onInMessage(Peer peer, int senderId);
}
