package im.actor.core.modules.calls.peers;

import im.actor.core.api.ApiPeerSettings;

public class PeerSettings {

    private boolean isPreConnectionEnabled = false;
    private boolean isMobile = false;

    public PeerSettings() {

    }

    public PeerSettings(ApiPeerSettings peerSettings) {
        if (peerSettings != null) {
            if (peerSettings.canPreConnect() != null) {
                isPreConnectionEnabled = peerSettings.canPreConnect();
            }
        }
    }

    public boolean isMobile() {
        return isMobile;
    }

    public boolean isPreConnectionEnabled() {
        return isPreConnectionEnabled;
    }

    public void setIsPreConnectionEnabled(boolean isPreConnectionEnabled) {
        this.isPreConnectionEnabled = isPreConnectionEnabled;
    }

    public ApiPeerSettings toApi() {
        return new ApiPeerSettings(false, isMobile, false, isPreConnectionEnabled);
    }
}
