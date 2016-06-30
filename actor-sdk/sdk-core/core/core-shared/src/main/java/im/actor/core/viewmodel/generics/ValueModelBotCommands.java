package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class ValueModelBotCommands extends ValueModel<ArrayListBotCommands> {

    public ValueModelBotCommands(String name, ArrayListBotCommands defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public ArrayListBotCommands get() {
        return super.get();
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListBotCommands> listener) {
        super.subscribe(listener);
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListBotCommands> listener, boolean notify) {
        super.subscribe(listener, notify);
    }

    @Override
    public void unsubscribe(ValueChangedListener<ArrayListBotCommands> listener) {
        super.unsubscribe(listener);
    }
}
