package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

public class WebRTCSessionDescription {

    @NotNull
    @Property("nonatomic, readonly")
    private final String sdp;
    @NotNull
    @Property("nonatomic, readonly")
    private final String type;

    @ObjectiveCName("initWithType:withSDP:")
    public WebRTCSessionDescription(@NotNull String type, @NotNull String sdp) {
        this.type = type;
        this.sdp = sdp;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getSdp() {
        return sdp;
    }
}
