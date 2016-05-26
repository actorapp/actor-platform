package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.Property;

public class WebRTCSettings {

    @Property("nonatomic, readonly")
    private boolean is3DESEnabled;
    @Property("nonatomic, readonly")
    private boolean isDataChannelsEnabled;
    @Property("nonatomic, readonly")
    private boolean isVideoEnabled;

    public WebRTCSettings(boolean is3DESEnabled, boolean isDataChannelsEnabled, boolean isVideoEnabled) {
        this.is3DESEnabled = is3DESEnabled;
        this.isDataChannelsEnabled = isDataChannelsEnabled;
        this.isVideoEnabled = isVideoEnabled;
    }

    public boolean is3DESEnabled() {
        return is3DESEnabled;
    }

    public boolean isDataChannelsEnabled() {
        return isDataChannelsEnabled;
    }

    public boolean isVideoEnabled() {
        return isVideoEnabled;
    }
}