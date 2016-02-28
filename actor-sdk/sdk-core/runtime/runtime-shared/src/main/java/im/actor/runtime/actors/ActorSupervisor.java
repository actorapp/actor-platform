package im.actor.runtime.actors;

public interface ActorSupervisor {
    void onActorStopped(ActorRef ref);
}
