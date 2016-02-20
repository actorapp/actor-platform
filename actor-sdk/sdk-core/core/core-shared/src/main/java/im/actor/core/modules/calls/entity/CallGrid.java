package im.actor.core.modules.calls.entity;

import java.util.ArrayList;
import java.util.Iterator;

import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.function.Predicate;

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

    public void addEdge(CallNode src, CallNode dest) {
        if (edges.isAny(FIND_EDGE(src, dest))) {
            throw new RuntimeException();
        }
        edges.add(new CallGridEdge(src, dest));
    }

    public void addNode(CallNode node) {
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

    public ArrayList<PendingEdge> calculatePendingEdges() {
        ManagedList<PendingEdge> res = ManagedList.empty();

        for (CallNode src : nodes) {
            if (!src.isAnswered() && !src.isSupportsPreConnection()) {
                continue;
            }
            for (CallNode dest : nodes) {
                if (!dest.isAnswered() && !dest.isSupportsPreConnection()) {
                    continue;
                }
                if (src == dest) {
                    continue;
                }
                if (edges.isAny(FIND_EDGE(src, dest))) {
                    continue;
                }
                if (res.isAny(FIND_PENDING(src, dest))) {
                    continue;
                }
                res.add(new PendingEdge(src, dest));
            }
        }
        return res;
    }

    private Predicate<PendingEdge> FIND_PENDING(final CallNode a, final CallNode b) {
        return new Predicate<PendingEdge>() {
            @Override
            public boolean apply(PendingEdge pendingEdge) {
                return (pendingEdge.getStart() == a && pendingEdge.getEnd() == b)
                        || (pendingEdge.getEnd() == a && pendingEdge.getStart() == b);
            }
        };
    }

    private Predicate<CallGridEdge> FIND_EDGE(final CallNode a, final CallNode b) {
        return new Predicate<CallGridEdge>() {
            @Override
            public boolean apply(CallGridEdge callGridEdge) {
                return (callGridEdge.getStart() == a && callGridEdge.getEnd() == b)
                        || (callGridEdge.getEnd() == a && callGridEdge.getStart() == b);
            }
        };
    }
}