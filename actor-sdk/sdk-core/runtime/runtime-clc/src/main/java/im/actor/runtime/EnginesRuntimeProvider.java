package im.actor.runtime;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.generic.GenericEnginesProvider;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.runtime.mvvm.PlatformDisplayList;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineItem;
import im.actor.runtime.storage.ListStorage;

public class EnginesRuntimeProvider extends GenericEnginesProvider {
    public EnginesRuntimeProvider() {
        super(DisplayList.OperationMode.GENERAL);
    }
}
