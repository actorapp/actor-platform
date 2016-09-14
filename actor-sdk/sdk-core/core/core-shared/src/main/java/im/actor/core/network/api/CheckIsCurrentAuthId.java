package im.actor.core.network.api;

import im.actor.runtime.actors.ask.AskMessage;

public class CheckIsCurrentAuthId implements AskMessage<Boolean> {
    private long authId;

    public CheckIsCurrentAuthId(long authId) {
        this.authId = authId;
    }

    public long getAuthId() {
        return authId;
    }
}
