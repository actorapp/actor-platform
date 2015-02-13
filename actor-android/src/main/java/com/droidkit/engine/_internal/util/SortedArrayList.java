package com.droidkit.engine._internal.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * An extension of <code>ArrayList</code> that insures that all of the items
 * added are sorted. <b>This breaks original list contract!</b>.
 * USE ONLY add(E o) AND addAll(Collection) METHODS!
 */
public class SortedArrayList<E> extends ArrayList<E> {

    protected final Comparator<E> comparator;

    /**
     * Constructs a new <code>SortedArrayList</code>.
     */
    public SortedArrayList(Comparator<E> c) {
        comparator = c;
    }


    // ---------------------------------------------------------------- override

    /**
     * Adds an Object to sorted list. Object is inserted at correct place, found
     * using binary search. If the same item exist, it will be put to the end of
     * the range.
     * <p>
     * This method breaks original list contract since objects are not
     * added at the list end, but in sorted manner.
     */
    @Override
    public boolean add(E o) {
        super.add(o);
        sort();
        return true;
    }

    /**
     * Add all of the elements in the given collection to this list.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        super.addAll(c);
        sort();
        return true;
    }


    // ---------------------------------------------------------------- sorting
    public void sort() {
        if (comparator != null) {
            Collections.sort(this, comparator);
        }
    }

}