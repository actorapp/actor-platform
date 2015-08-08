/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.core.viewmodel.UserPhone;

/**
 * Created by ex3ndr on 22.05.15.
 */
public class ArrayListUserPhone extends ArrayList<UserPhone> {

    public ArrayListUserPhone(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayListUserPhone() {
        super();
    }

    public ArrayListUserPhone(Collection<? extends UserPhone> c) {
        super(c);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public UserPhone get(int index) {
        return super.get(index);
    }

}
