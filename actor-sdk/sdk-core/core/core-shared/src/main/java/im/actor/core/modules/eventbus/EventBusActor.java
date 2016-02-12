package im.actor.core.modules.eventbus;

import org.jetbrains.annotations.Nullable;

import im.actor.core.api.rpc.RequestCreateNewEventBus;
import im.actor.core.api.rpc.RequestJoinEventBus;
import im.actor.core.api.rpc.RequestKeepAliveEventBus;
import im.actor.core.api.rpc.ResponseCreateNewEventBus;
import im.actor.core.api.rpc.ResponseJoinEventBus;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;

public class EventBusActor extends ModuleActor {

    private static final long TIMEOUT = 15000;
    private static final long KEEP_ALIVE = 5000;

    private boolean isProcessing;
    private String busId;
    private long deviceId;

    private Cancellable keepAliveCancel;
    private long keepAliveRequest = -1;

    public EventBusActor(ModuleContext context) {
        super(context);
    }

    public EventBusActor(String busId, ModuleContext context) {
        super(context);
        this.busId = busId;
    }

    public String getBusId() {
        return busId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    //
    // Start
    //

    @Override
    public void preStart() {
        super.preStart();

        isProcessing = true;
        if (busId != null) {
            api(new RequestJoinEventBus(busId, TIMEOUT)).then(new Consumer<ResponseJoinEventBus>() {
                @Override
                public void apply(ResponseJoinEventBus responseJoinEventBus) {
                    deviceId = responseJoinEventBus.getDeviceId();
                    context().getEventBus().subscribe(busId, self());
                    onBusJoined();
                    onBusStarted();
                    isProcessing = false;
                    unstashAll();
                    keepAliveCancel = schedule(new KeepAlive(), KEEP_ALIVE);
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    dispose();
                }
            }).done(self());
        } else {
            api(new RequestCreateNewEventBus(TIMEOUT, true)).then(new Consumer<ResponseCreateNewEventBus>() {
                @Override
                public void apply(ResponseCreateNewEventBus responseCreateNewEventBus) {
                    busId = responseCreateNewEventBus.getId();
                    deviceId = responseCreateNewEventBus.getDeviceId();
                    context().getEventBus().subscribe(busId, self());
                    onBusCreated();
                    onBusStarted();
                    isProcessing = false;
                    unstashAll();
                    keepAliveCancel = schedule(new KeepAlive(), KEEP_ALIVE);
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {
                    dispose();
                }
            }).done(self());
        }
    }


    //
    // Processing
    //

    public void onBusCreated() {

    }

    public void onBusJoined() {

    }

    public void onBusStarted() {

    }


    public void onDeviceConnected(int uid, long deviceId) {

    }

    public void onDeviceDisconnected(int uid, long deviceId) {

    }

    public void onMessageReceived(@Nullable Integer senderId, @Nullable Long senderDeviceId, byte[] data) {

    }


    public void onBusShutdown() {

    }

    public void onBusDisposed() {

    }

    public void onBusStopped() {

    }


    //
    // Keep Alive
    //

    private void doKeepAlive() {
        stopKeepAlive();
        keepAliveRequest = request(new RequestKeepAliveEventBus(busId, TIMEOUT), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                // Do Nothing
            }

            @Override
            public void onError(RpcException e) {
                dispose();
            }
        });
        keepAliveCancel = schedule(new KeepAlive(), KEEP_ALIVE);
    }

    private void stopKeepAlive() {
        if (keepAliveRequest != -1) {
            context().getActorApi().cancelRequest(keepAliveRequest);
            keepAliveRequest = -1;
        }
        if (keepAliveCancel != null) {
            keepAliveCancel.cancel();
            keepAliveCancel = null;
        }
    }

    //
    // Shutdown
    //

    public void shutdown() {
        isProcessing = true;
        stopKeepAlive();
        context().getEventBus().unsubscribe(busId, self());
        onBusShutdown();
        onBusStopped();
        api(new RequestKeepAliveEventBus(busId, 1L)).then(new Consumer<ResponseVoid>() {
            @Override
            public void apply(ResponseVoid responseVoid) {
                self().send(PoisonPill.INSTANCE);
            }
        }).done(self());
    }

    public void dispose() {
        isProcessing = true;
        stopKeepAlive();
        context().getEventBus().unsubscribe(busId, self());
        onBusDisposed();
        onBusStopped();
        self().send(PoisonPill.INSTANCE);
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof EventBusShutdown) {
            if (isProcessing) {
                stash();
                return;
            }
            shutdown();
        } else if (message instanceof EventBusDisposed) {
            if (isProcessing) {
                stash();
                return;
            }
            dispose();
        } else if (message instanceof EventBusDeviceConnected) {
            if (isProcessing) {
                stash();
                return;
            }
            EventBusDeviceConnected deviceConnected = (EventBusDeviceConnected) message;
            onDeviceConnected(deviceConnected.getUid(), deviceConnected.getDeviceId());
        } else if (message instanceof EventBusDeviceDisconnected) {
            if (isProcessing) {
                stash();
                return;
            }
            EventBusDeviceDisconnected deviceDisconnected = (EventBusDeviceDisconnected) message;
            onDeviceDisconnected(deviceDisconnected.getUid(), deviceDisconnected.getDeviceId());
        } else if (message instanceof EventBusMessage) {
            if (isProcessing) {
                stash();
                return;
            }
            EventBusMessage eventBusMessage = (EventBusMessage) message;
            onMessageReceived(eventBusMessage.getSenderId(), eventBusMessage.getSenderDeviceId(),
                    eventBusMessage.getMessage());
        } else if (message instanceof KeepAlive) {
            if (isProcessing) {
                stash();
                return;
            }
            doKeepAlive();
        } else {
            super.onReceive(message);
        }
    }

    public static class EventBusShutdown {

    }

    public static class EventBusDisposed {

    }

    public static class EventBusDeviceConnected {

        private int uid;
        private long deviceId;

        public EventBusDeviceConnected(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class EventBusDeviceDisconnected {

        private int uid;
        private long deviceId;

        public EventBusDeviceDisconnected(int uid, long deviceId) {
            this.uid = uid;
            this.deviceId = deviceId;
        }

        public int getUid() {
            return uid;
        }

        public long getDeviceId() {
            return deviceId;
        }
    }

    public static class EventBusMessage {

        private Integer senderId;
        private Long senderDeviceId;
        private byte[] message;

        public EventBusMessage(Integer senderId, Long senderDeviceId, byte[] message) {
            this.senderId = senderId;
            this.senderDeviceId = senderDeviceId;
            this.message = message;
        }

        public Integer getSenderId() {
            return senderId;
        }

        public Long getSenderDeviceId() {
            return senderDeviceId;
        }

        public byte[] getMessage() {
            return message;
        }
    }

    private static class KeepAlive {

    }
}
