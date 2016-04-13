/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging.history.entity;

import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;

public class DialogHistory {

    private final Peer peer;
    private final int unreadCount;
    private final long sortDate;

    private final long rid;
    private final long date;
    private final int senderId;
    private final AbsContent content;
    private boolean isRead;
    private boolean isReceived;

    public DialogHistory(Peer peer, int unreadCount, long sortDate,
                         long rid, long date, int senderId, AbsContent content, boolean isRead,
                         boolean isReceived) {
        this.peer = peer;
        this.unreadCount = unreadCount;
        this.sortDate = sortDate;
        this.rid = rid;
        this.date = date;
        this.senderId = senderId;
        this.content = content;
        this.isRead = isRead;
        this.isReceived = isReceived;
    }

    public Peer getPeer() {
        return peer;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public long getSortDate() {
        return sortDate;
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

    public AbsContent getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isReceived() {
        return isReceived;
    }
}
