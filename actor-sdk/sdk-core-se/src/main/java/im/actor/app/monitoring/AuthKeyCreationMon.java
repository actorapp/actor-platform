package im.actor.app.monitoring;

import im.actor.core.network.Endpoints;
import im.actor.core.network.TrustedKey;
import im.actor.core.network.api.AuthKeyActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.mtproto.ConnectionEndpoint;

public class AuthKeyCreationMon extends Actor {
    private ActorRef keyCreation;
    private Endpoints endpoints;

    @Override
    public void preStart() {
        super.preStart();
        endpoints = new Endpoints(new ConnectionEndpoint[]{
                new ConnectionEndpoint("front1-mtproto-api-rev3.actor.im", 443, ConnectionEndpoint.Type.TCP),
                new ConnectionEndpoint("front2-mtproto-api-rev3.actor.im", 443, ConnectionEndpoint.Type.TCP)
        }, new TrustedKey[0]);
        keyCreation = system().actorOf(Props.create(AuthKeyActor.class, new ActorCreator<AuthKeyActor>() {
            @Override
            public AuthKeyActor create() {
                return new AuthKeyActor();
            }
        }), getPath() + "/test");
    }

    private void startMonitoring(long delay) {
        Log.d("AuthKeyCreation", "Start monitoring");
        keyCreation.send(new AuthKeyActor.StartKeyCreation(endpoints), self());
    }

    private void onKeyCreated() {
        Log.d("AuthKeyCreation", "Key created");
        keyCreation.send(new AuthKeyActor.StartKeyCreation(endpoints), self());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartMonitoring) {
            startMonitoring(((StartMonitoring) message).getDelay());
        } else if (message instanceof AuthKeyActor.KeyCreated) {
            onKeyCreated();
        } else {
            super.onReceive(message);
        }
    }

    public static class StartMonitoring {
        private long delay;

        public StartMonitoring(long delay) {
            this.delay = delay;
        }

        public long getDelay() {
            return delay;
        }
    }
}