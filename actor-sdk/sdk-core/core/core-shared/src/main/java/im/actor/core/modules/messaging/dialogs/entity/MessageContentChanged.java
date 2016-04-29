package im.actor.core.modules.messaging.dialogs.entity;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;

public class MessageContentChanged implements AskMessage<Void> {

    private Peer peer;
    private long rid;
    private AbsContent content;

    public MessageContentChanged(Peer peer, long rid, AbsContent content) {
        this.peer = peer;
        this.rid = rid;
        this.content = content;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public AbsContent getContent() {
        return content;
    }
}
