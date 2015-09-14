package im.actor.core.modules;

import im.actor.core.modules.api.PersistentRequestsActor;
import im.actor.core.modules.events.AppVisibleChanged;
import im.actor.core.modules.events.ConnectingStateChanged;
import im.actor.core.modules.events.NewSessionCreated;
import im.actor.core.modules.utils.PreferenceApiStorage;
import im.actor.core.network.ActorApi;
import im.actor.core.network.ActorApiCallback;
import im.actor.core.network.Endpoints;
import im.actor.core.network.parser.Request;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

import static im.actor.runtime.actors.ActorSystem.system;

public class ApiModule extends AbsModule implements BusSubscriber {

    private final ActorApi actorApi;
    private final ActorRef persistentRequests;

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

        persistentRequests = system().actorOf(Props.create(PersistentRequestsActor.class, new ActorCreator<PersistentRequestsActor>() {
            @Override
            public PersistentRequestsActor create() {
                return new PersistentRequestsActor(context());
            }
        }), "api/persistence");
    }


    public ActorApi getActorApi() {
        return actorApi;
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
            context().getUpdatesModule().onUpdateReceived(obj);
        }

        @Override
        public void onConnectionsChanged(int count) {
            context().getEvents().post(new ConnectingStateChanged(count == 0));
        }
    }
}
