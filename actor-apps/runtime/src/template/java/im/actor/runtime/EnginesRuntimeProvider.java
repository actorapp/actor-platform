package im.actor.runtime;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.KeyValueEngine;
import im.actor.runtime.storage.KeyValueItem;
import im.actor.runtime.storage.KeyValueStorage;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

/**
 * Created by ex3ndr on 08.08.15.
 */
public class EnginesRuntimeProvider implements EnginesRuntime {

    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public <T extends BserObject & KeyValueItem> KeyValueEngine<T> createKeyValueEngine(KeyValueStorage storage, BserCreator<T> creator) {
        throw new RuntimeException("Dumb");
    }
}
