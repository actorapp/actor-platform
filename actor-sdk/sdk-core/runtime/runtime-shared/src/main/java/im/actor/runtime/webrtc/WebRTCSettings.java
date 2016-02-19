package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.Property;

public class WebRTCSettings {

    @Property("nonatomic, readonly")
    private boolean is3DESEnabled;
    @Property("nonatomic, readonly")
    private boolean isDataChannelsEnabled;

    public WebRTCSettings(boolean is3DESEnabled, boolean isDataChannelsEnabled) {
        this.is3DESEnabled = is3DESEnabled;
        this.isDataChannelsEnabled = isDataChannelsEnabled;
    }

    public boolean is3DESEnabled() {
        return is3DESEnabled;
    }

    public boolean isDataChannelsEnabled() {
        return isDataChannelsEnabled;
    }
}