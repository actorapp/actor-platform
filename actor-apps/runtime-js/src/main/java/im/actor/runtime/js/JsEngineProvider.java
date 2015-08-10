package im.actor.runtime.js;

import im.actor.runtime.EnginesRuntime;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.js.storage.JsListEngine;
import im.actor.runtime.js.storage.JsListStorage;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

public class JsEngineProvider implements EnginesRuntime {
    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        return new JsListEngine<T>((JsListStorage) storage, creator);
    }

    @Override
    public boolean isDisplayListSupported() {
        return false;
    }
}
