/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.mvvm.alg;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.mvvm.ChangeDescription;

public interface Modification<T> {
    List<ChangeDescription<T>> modify(ArrayList<T> sourceList);
}