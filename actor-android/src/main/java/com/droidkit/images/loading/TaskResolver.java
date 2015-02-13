package com.droidkit.images.loading;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.images.loading.actors.base.BasicTaskActor;

import java.util.HashMap;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class TaskResolver {
    private final HashMap<Class<? extends AbsTask>, Class<? extends BasicTaskActor>> types =
            new HashMap<Class<? extends AbsTask>, Class<? extends BasicTaskActor>>();

    private final ImageLoader loader;

    public TaskResolver(ImageLoader loader) {
        this.loader = loader;
    }

    public synchronized void register(Class<? extends AbsTask> taskClass, Class<? extends BasicTaskActor> actorClass) {
        if (types.containsKey(taskClass)) {
            throw new RuntimeException("Already registered actor for task " + taskClass.getName());
        }

        types.put(taskClass, actorClass);
    }

    public <T extends AbsTask> ActorSelection resolveSelection(final T task) {
        if (!types.containsKey(task.getClass())) {
            throw new RuntimeException("No actor for task " + task.getClass().getName());
        }

        final Class actorClass = types.get(task.getClass());
        ActorCreator creator = new ActorCreator() {
            @Override
            public Actor create() {
                try {
                    return (Actor) actorClass.getConstructors()[0].newInstance(task, loader);
                } catch (Exception e) {
                    throw new RuntimeException("Exception during creating actor", e);
                }
            }
        };
        String path = "i_" + task.getKey();
        return new ActorSelection(Props.create(actorClass, creator), path);
    }
}
