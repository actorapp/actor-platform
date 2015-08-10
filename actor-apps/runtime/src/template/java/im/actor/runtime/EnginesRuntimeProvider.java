package im.actor.runtime;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

public class EnginesRuntimeProvider implements EnginesRuntime {

    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public boolean isDisplayListSupported() {
        throw new RuntimeException("Dumb");
    }
}
