/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel.generics;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.core.viewmodel.UserLink;

/**
 * Created by ex3ndr on 22.05.15.
 */
public class ArrayListUserLink extends ArrayList<UserLink> {

    public ArrayListUserLink(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayListUserLink() {
        super();
    }

    public ArrayListUserLink(Collection<? extends UserLink> c) {
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
    public UserLink get(int index) {
        return super.get(index);
    }

}
