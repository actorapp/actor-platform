package im.actor.core.js.modules;

import im.actor.core.js.JsMessenger;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class JsIdleModule extends AbsModule {

    private static final long TIMEOUT = 3 * 60 * 1000/*3 min*/;

    private JsMessenger messenger;
    private ActorRef idleActor;

    public JsIdleModule(final JsMessenger messenger, final ModuleContext context) {
        super(context);

        this.messenger = messenger;

        this.messenger.onAppVisible();

        idleActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public IdleActor create() {
                return new IdleActor(messenger, context);
            }
        }), "js/idle_timer");
    }

    public void onHidden() {
        idleActor.send(new IdleActor.OnHidden());
    }

    public void onVisible() {
        idleActor.send(new IdleActor.OnVisible());
    }

    private static class IdleActor extends ModuleActor implements JsIdleCallback {

        private boolean isAppVisible = true;
        private JsMessenger messenger;

        public IdleActor(JsMessenger messenger, ModuleContext context) {
            super(context);
            this.messenger = messenger;
        }

        @Override
        public void preStart() {
            // Log.d("JsIdle", "preStart");
            JsIdleDetection.subscribe(this);
            self().sendOnce(new FlushTimeout(), TIMEOUT);
        }

        public void onActionDetected() {
            // Log.d("JsIdle", "onActionDetected");
            if (!isAppVisible) {
                isAppVisible = true;
                messenger.onAppVisible();
            }
            self().sendOnce(new FlushTimeout(), TIMEOUT);
        }

        public void onTimeoutDetected() {
            // Log.d("JsIdle", "onTimeoutDetected");
            if (isAppVisible) {
                isAppVisible = false;
                messenger.onAppHidden();
            }
        }

        public void onHidden() {
            // Log.d("JsIdle", "onHidden");
            onTimeoutDetected();
        }

        public void onVisible() {
            // Log.d("JsIdle", "onVisible");
            onActionDetected();
        }

        @Override
        public void onReceive(Object message) {
            if (message instanceof FlushTimeout) {
                onTimeoutDetected();
            } else if (message instanceof OnHidden) {
                onHidden();
            } else if (message instanceof OnVisible) {
                onVisible();
            } else {
                super.onReceive(message);
            }
        }

        private class FlushTimeout {

        }

        public static class OnHidden {

        }

        public static class OnVisible {

        }
    }
}
