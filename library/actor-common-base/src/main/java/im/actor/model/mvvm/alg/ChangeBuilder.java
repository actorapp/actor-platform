/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm.alg;

import java.util.ArrayList;

import im.actor.model.mvvm.ChangeDescription;

public final class ChangeBuilder {

    public static <T> ArrayList<ChangeDescription<T>> processAndroidModifications(
            ArrayList<ChangeDescription<T>> modifications, ArrayList<T> initialList) {
        return modifications;
    }

    public static <T> ArrayList<ChangeDescription<T>> processAppleModifications(
            ArrayList<ChangeDescription<T>> modifications, ArrayList<T> initialList) {
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

        ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();

        // Building deletions
        for (int i = 0; i < states.size(); i++) {
            State<T> s = states.get(i);
            if (s.wasDeleted) {
                res.add(ChangeDescription.<T>remove(s.startingIndex));
            }
        }

        // Building add/update/move
        for (int i = 0; i < current.size(); i++) {
            State<T> s = current.get(i);
            if (s.wasMoved && s.startingIndex != i) {
                res.add(ChangeDescription.<T>move(s.startingIndex, i));
            }
            if (s.wasUpdated) {
                res.add(ChangeDescription.update(s.startingIndex, s.item));
            }
            if (s.wasAdded) {
                res.add(ChangeDescription.add(i, s.item));
            }
        }

        return res;
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
