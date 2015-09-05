package im.actor.core.modules;

import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.ConnectingStateChanged;
import im.actor.core.modules.events.NewSessionCreated;
import im.actor.core.modules.utils.PreferenceApiStorage;
import im.actor.core.network.ActorApi;
import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.Endpoints;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

public class ApiModule extends AbsModule implements BusSubscriber {

    private final ActorApi actorApi;

    public ApiModule(Modules context) {
        super(context);

        this.actorApi = new ActorApi(new Endpoints(context().getConfiguration().getEndpoints()),
                new PreferenceApiStorage(context().getPreferences()),
                new ActorApiCallbackImpl(),
                context().getConfiguration().isEnableNetworkLogging(),
                context().getConfiguration().getMinDelay(),
                context().getConfiguration().getMaxDelay(),
                context().getConfiguration().getMaxFailureCount());

        context.getEvents().subscribe(this, AppVisibleChanged.EVENT);
    }

    public ActorApi getActorApi() {
        return actorApi;
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof AppVisibleChanged) {
            if (((AppVisibleChanged) event).isVisible()) {
                actorApi.forceNetworkCheck();
            }
        }
    }

    private class ActorApiCallbackImpl implements ActorApiCallback {

        @Override
        public void onAuthIdInvalidated() {
            ((Modules) context()).onLoggedOut();
        }

        @Override
        public void onNewSessionCreated() {
            context().getEvents().post(new NewSessionCreated());
        }

        @Override
        public void onUpdateReceived(Object obj) {
            context().getUpdatesModule().onUpdateReceived(obj);
        }

        @Override
        public void onConnectionsChanged(int count) {
            context().getEvents().post(new ConnectingStateChanged(count == 0));
        }
    }
}
