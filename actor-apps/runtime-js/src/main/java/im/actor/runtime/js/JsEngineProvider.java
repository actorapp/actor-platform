package im.actor.runtime.js;

import java.util.HashMap;

import im.actor.runtime.EnginesRuntime;
import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.js.mvvm.JsEntityConverter;
import im.actor.runtime.js.storage.JsListEngine;
import im.actor.runtime.js.storage.JsListStorage;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

public class JsEngineProvider implements EnginesRuntime {

    private static HashMap<String, JsEntityConverter> converters = new HashMap<String, JsEntityConverter>();

    public static void registerEntity(String name, JsEntityConverter converter) {
        converters.put(name, converter);
    }

    @Override
    public <T extends BserObject & ListEngineItem> ListEngine<T> createListEngine(ListStorage storage, BserCreator<T> creator) {
        return new JsListEngine<T>((JsListStorage) storage, creator);
    }

    @Override
    public <T extends BserObject & ListEngineItem> PlatformDisplayList<T> createDisplayList(ListEngine<T> listEngine,
                                                                                    boolean isSharedInstance,
                                                                                    String clazz) {
        JsEntityConverter converter = converters.get(clazz);
        if (converter == null) {
            throw new RuntimeException("Unsupported entity type: " + clazz);
        }

        return new JsDisplayList<>((JsListEngine) listEngine, converter);
    }
}