package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

/**
 * Created by ex3ndr on 16.10.15.
 */
public class ValueModelContactRecord extends ValueModel<ArrayListContactRecord> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public ValueModelContactRecord(String name, ArrayListContactRecord defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public ArrayListContactRecord get() {
        return super.get();
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListContactRecord> listener) {
        super.subscribe(listener);
    }

    @Override
    public void subscribe(ValueChangedListener<ArrayListContactRecord> listener, boolean notify) {
        super.subscribe(listener, notify);
    }

    @Override
    public void unsubscribe(ValueChangedListener<ArrayListContactRecord> listener) {
        super.unsubscribe(listener);
    }
}
