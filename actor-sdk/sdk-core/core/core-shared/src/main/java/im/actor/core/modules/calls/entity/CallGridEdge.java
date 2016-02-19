package im.actor.core.modules.calls.entity;

public class CallGridEdge {

    private final CallNode start;
    private final CallNode end;

    public CallGridEdge(CallNode start, CallNode end) {
        this.start = start;
        this.end = end;
    }

    public CallNode getStart() {
        return start;
    }

    public CallNode getEnd() {
        return end;
    }
}
