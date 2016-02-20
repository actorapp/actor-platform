package im.actor.code.modules.calls.entity;

import org.junit.Test;

import im.actor.core.api.ApiPeerSettings;
import im.actor.core.modules.calls.entity.CallGrid;
import im.actor.core.modules.calls.entity.CallNode;
import im.actor.core.modules.calls.entity.CallNodeState;

import static junit.framework.TestCase.assertEquals;

public class CallGridTests {

    @Test
    public void doTest() {
        CallGrid callGrid = new CallGrid();

        CallNode a0 = new CallNode(null, 0);
        CallNode a1 = new CallNode(null, 1);
        CallNode a2 = new CallNode(null, 2);
        CallNode a3 = new CallNode(null, 3);
        CallNode a4 = new CallNode(null, 4);
        CallNode a5 = new CallNode(null, 5);

        callGrid.addNode(a0);
        callGrid.addNode(a1);
        callGrid.addNode(a2);
        callGrid.addNode(a3);
        callGrid.addNode(a4);
        callGrid.addNode(a5);

        // No Cells are answered or ready for pre connection
        assertEquals(0, callGrid.calculatePendingEdges().size());

        a1.setPeerSettings(new ApiPeerSettings(false, false, false, true));
        a0.setPeerSettings(new ApiPeerSettings(false, false, false, true));
        assertEquals(1, callGrid.calculatePendingEdges().size());
        a2.setDeviceState(CallNodeState.CONNECTING);
        assertEquals(3, callGrid.calculatePendingEdges().size());
    }
}
