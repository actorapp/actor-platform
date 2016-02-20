package im.actor.core.modules.calls.entity;

public class PendingEdge {

    private CallNode start;
    private CallNode end;

    public PendingEdge(CallNode start, CallNode end) {
        this.start = start;
        this.end = end;
    }

    public CallNode getStart() {
        return start;
    }

    public CallNode getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + start.getDeviceId() + "->" + end.getDeviceId() + "]";
    }
}
