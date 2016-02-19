package im.actor.core.modules.calls.entity;

import java.util.HashMap;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.PeerConnectionActor;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class PeerCollection {

    private HashMap<Integer, HashMap<Long, ActorRef>> peerConnections = new HashMap<>();
    private ActorRef parent;
    private ModuleContext context;
    private boolean isEnabled;
    private boolean isMuted;

    public PeerCollection(ActorRef parent, ModuleContext context) {
        this.parent = parent;
        this.context = context;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        if (this.isEnabled == isEnabled) {
            return;
        }

        this.isEnabled = isEnabled;
        for (HashMap<Long, ActorRef> deviceRefs : peerConnections.values()) {
            for (ActorRef ref : deviceRefs.values()) {
                ref.send(new PeerConnectionActor.DoEnable(isEnabled));
            }
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setIsMuted(boolean isMuted) {
        if (this.isMuted == isMuted) {
            return;
        }

        this.isMuted = isMuted;
        for (HashMap<Long, ActorRef> deviceRefs : peerConnections.values()) {
            for (ActorRef ref : deviceRefs.values()) {
                ref.send(new PeerConnectionActor.DoMute(isMuted));
            }
        }
    }

    public ActorRef getPeer(int uid, long deviceId) {
        if (!peerConnections.containsKey(uid)) {
            peerConnections.put(uid, new HashMap<Long, ActorRef>());
        }
        HashMap<Long, ActorRef> refs = peerConnections.get(uid);
        if (refs.containsKey(deviceId)) {
            return refs.get(deviceId);
        }
        ActorRef ref = system().actorOf(parent.getPath() + "/uid:" + uid + "/" + deviceId,
                PeerConnectionActor.CONSTRUCTOR(parent, uid, deviceId, false, false, context));
        refs.put(deviceId, ref);
        return ref;
    }

    public void disconnectPeer(int uid, long deviceId) {
        HashMap<Long, ActorRef> deviceRefs = peerConnections.get(uid);
        if (deviceRefs == null) {
            return;
        }
        ActorRef ref = deviceRefs.get(deviceId);
        if (ref == null) {
            return;
        }
        ref.send(new PeerConnectionActor.DoStop());
    }

    public void stopAll() {
        for (int uid : peerConnections.keySet()) {
            HashMap<Long, ActorRef> peers = peerConnections.get(uid);
            for (ActorRef p : peers.values()) {
                p.send(new PeerConnectionActor.DoStop());
            }
        }
        peerConnections.clear();
    }
}
