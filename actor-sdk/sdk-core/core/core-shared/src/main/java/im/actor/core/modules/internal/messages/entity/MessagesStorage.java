/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.messages.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class MessagesStorage extends BserObject {

    public static MessagesStorage fromBytes(byte[] data) throws IOException {
        return Bser.parse(new MessagesStorage(), data);
    }

    private ArrayList<MessageRef> messages = new ArrayList<MessageRef>();

    public ArrayList<MessageRef> getMessages() {
        return messages;
    }

    public void addOrUpdate(long rid, long date) {
        for (MessageRef ref : messages) {
            if (ref.getRid() == rid) {
                messages.remove(ref);
                break;
            }
        }
        messages.add(new MessageRef(rid, date));
    }

    public boolean update(long rid, long date) {
        for (MessageRef ref : messages) {
            if (ref.getRid() == rid) {
                messages.remove(ref);
                messages.add(new MessageRef(rid, date));
                return true;
            }
        }
        return false;
    }

    public boolean remove(long rid) {
        for (MessageRef ref : messages) {
            if (ref.getRid() == rid) {
                messages.remove(ref);
                return true;
            }
        }
        return false;
    }

    public boolean remove(List<Long> rids) {
        ArrayList<MessageRef> toRemove = find(rids);
        if (toRemove.size() > 0) {
            messages.removeAll(toRemove);
            return false;
        }
        return false;
    }

    public ArrayList<MessageRef> find(List<Long> rids) {
        ArrayList<MessageRef> res = new ArrayList<MessageRef>();
        outer:
        for (MessageRef ref : messages) {
            for (Long l : rids) {
                if (ref.getRid() == l) {
                    res.add(ref);
                    continue outer;
                }
            }
        }
        return res;
    }

    public ArrayList<MessageRef> removeBeforeDate(long date) {
        ArrayList<MessageRef> res = findBeforeDate(date);
        messages.removeAll(res);
        return res;
    }

    public ArrayList<MessageRef> findBeforeDate(long date) {
        ArrayList<MessageRef> res = new ArrayList<MessageRef>();
        for (MessageRef ref : messages) {
            if (ref.getDate() <= date) {
                res.add(ref);
            }
        }
        return res;
    }

    public void clear() {
        messages.clear();
    }

    public int getCount() {
        return messages.size();
    }

    @Override

    public void parse(BserValues values) throws IOException {
        messages.clear();
        int count = values.getRepeatedCount(1);
        List<MessageRef> tmp = new ArrayList<MessageRef>();
        for (int i = 0; i < count; i++) {
            tmp.add(new MessageRef());
        }
        messages.addAll(values.getRepeatedObj(1, tmp));
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, messages);
    }
}
