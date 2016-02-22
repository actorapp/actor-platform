package im.actor.core.modules.calls.peers.messages;

public class MediaEnableOutput {

    private boolean isEnabled;

    public MediaEnableOutput(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
