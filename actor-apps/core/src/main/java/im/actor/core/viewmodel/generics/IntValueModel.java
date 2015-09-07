package im.actor.core.viewmodel.generics;

import im.actor.runtime.mvvm.ValueModel;

public class IntValueModel extends ValueModel<Integer> {
    /**
     * Create ValueModel
     *
     * @param name         name of variable
     * @param defaultValue default value
     */
    public IntValueModel(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Integer get() {
        return super.get();
    }
}
