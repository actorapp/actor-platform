package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.generics.ArrayListMediaTrack;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.runtime.mvvm.ValueModel;

public class CallVM {

    @Property("nonatomic, readonly")
    private final long callId;
    @Property("nonatomic, readonly")
    private final Peer peer;
    @Property("nonatomic, readonly")
    private final ValueModel<CallState> state;

    // Own Stream

    @Property("nonatomic, readonly")
    private final ValueModel<ArrayListMediaTrack> ownVideoTracks;
    @Property("nonatomic, readonly")
    private final ValueModel<ArrayListMediaTrack> ownAudioTracks;

    @Property("nonatomic, readonly")
    private final ValueModel<ArrayListMediaTrack> theirVideoTracks;
    @Property("nonatomic, readonly")
    private final ValueModel<ArrayListMediaTrack> theirAudioTracks;


    @Property("nonatomic, readonly")
    private final BooleanValueModel isOwnAudioEnabled;
    @Property("nonatomic, readonly")
    private final BooleanValueModel isOwnVideoEnabled;

    @Property("nonatomic, readonly")
    private final ValueModel<ArrayList<CallMember>> members;
    @Property("nonatomic, readonly")
    private long callStart;
    @Property("nonatomic, readonly")
    private long callEnd;
    @Property("nonatomic, readonly")
    private final boolean isOutgoing;

    public CallVM(long callId, Peer peer, boolean isOutgoing, boolean isVideoEnabled, ArrayList<CallMember> initialMembers, CallState state) {
        this.callId = callId;
        this.peer = peer;
        this.isOutgoing = isOutgoing;
        this.state = new ValueModel<>("calls." + callId + ".state", state);
        this.ownVideoTracks = new ValueModel<>("calls." + callId + ".own_video", new ArrayListMediaTrack());
        this.ownAudioTracks = new ValueModel<>("calls." + callId + ".own_audio", new ArrayListMediaTrack());
        this.theirVideoTracks = new ValueModel<>("calls." + callId + ".their_video", new ArrayListMediaTrack());
        this.theirAudioTracks = new ValueModel<>("calls." + callId + ".their_audio", new ArrayListMediaTrack());
        this.members = new ValueModel<>("calls." + callId + ".members", new ArrayList<>(initialMembers));
        this.isOwnAudioEnabled = new BooleanValueModel("calls." + callId + ".audio_enabled", true);
        this.isOwnVideoEnabled = new BooleanValueModel("calls." + callId + ".video_enabled", isVideoEnabled);
        this.callStart = 0;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    public Peer getPeer() {
        return peer;
    }

    public long getCallId() {
        return callId;
    }

    public BooleanValueModel getIsOwnAudioEnabled() {
        return isOwnAudioEnabled;
    }

    public BooleanValueModel getIsOwnVideoEnabled() {
        return isOwnVideoEnabled;
    }

    public ValueModel<CallState> getState() {
        return state;
    }

    public ValueModel<ArrayList<CallMember>> getMembers() {
        return members;
    }

    public void setCallStart(long callStart) {
        this.callStart = callStart;
    }

    public long getCallStart() {
        return callStart;
    }

    public long getCallEnd() {
        return callEnd;
    }

    public void setCallEnd(long callEnd) {
        this.callEnd = callEnd;
    }

    public ValueModel<ArrayListMediaTrack> getOwnVideoTracks() {
        return ownVideoTracks;
    }

    public ValueModel<ArrayListMediaTrack> getOwnAudioTracks() {
        return ownAudioTracks;
    }

    public ValueModel<ArrayListMediaTrack> getTheirVideoTracks() {
        return theirVideoTracks;
    }

    public ValueModel<ArrayListMediaTrack> getTheirAudioTracks() {
        return theirAudioTracks;
    }
}