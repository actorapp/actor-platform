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
            return new ConversationState(Peer.fromUniqueId(id), false);
        }
    };

    public static final String ENTITY_NAME = "ConversationState";

    private Peer peer;
    private boolean isLoaded;

    public ConversationState(Peer peer, boolean isLoaded) {
        this.peer = peer;
        this.isLoaded = isLoaded;
    }

    private ConversationState() {

    }

    public Peer getPeer() {
        return peer;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public ConversationState changeIsLoaded(boolean isLoaded) {
        return new ConversationState(peer, isLoaded);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        isLoaded = values.getBool(2, false);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBytes(1, peer.toByteArray());
        writer.writeBool(2, isLoaded);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }
}
