/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import junit.framework.TestCase;

import java.util.ArrayList;

import im.actor.model.mvvm.alg.ChangeBuilder;

public class ChangeBuilderTest extends TestCase {

    private ArrayList<Integer> prepareList(int amount) {
        ArrayList<Integer> workList = new ArrayList<Integer>();
        for (int i = 0; i < amount; i++) {
            workList.add(i);
        }
        return workList;
    }

    private void assertHasOperation(ChangeDescription<Integer> op,
                                    ArrayList<ChangeDescription<Integer>> list) {
        for (ChangeDescription<Integer> i : list) {
            if (i.getOperationType() == op.getOperationType()) {
                if (op.getOperationType() == ChangeDescription.OperationType.REMOVE) {
                    if (op.getIndex() == i.getIndex()) {
                        return;
                    }
                } else if (op.getOperationType() == ChangeDescription.OperationType.ADD) {
                    if (op.getIndex() == i.getIndex()) {
                        return;
                    }
                }
            }
        }
        assertTrue(false);
    }

    public void testIOSDeletions() {
        // Initial List: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        ArrayList<Integer> workList = prepareList(10);

        // Result List: 0, 1, 2, 5, 6, 7, 8, 9
        ArrayList<Integer> resultList = prepareList(10);
        resultList.remove(3);
        resultList.remove(4);

        ArrayList<ChangeDescription<Integer>> operations = new ArrayList<ChangeDescription<Integer>>();
        operations.add(ChangeDescription.<Integer>remove(3));
        operations.add(ChangeDescription.<Integer>remove(3));

        ArrayList<ChangeDescription<Integer>> res =
                ChangeBuilder.processAppleModifications(operations, workList);

        assertHasOperation(ChangeDescription.<Integer>remove(3), res);
        assertHasOperation(ChangeDescription.<Integer>remove(4), res);
        assertEquals(res.size(), 2);
    }

    public void testIOSAdditions() {
        // Initial List: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        ArrayList<Integer> workList = prepareList(10);

        // Result List: 0, 1, 2, 3, 11, 10, 4, 5, 6, 7, 8, 9
        ArrayList<Integer> resultList = prepareList(10);
        resultList.add(4, 10);
        resultList.add(4, 11);

        ArrayList<ChangeDescription<Integer>> operations = new ArrayList<ChangeDescription<Integer>>();
        operations.add(ChangeDescription.add(4, 10));
        operations.add(ChangeDescription.add(4, 11));

        ArrayList<ChangeDescription<Integer>> res =
                ChangeBuilder.processAppleModifications(operations, workList);
        assertHasOperation(ChangeDescription.add(4, 10), res);
        assertHasOperation(ChangeDescription.add(5, 11), res);
    }
}