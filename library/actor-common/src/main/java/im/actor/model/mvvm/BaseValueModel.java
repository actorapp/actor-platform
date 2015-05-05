/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

public abstract class BaseValueModel<T> {
    private T rawObj;

    public BaseValueModel(T rawObj) {
        this.rawObj = rawObj;
    }

    void update(T rawObj) {
        this.rawObj = rawObj;
        updateValues(rawObj);
    }

    protected abstract void updateValues(T rawObj);
}
