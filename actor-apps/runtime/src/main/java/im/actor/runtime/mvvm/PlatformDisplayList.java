package im.actor.runtime.mvvm;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.storage.ListEngineItem;

public interface PlatformDisplayList<T extends BserObject & ListEngineItem> {

    void initCenter(long rid);

    void initTop();

    void initEmpty();
}
