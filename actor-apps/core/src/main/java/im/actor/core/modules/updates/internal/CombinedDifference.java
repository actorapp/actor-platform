package im.actor.core.modules.updates.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import im.actor.core.api.ApiAppCounters;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.entity.Peer;
import im.actor.core.modules.internal.messages.entity.EntityConverter;
import im.actor.core.network.parser.Update;

public class CombinedDifference {

    private HashMap<Peer, Long> readByMe = new HashMap<Peer, Long>();
    private HashMap<Peer, Long> read = new HashMap<Peer, Long>();
    private HashMap<Peer, Long> received = new HashMap<Peer, Long>();
    private HashMap<Peer, List<UpdateMessage>> messages = new HashMap<Peer, List<UpdateMessage>>();
    private List<Update> otherUpdates = new ArrayList<Update>();
    private ApiAppCounters counters;

    public ApiAppCounters getCounters() {
        return counters;
    }

    public void setCounters(ApiAppCounters counters) {
        this.counters = counters;
    }

    public HashMap<Peer, Long> getReadByMe() {
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
            ArrayList<UpdateMessage> msgs = new ArrayList<UpdateMessage>();
            msgs.add(message);
            messages.put(peer, msgs);
        }
    }

    public void putReadByMe(Peer peer, long date) {
        put(readByMe, peer, date);
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
}