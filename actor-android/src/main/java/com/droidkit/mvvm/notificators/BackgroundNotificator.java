package com.droidkit.mvvm.notificators;

import com.droidkit.actors.ActorRef;
import com.droidkit.engine._internal.RunnableActor;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 19.09.14.
 */
public abstract class BackgroundNotificator<S, V> extends Notificator<S, V> {

    private ActorRef notificator;

    protected BackgroundNotificator() {
        notificator = system().actorOf(RunnableActor.class, "mvvm_notificator");
    }

    @Override
    public void notify(final V value) {
        notificator.send(new Runnable() {
            @Override
            public void run() {
                synchronized (subscribers) {
                    for (S sub : subscribers) {
                        BackgroundNotificator.this.notify(sub, value);
                    }
                }
            }
        });
    }

    protected abstract void notify(S subscriber, V value);
}
