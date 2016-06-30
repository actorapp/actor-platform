package im.actor.runtime;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

public interface EnginesRuntime {

    /**
     * Creating of List Engine instance
     *
     * @param storage list storage
     * @param creator list creator
     * @param <T>     type of object
     * @return the List Engine
     */
    <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator);

    /**
     * Creating of Display List instance
     *
     * @param listEngine       list engine
     * @param isSharedInstance is this list can be used by different lists
     * @param clazz            entity simple class name
     * @param <T>              type of object
     * @return the Display List
     */
    <T extends BserObject & ListEngineItem> PlatformDisplayList<T> createDisplayList(ListEngine<T> listEngine, boolean isSharedInstance,
                                                                             String clazz);
}
