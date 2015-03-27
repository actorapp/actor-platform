package im.actor.model.droidkit.actors;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class RunnableActor extends Actor {
    public static ActorRef buildActor(String path) {
        return ActorSystem.system().actorOf(Props.create(RunnableActor.class, new ActorCreator<RunnableActor>() {
            @Override
            public RunnableActor create() {
                return new RunnableActor();
            }
        }), path);
    }
}
