package im.actor.core.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.mvvm.ValueDefaultCreator;
import im.actor.runtime.storage.KeyValueItem;

public class ConversationState extends BserObject implements KeyValueItem {

    public static ConversationState fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ConversationState(), data);
    }

    public static BserCreator<ConversationState> CREATOR = new BserCreator<ConversationState>() {
        @Override
        public ConversationState createInstance() {
            return new ConversationState();
        }
    };

    public static ValueDefaultCreator<ConversationState> DEFAULT_CREATOR = new ValueDefaultCreator<ConversationState>() {
        @Override
        public ConversationState createDefaultInstance(long id) {
            return new ConversationState(Peer.fromUniqueId(id), false, 0, 0, 0, 0);
        }
    };

    public static final String ENTITY_NAME = "ConversationState";

    private Peer peer;
    private boolean isLoaded;
    private long inMaxMessageDate;
    private long inReadDate;
    private long outReadDate;
    private long outReceiveState;

    public ConversationState(Peer peer, boolean isLoaded,
                             long inMaxMessageDate,
                             long inReadDate,
                             long outReadDate,
                             long outReceiveState) {
        this.peer = peer;
        this.isLoaded = isLoaded;
        this.inMaxMessageDate = inMaxMessageDate;
        this.inReadDate = inReadDate;
        this.outReadDate = outReadDate;
        this.outReceiveState = outReceiveState;
    }

    private ConversationState() {

    }

    public Peer getPeer() {
        return peer;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public long getInMaxMessageDate() {
        return inMaxMessageDate;
    }

    public long getInReadDate() {
        return inReadDate;
    }

    public long getOutReadDate() {
        return outReadDate;
    }

    public long getOutReceiveState() {
        return outReceiveState;
    }

    public ConversationState changeIsLoaded(boolean isLoaded) {
        return new ConversationState(peer, isLoaded, inMaxMessageDate, inReadDate, outReadDate, outReceiveState);
    }

    public ConversationState changeInReadDate(long inReadDate) {
        return new ConversationState(peer, isLoaded, inMaxMessageDate, inReadDate, outReadDate, outReceiveState);
    }

    public ConversationState changeInMaxDate(long inMaxMessageDate) {
        return new ConversationState(peer, isLoaded, inMaxMessageDate, inReadDate, outReadDate, outReceiveState);
    }

    public ConversationState changeOutReceiveDate(long outReceiveState) {
        return new ConversationState(peer, isLoaded, inMaxMessageDate, inReadDate, outReadDate, outReceiveState);
    }

    public ConversationState changeOutReadDate(long outReadDate) {
        return new ConversationState(peer, isLoaded, inMaxMessageDate, inReadDate, outReadDate, outReceiveState);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        isLoaded = values.getBool(2, false);
        inReadDate = values.getLong(3, 0);
        outReceiveState = values.getLong(4, 0);
        outReadDate = values.getLong(5, 0);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBytes(1, peer.toByteArray());
        writer.writeBool(2, isLoaded);
        writer.writeLong(3, inReadDate);
        writer.writeLong(4, outReceiveState);
        writer.writeLong(5, outReadDate);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }
}
