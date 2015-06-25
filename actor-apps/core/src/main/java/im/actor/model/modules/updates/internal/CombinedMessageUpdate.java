/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.updates.internal;

import java.util.ArrayList;

import im.actor.model.api.Message;

public class CombinedMessageUpdate {

    private ArrayList<CombinedMessage> messages = new ArrayList<CombinedMessage>();
    private long receivedKey;
    private long readKey;

    public long getReceivedKey() {
        return receivedKey;
    }

    public void setReceivedKey(long receivedKey) {
        this.receivedKey = receivedKey;
    }

    public long getReadKey() {
        return readKey;
    }

    public void setReadKey(long readKey) {
        this.readKey = readKey;
    }

    public ArrayList<CombinedMessage> getMessages() {
        return messages;
    }

    public static class CombinedMessage {
        private long rid;
        private int sender;
        private long date;
        private Message message;

        public CombinedMessage(long rid, int sender, long date, Message message) {
            this.rid = rid;
            this.sender = sender;
            this.date = date;
            this.message = message;
        }

        public long getRid() {
            return rid;
        }

        public int getSender() {
            return sender;
        }

        public long getDate() {
            return date;
        }

        public Message getMessage() {
            return message;
        }
    }
}
