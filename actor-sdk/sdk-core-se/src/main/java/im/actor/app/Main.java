package im.actor.app;

import im.actor.app.monitoring.AuthKeyCreationMon;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;

public class Main {

    public static void main(String[] args) {

        ActorRef actor = ActorSystem.system().actorOf(Props.create(AuthKeyCreationMon.class, new ActorCreator<AuthKeyCreationMon>() {
            @Override
            public AuthKeyCreationMon create() {
                return new AuthKeyCreationMon();
            }
        }), "auth_key_mon");

        actor.send(new AuthKeyCreationMon.StartMonitoring(0));

        while (true) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
