/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm.alg;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.generic.mvvm.ChangeDescription;


public interface Modification<T> {
    List<ChangeDescription<T>> modify(ArrayList<T> sourceList);
}