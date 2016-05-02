package im.actor.core.js.modules;

import im.actor.core.js.JsMessenger;
import im.actor.core.js.providers.electron.JsElectronApp;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Cancellable;
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

        idleActor = system().actorOf("js/idle_timer", () -> new IdleActor(messenger, context));
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
        private Cancellable flushCancellable;
        private boolean isElectron = JsElectronApp.isElectron();

        public IdleActor(JsMessenger messenger, ModuleContext context) {
            super(context);
            this.messenger = messenger;
        }

        @Override
        public void preStart() {
            if (!isElectron) {
                JsIdleDetection.subscribe(this);
                scheduleFlush();
            }
        }

        public void onActionDetected() {
            if (!isAppVisible) {
                isAppVisible = true;
                messenger.onAppVisible();
            }
            if (!isElectron) {
                scheduleFlush();
            }
        }

        void scheduleFlush() {
            if (flushCancellable != null) {
                flushCancellable.cancel();
                flushCancellable = null;
            }
            flushCancellable = schedule(new FlushTimeout(), TIMEOUT);
        }

        public void onTimeoutDetected() {
            if (isAppVisible) {
                isAppVisible = false;
                messenger.onAppHidden();
            }
        }

        public void onHidden() {
            onTimeoutDetected();
        }

        public void onVisible() {
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
