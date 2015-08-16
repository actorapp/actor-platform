package im.actor.runtime.cocoa;

import im.actor.runtime.generic.GenericEnginesProvider;
import im.actor.runtime.generic.mvvm.DisplayList;

public class CocoaEnginesProvider extends GenericEnginesProvider {
    public CocoaEnginesProvider() {
        super(DisplayList.OperationMode.IOS);
    }
}
