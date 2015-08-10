package im.actor.runtime.storage;

import im.actor.runtime.EnginesRuntime;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;

public class AsyncEnginesProvider implements EnginesRuntime {

    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        return new AsyncListEngine<T>((ListStorageDisplayEx) storage, creator);
    }

    @Override
    public boolean isDisplayListSupported() {
        return true;
    }
}
