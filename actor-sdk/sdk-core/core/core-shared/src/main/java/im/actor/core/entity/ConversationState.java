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

    public static BserCreator<ConversationState> CREATOR = ConversationState::new;

    public static ValueDefaultCreator<ConversationState> DEFAULT_CREATOR = id ->
            new ConversationState(Peer.fromUniqueId(id), false, true, 0, 0, 0, 0, 0, 0);

    public static final String ENTITY_NAME = "ConversationState";

    private Peer peer;
    private boolean isLoaded;
    private boolean isEmpty;
    private int unreadCount;
    private long inMaxMessageDate;
    private long outSendDate;
    private long inReadDate;
    private long outReadDate;
    private long outReceiveDate;

    public ConversationState(Peer peer, boolean isLoaded, boolean isEmpty,
                             int unreadCount,
                             long inMaxMessageDate,
                             long inReadDate,
                             long outReadDate,
                             long outReceiveDate,
                             long outSendDate) {
        this.peer = peer;
        this.isLoaded = isLoaded;
        this.isEmpty = isEmpty;
        this.unreadCount = unreadCount;
        this.inMaxMessageDate = inMaxMessageDate;
        this.inReadDate = inReadDate;
        this.outReadDate = outReadDate;
        this.outReceiveDate = outReceiveDate;
        this.outSendDate = outSendDate;
    }

    private ConversationState() {

    }

    public Peer getPeer() {
        return peer;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public long getInMaxMessageDate() {
        return inMaxMessageDate;
    }

    public long getInReadDate() {
        return inReadDate;
    }

    public long getOutSendDate() {
        return outSendDate;
    }

    public long getOutReadDate() {
        return outReadDate;
    }

    public long getOutReceiveDate() {
        return outReceiveDate;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public ConversationState changeIsLoaded(boolean isLoaded) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeIsEmpty(boolean isEmpty) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeInReadDate(long inReadDate) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeInMaxDate(long inMaxMessageDate) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeOutReceiveDate(long outReceiveDate) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeOutReadDate(long outReadDate) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }

    public ConversationState changeCounter(int unreadCount) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }


    public ConversationState changeOutSendDate(long outSendDate) {
        return new ConversationState(peer, isLoaded, isEmpty, unreadCount, inMaxMessageDate, inReadDate, outReadDate, outReceiveDate, outSendDate);
    }


    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        isLoaded = values.getBool(2, false);
        isEmpty = values.getBool(8, false);
        inReadDate = values.getLong(3, 0);
        outReceiveDate = values.getLong(4, 0);
        outReadDate = values.getLong(5, 0);
        unreadCount = values.getInt(6);
        outSendDate = values.getLong(7, 0);
        inMaxMessageDate = values.getLong(9, 0);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBytes(1, peer.toByteArray());
        writer.writeBool(2, isLoaded);
        writer.writeBool(8, isEmpty);
        writer.writeLong(3, inReadDate);
        writer.writeLong(4, outReceiveDate);
        writer.writeLong(5, outReadDate);
        writer.writeInt(6, unreadCount);
        writer.writeLong(7, outSendDate);
        writer.writeLong(9, inMaxMessageDate);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }
}
