/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

public interface ModelChangedListener<T> {
    void onChanged(T model);
}
