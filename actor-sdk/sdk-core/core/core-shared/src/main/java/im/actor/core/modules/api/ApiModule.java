package im.actor.core.modules.api;

import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.events.AppVisibleChanged;
import im.actor.core.events.ConnectingStateChanged;
import im.actor.core.events.NewSessionCreated;
import im.actor.core.network.ActorApi;
import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.AuthKeyStorage;
import im.actor.core.network.Endpoints;
import im.actor.core.network.parser.Request;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

import static im.actor.runtime.actors.ActorSystem.system;

public class ApiModule extends AbsModule implements BusSubscriber {

    private final ActorApi actorApi;
    private final ActorRef persistentRequests;
    private final AuthKeyStorage authKeyStorage;

    public ApiModule(Modules context) {
        super(context);

        this.authKeyStorage = new PreferenceApiStorage(context().getPreferences());

        this.actorApi = new ActorApi(new Endpoints(context().getConfiguration().getEndpoints(),
                context().getConfiguration().getTrustedKeys()),
                authKeyStorage,
                new ActorApiCallbackImpl(),
                context().getConfiguration().isEnableNetworkLogging(),
                context().getConfiguration().getMinDelay(),
                context().getConfiguration().getMaxDelay(),
                context().getConfiguration().getMaxFailureCount());

        context.getEvents().subscribe(this, AppVisibleChanged.EVENT);

        persistentRequests = system().actorOf("api/persistence", () -> {
            return new PersistentRequestsActor(context());
        });
    }

    /**
     * Get Actor API instance
     *
     * @return Actor API instance
     */
    public ActorApi getActorApi() {
        return actorApi;
    }

    /**
     * DANGER. Get Auth Keys storage. Do not use it if you don't understand what it is.
     *
     * @return Auth Key Storage
     */
    public AuthKeyStorage getAuthKeyStorage() {
        return authKeyStorage;
    }

    /**
     * Performing persist request. Keep repeating request even after application restart
     *
     * @param request request
     */
    public void performPersistRequest(Request request) {
        persistentRequests.send(new PersistentRequestsActor.PerformRequest(request));
    }

    /**
     * Perform cursor persist request. Request is performed only if key is bigger than previous
     * request with same name
     *
     * @param name    name of cursor
     * @param key     sorting key
     * @param request request for performing
     */
    public void performPersistCursorRequest(String name, long key, Request request) {
        persistentRequests.send(new PersistentRequestsActor.PerformCursorRequest(name, key, request));
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
            if (context().getUpdatesModule() != null) {
                context().getUpdatesModule().onUpdateReceived(obj);
            }
        }

        @Override
        public void onConnectionsChanged(int count) {
            context().getEvents().post(new ConnectingStateChanged(count == 0));
        }
    }
}
