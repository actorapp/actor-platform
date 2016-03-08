package im.actor.allmessages;

import java.io.IOException;
import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class MessageEx extends Message {

    public static final BserCreator<MessageEx> CREATOR = new BserCreator<MessageEx>() {
        @Override
        public MessageEx createInstance() {
            return new MessageEx();
        }
    };

    public MessageEx(){
        super();
    }

    Peer peer;
    public MessageEx(long rid, long sortDate, long date, int senderId, MessageState messageState, AbsContent content, List<Reaction> reactions) {
        super(rid, sortDate, date, senderId, messageState, content, reactions);
    }

    public MessageEx(long rid, long sortDate, long date, int senderId, MessageState messageState, AbsContent content, List<Reaction> reactions, Peer peer) {
        super(rid, sortDate, date, senderId, messageState, content, reactions);
        this.peer = peer;
    }

    public MessageEx(Message msg, Peer peer) {
        super(msg.getRid(), msg.getSortDate(), msg.getDate(), msg.getSenderId(), msg.getMessageState(), msg.getContent(), msg.getReactions());
        this.peer = peer;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        peer = Peer.fromUniqueId(values.getLong(8));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeLong(8, peer.getUnuqueId());
    }

    public Peer getPeer() {
        return peer;
    }
}
