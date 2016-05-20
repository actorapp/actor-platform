package im.actor.core.modules.sequence.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiAppCounters;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Peer;
import im.actor.core.entity.EntityConverter;
import im.actor.core.network.parser.Update;

public class CombinedDifference {

    private HashMap<Peer, ReadByMeValue> readByMe = new HashMap<>();
    private HashMap<Peer, Long> read = new HashMap<>();
    private HashMap<Peer, Long> received = new HashMap<>();
    private HashMap<Peer, List<UpdateMessage>> messages = new HashMap<>();
    private List<Update> otherUpdates = new ArrayList<>();

    public HashMap<Peer, ReadByMeValue> getReadByMe() {
        return readByMe;
    }

    public HashMap<Peer, Long> getRead() {
        return read;
    }

    public HashMap<Peer, Long> getReceived() {
        return received;
    }

    public HashMap<Peer, List<UpdateMessage>> getMessages() {
        return messages;
    }

    public List<Update> getOtherUpdates() {
        return otherUpdates;
    }

    public void putMessage(UpdateMessage message) {
        Peer peer = EntityConverter.convert(message.getPeer());
        if (messages.containsKey(peer)) {
            messages.get(peer).add(message);
        } else {
            ArrayList<UpdateMessage> msgs = new ArrayList<>();
            msgs.add(message);
            messages.put(peer, msgs);
        }
    }

    public void putReadByMe(Peer peer, long date, int counter) {
        if (readByMe.containsKey(peer)) {
            if (readByMe.get(peer).date <= date) {
                readByMe.put(peer, new ReadByMeValue(date, counter));
            }
        } else {
            readByMe.put(peer, new ReadByMeValue(date, counter));
        }
    }

    public void putRead(Peer peer, long date) {
        if (received.containsKey(peer)) {
            if (received.get(peer) <= date) {
                received.remove(peer);
            }
        }

        put(read, peer, date);
    }

    public void putReceived(Peer peer, long date) {
        if (read.containsKey(peer)) {
            if (read.get(peer) >= date) {
                return;
            }
        }

        put(received, peer, date);
    }

    private void put(HashMap<Peer, Long> map, Peer peer, long date) {
        if (map.containsKey(peer)) {
            if (map.get(peer) <= date) {
                map.put(peer, date);
            }
        } else {
            map.put(peer, date);
        }
    }

    public static class ReadByMeValue {

        private long date;
        private int counter;

        public ReadByMeValue(long date, int counter) {
            this.date = date;
            this.counter = counter;
        }

        public long getDate() {
            return date;
        }

        public int getCounter() {
            return counter;
        }
    }
}