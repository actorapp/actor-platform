package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.Property;

/**
 * WebRTC Features
 */
public class WebRTCFeatures {

    @Property("nonatomic")
    private boolean is3DESSupported = false;

    /**
     * If 3DES supported
     *
     * @return is 3DES supported
     */
    public boolean is3DESSupported() {
        return is3DESSupported;
    }

    /**
     * Setting 3DES support flag
     *
     * @param is3DESSupported set if 3DES supported
     */
    public void setIs3DESSupported(boolean is3DESSupported) {
        this.is3DESSupported = is3DESSupported;
    }
}
