package im.actor.sdk.core;

import im.actor.core.RawUpdatesHandler;
import im.actor.core.providers.RawUpdatesHandlerProvider;
import im.actor.sdk.ActorSDK;

public class AndroidRawUpdateHandlerProvider implements RawUpdatesHandlerProvider {
    @Override
    public RawUpdatesHandler getRawUpdatesHandler() {
        return ActorSDK.sharedActor().getDelegate().getRawUpdatesHandler();
    }
}
