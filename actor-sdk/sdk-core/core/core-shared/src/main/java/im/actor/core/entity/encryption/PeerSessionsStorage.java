package im.actor.core.entity.encryption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.function.Function;
import im.actor.runtime.storage.KeyValueItem;

public class PeerSessionsStorage extends BserObject implements KeyValueItem {

    public static Function<PeerSessionsStorage, PeerSession[]> SESSIONS = new Function<PeerSessionsStorage, PeerSession[]>() {
        @Override
        public PeerSession[] apply(PeerSessionsStorage peerSessionsStorage) {
            if (peerSessionsStorage == null) {
                return new PeerSession[0];
            }
            return peerSessionsStorage.getSessions();
        }
    };

    private int uid;
    private ArrayList<PeerSession> sessions;

    public PeerSessionsStorage(int uid, List<PeerSession> sessions) {
        this.sessions = new ArrayList<>(sessions);
        this.uid = uid;
    }

    public PeerSessionsStorage(byte[] data) throws IOException {
        load(data);
    }

    public int getUid() {
        return uid;
    }

    public PeerSession[] getSessions() {
        return sessions.toArray(new PeerSession[sessions.size()]);
    }

    public PeerSessionsStorage addSession(PeerSession session) {
        for (PeerSession s : sessions) {
            if (s.getOwnKeyGroupId() == session.getOwnKeyGroupId() &&
                    s.getTheirKeyGroupId() == session.getTheirKeyGroupId() &&
                    s.getOwnPreKeyId() == session.getOwnPreKeyId() &&
                    s.getTheirPreKeyId() == session.getTheirPreKeyId()) {
                return this;
            }
        }
        ArrayList<PeerSession> nSessions = new ArrayList<>(sessions);
        nSessions.add(session);
        return new PeerSessionsStorage(uid, nSessions);
    }

    @Override
    public void parse(BserValues values) throws IOException {
        uid = values.getInt(1);
        sessions = new ArrayList<>();
        List<byte[]> descs = values.getRepeatedBytes(2);
        for (byte[] d : descs) {
            sessions.add(new PeerSession(d));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, uid);
        writer.writeRepeatedObj(2, sessions);
    }

    @Override
    public long getEngineId() {
        return uid;
    }
}
