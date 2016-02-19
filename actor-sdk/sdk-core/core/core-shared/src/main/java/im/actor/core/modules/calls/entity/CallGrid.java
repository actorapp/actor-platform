package im.actor.core.modules.calls.entity;

import java.util.ArrayList;
import java.util.Iterator;

import im.actor.runtime.collections.ManagedList;

public class CallGrid {

    private ManagedList<CallNode> nodes = ManagedList.empty();
    private ManagedList<CallGridEdge> edges = ManagedList.empty();

    public ManagedList<CallNode> getNodes() {
        return nodes;
    }

    public ManagedList<CallNode> getNodes(int uid) {
        return nodes.filter(CallNode.PREDICATE(uid));
    }

    public ManagedList<CallGridEdge> getEdges() {
        return edges;
    }

    public void addNode(CallNode node) {

        //
        // Adding Node
        //
        nodes.add(node);
    }

    public ArrayList<CallGridEdge> removeNode(CallNode node) throws InvalidTransactionException {

        //
        // Removing Node
        //
        if (!nodes.remove(node)) {
            throw new InvalidTransactionException();
        }

        //
        // Searching and removing edges
        //
        ArrayList<CallGridEdge> removedEdges = new ArrayList<>();
        Iterator<CallGridEdge> ei = edges.iterator();
        while (ei.hasNext()) {
            CallGridEdge edge = ei.next();
            if (edge.getStart() == node || edge.getEnd() == node) {
                ei.remove();
                removedEdges.add(edge);
            }
        }
        return removedEdges;
    }
}