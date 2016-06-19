package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.webrtc.WebRTCMediaStream;

public class CallMediaSource {

    @Property("nonatomic, readonly")
    private long deviceId;
    @Property("nonatomic, readonly")
    private boolean isVideoEnabled;
    @Property("nonatomic, readonly")
    private boolean isAudioEnabled;
    @Property("nonatomic, readonly")
    private WebRTCMediaStream stream;

    public CallMediaSource(long deviceId, boolean isAudioEnabled, boolean isVideoEnabled, WebRTCMediaStream stream) {
        this.deviceId = deviceId;
        this.isVideoEnabled = isVideoEnabled;
        this.isAudioEnabled = isAudioEnabled;
        this.stream = stream;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public boolean isVideoEnabled() {
        return isVideoEnabled;
    }

    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    public WebRTCMediaStream getStream() {
        return stream;
    }
}
