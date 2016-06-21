package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.webrtc.WebRTCMediaStream;
import im.actor.runtime.webrtc.WebRTCMediaTrack;
import im.actor.runtime.webrtc.WebRTCPeerConnection;

public class CallVM {

    @Property("nonatomic, readonly")
    private final long callId;
    @Property("nonatomic, readonly")
    private final Peer peer;
    @Property("nonatomic, readonly")
    private final ValueModel<CallState> state;

    // Own Stream

    @Property("nonatomic, readonly")
    private final ValueModel<ArrayList<WebRTCMediaTrack>> ownVideoTracks;
    @Property("nonatomic, readonly")
    private final ValueModel<ArrayList<WebRTCMediaTrack>> ownAudioTracks;

    @Property("nonatomic, readonly")
    private final ValueModel<ArrayList<WebRTCMediaTrack>> theirVideoTracks;
    @Property("nonatomic, readonly")
    private final ValueModel<ArrayList<WebRTCMediaTrack>> theirAudioTracks;


    @Property("nonatomic, readonly")
    private final BooleanValueModel isAudioEnabled;
    @Property("nonatomic, readonly")
    private final BooleanValueModel isVideoEnabled;

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
        this.ownVideoTracks = new ValueModel<>("calls." + callId + ".own_video", new ArrayList<>());
        this.ownAudioTracks = new ValueModel<>("calls." + callId + ".own_audio", new ArrayList<>());
        this.theirVideoTracks = new ValueModel<>("calls." + callId + ".their_video", new ArrayList<>());
        this.theirAudioTracks = new ValueModel<>("calls." + callId + ".their_audio", new ArrayList<>());
        this.members = new ValueModel<>("calls." + callId + ".members", new ArrayList<>(initialMembers));
        this.isAudioEnabled = new BooleanValueModel("calls." + callId + ".audio_enabled", true);
        this.isVideoEnabled = new BooleanValueModel("calls." + callId + ".video_enabled", isVideoEnabled);
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

    public BooleanValueModel getIsAudioEnabled() {
        return isAudioEnabled;
    }

    public BooleanValueModel getIsVideoEnabled() {
        return isVideoEnabled;
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

    public ValueModel<ArrayList<WebRTCMediaTrack>> getOwnVideoTracks() {
        return ownVideoTracks;
    }

    public ValueModel<ArrayList<WebRTCMediaTrack>> getOwnAudioTracks() {
        return ownAudioTracks;
    }

    public ValueModel<ArrayList<WebRTCMediaTrack>> getTheirVideoTracks() {
        return theirVideoTracks;
    }

    public ValueModel<ArrayList<WebRTCMediaTrack>> getTheirAudioTracks() {
        return theirAudioTracks;
    }
}