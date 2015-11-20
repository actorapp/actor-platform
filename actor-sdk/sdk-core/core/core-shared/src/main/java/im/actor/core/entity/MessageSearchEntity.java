package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.content.AbsContent;

public class MessageSearchEntity {
    private @NotNull Peer peer;
    private long rid;
    private long date;
    private int senderId;

    private @NotNull AbsContent content;

    public MessageSearchEntity(@NotNull Peer peer, long rid, long date, int senderId, @NotNull AbsContent content) {
        this.peer = peer;
        this.rid = rid;
        this.date = date;
        this.senderId = senderId;
        this.content = content;
    }

    @NotNull
    public Peer getPeer() {
        return peer;
    }

    public long getRid() {
        return rid;
    }

    public long getDate() {
        return date;
    }

    public int getSenderId() {
        return senderId;
    }

    @NotNull
    public AbsContent getContent() {
        return content;
    }
}
