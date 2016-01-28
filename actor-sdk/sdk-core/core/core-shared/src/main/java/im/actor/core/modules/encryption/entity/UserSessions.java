package im.actor.core.modules.encryption.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.encryption.PeerSession;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class UserSessions extends BserObject {

    private int uid;
    private ArrayList<PeerSession> sessionDescriptors;

    public UserSessions(int uid, ArrayList<PeerSession> sessionDescriptors) {
        this.uid = uid;
        this.sessionDescriptors = sessionDescriptors;
    }

    public int getUid() {
        return uid;
    }

    public ArrayList<PeerSession> getSessionDescriptors() {
        return sessionDescriptors;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);

        sessionDescriptors = new ArrayList<>();
        List<byte[]> desc = values.getRepeatedBytes(2);
        for (byte[] b : desc) {
            sessionDescriptors.add(new PeerSession(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeRepeatedObj(2, sessionDescriptors);
    }
}
