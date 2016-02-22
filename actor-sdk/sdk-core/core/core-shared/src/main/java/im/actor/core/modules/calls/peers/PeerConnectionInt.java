package im.actor.core.modules.calls.peers;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.calls.entity.PeerNodeSettings;
import im.actor.runtime.actors.ActorInterface;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.webrtc.WebRTCMediaStream;

import static im.actor.runtime.actors.ActorSystem.system;

public class PeerConnectionInt extends ActorInterface {

    private final PeerConnectionCallback callback;

    public PeerConnectionInt(PeerNodeSettings ownSettings,
                             PeerNodeSettings theirSettings,
                             WebRTCMediaStream mediaStream,
                             PeerConnectionCallback callback,
                             ModuleContext context,
                             ActorRef dest, String path) {
        this.callback = callback;
        ActorRef ref = system().actorOf(dest.getPath() + "/" + path,
                PeerConnectionActor.CONSTRUCTOR(ownSettings, theirSettings, mediaStream,
                        new WrappedCallback(), context));
        setDest(ref);
    }

    public void onOfferNeeded() {
        send(new PeerConnectionActor.OnOfferNeeded());
    }

    public void onOffer(String sdp) {
        send(new PeerConnectionActor.OnOffer(sdp));
    }

    public void onAnswer(String sdp) {
        send(new PeerConnectionActor.OnAnswer(sdp));
    }

    public void onCandidate(int index, String id, String sdp) {
        send(new PeerConnectionActor.OnCandidate(index, id, sdp));
    }

    private class WrappedCallback implements PeerConnectionCallback {

        @Override
        public void onOffer(final String sdp) {
            getDest().send(new Runnable() {
                @Override
                public void run() {
                    callback.onOffer(sdp);
                }
            });
        }

        @Override
        public void onAnswer(final String sdp) {
            getDest().send(new Runnable() {
                @Override
                public void run() {
                    callback.onAnswer(sdp);
                }
            });
        }

        @Override
        public void onCandidate(final int mdpIndex, final String id, final String sdp) {
            getDest().send(new Runnable() {
                @Override
                public void run() {
                    callback.onCandidate(mdpIndex, id, sdp);
                }
            });
        }

        @Override
        public void onStreamAdded(final WebRTCMediaStream stream) {
            getDest().send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamAdded(stream);
                }
            });
        }

        @Override
        public void onStreamRemoved(final WebRTCMediaStream stream) {
            getDest().send(new Runnable() {
                @Override
                public void run() {
                    callback.onStreamRemoved(stream);
                }
            });
        }
    }
}