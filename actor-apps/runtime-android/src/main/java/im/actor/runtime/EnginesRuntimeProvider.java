package im.actor.runtime;

import im.actor.runtime.generic.GenericEnginesProvider;
import im.actor.runtime.generic.mvvm.DisplayList;

public class EnginesRuntimeProvider extends GenericEnginesProvider {
    public EnginesRuntimeProvider() {
        super(DisplayList.OperationMode.ANDROID);
    }
}
