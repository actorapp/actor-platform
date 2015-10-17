/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm.alg;

import java.util.ArrayList;

import im.actor.runtime.generic.mvvm.AppleListUpdate;
import im.actor.runtime.generic.mvvm.ChangeDescription;

public final class ChangeBuilder {

    private static <T> ArrayList<ChangeDescription<T>> optimize(ArrayList<ChangeDescription<T>> modifications) {
        ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
        ChangeDescription<T> desc = null;
        for (ChangeDescription<T> d : modifications) {

            if (d.getOperationType() == ChangeDescription.OperationType.ADD) {
                if (desc != null) {
                    if (desc.getOperationType() == ChangeDescription.OperationType.ADD) {
                        if (desc.getIndex() + desc.getLength() == d.getIndex()) {
                            desc = ChangeDescription.mergeAdd(desc, d);
                        } else {
                            res.add(desc);
                            desc = null;
                        }
                    } else {
                        res.add(desc);
                        desc = null;
                    }
                }
            } else {
                if (desc != null) {
                    res.add(desc);
                    desc = null;
                }
            }

            if (desc == null) {
                desc = d;
                // res.add(d);
            }
        }
        if (desc != null) {
            res.add(desc);
        }
        return res;
    }

    public static <T> ArrayList<ChangeDescription<T>> processAndroidModifications(
            ArrayList<ChangeDescription<T>> modifications, ArrayList<T> initialList) {
        modifications = optimize(modifications);

        return modifications;
    }

    public static <T> AppleListUpdate processAppleModifications(
            ArrayList<ChangeDescription<T>> modifications, ArrayList<T> initialList, boolean isLoadMore) {

        modifications = optimize(modifications);

        ArrayList<State<T>> states = new ArrayList<State<T>>();
        ArrayList<State<T>> current = new ArrayList<State<T>>();

        for (int i = 0; i < initialList.size(); i++) {
            State<T> state = new State<T>();
            state.startingIndex = i;
            states.add(state);
            current.add(state);
        }
        for (ChangeDescription<T> m : modifications) {
            if (m.getOperationType() == ChangeDescription.OperationType.REMOVE) {
                for (int i = 0; i < m.getLength(); i++) {
                    State<T> state = current.remove(m.getIndex());
                    state.wasDeleted = true;
                }
            } else if (m.getOperationType() == ChangeDescription.OperationType.ADD) {
                int index = m.getIndex();
                for (T itm : m.getItems()) {
                    State<T> state = new State<T>();
                    state.wasAdded = true;
                    state.item = itm;
                    current.add(index++, state);
                }
            } else if (m.getOperationType() == ChangeDescription.OperationType.MOVE) {
                State<T> state = current.remove(m.getIndex());
                current.add(m.getDestIndex(), state);
                if (!state.wasAdded) {
                    state.wasMoved = true;
                }
            } else if (m.getOperationType() == ChangeDescription.OperationType.UPDATE) {
                int index = m.getIndex();
                for (T itm : m.getItems()) {
                    State<T> state = current.get(index++);
                    state.item = itm;
                    if (!state.wasAdded) {
                        state.wasUpdated = true;
                    }
                }
            }
        }

        ArrayList<Integer> removed = new ArrayList<Integer>();
        ArrayList<Move> moved = new ArrayList<Move>();
        ArrayList<Integer> added = new ArrayList<Integer>();
        ArrayList<Integer> updated = new ArrayList<Integer>();

        // Building deletions
        for (int i = 0; i < states.size(); i++) {
            State<T> s = states.get(i);
            if (s.wasDeleted) {
                removed.add(s.startingIndex);
            }
        }

        // Building add/update/move
        for (int i = 0; i < current.size(); i++) {
            State<T> s = current.get(i);
            if (s.wasMoved && s.startingIndex != i) {
                moved.add(new Move(s.startingIndex, i));
            }
            if (s.wasUpdated) {
                updated.add(i);
            }
            if (s.wasAdded) {
                added.add(i);
            }
        }

        return new AppleListUpdate(removed, added, moved, updated, isLoadMore);
    }

    private ChangeBuilder() {

    }

    private static class State<T> {
        private boolean wasDeleted;
        private int startingIndex = -1;
        private boolean wasUpdated;
        private boolean wasMoved;
        private boolean wasAdded;
        private T item;
    }
}
