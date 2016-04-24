package im.actor.core.modules.eventbus;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestCreateNewEventBus;
import im.actor.core.api.rpc.RequestJoinEventBus;
import im.actor.core.api.rpc.RequestKeepAliveEventBus;
import im.actor.core.api.rpc.RequestPostToEventBus;
import im.actor.core.api.rpc.ResponseCreateNewEventBus;
import im.actor.core.api.rpc.ResponseJoinEventBus;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.ModuleContext;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.function.Consumer;

public class EventBusActor extends ModuleActor {

    private static final long DEFAULT_TIMEOUT = 16000;

    private boolean isProcessing;
    private String busId;
    private long deviceId;

    private long keepAliveTimeout;
    private long keepAliveRetry;

    private Cancellable keepAliveCancel;
    private long keepAliveRequest = -1;

    public EventBusActor(ModuleContext context) {
        super(context);
    }

    public String getBusId() {
        return busId;
    }

    public long getDeviceId() {
        return deviceId;
    }


    public void joinBus(final String busId) {
        joinBus(busId, DEFAULT_TIMEOUT);
    }

    public void joinBus(final String busId, final long timeout) {
        isProcessing = true;
        api(new RequestJoinEventBus(busId, timeout)).then(responseJoinEventBus ->
                connectBus(busId, responseJoinEventBus.getDeviceId(), timeout, true)
        ).failure(e -> dispose());
    }

    public void createBus() {
        createBus(DEFAULT_TIMEOUT);
    }

    public void createBus(final long timeout) {
        isProcessing = true;
        api(new RequestCreateNewEventBus(timeout, true)).then(responseCreateNewEventBus ->
                connectBus(responseCreateNewEventBus.getId(), responseCreateNewEventBus.getDeviceId(), timeout, false)
        ).failure(e -> dispose());
    }

    public void connectBus(String busId, long deviceId, boolean isJoined) {
        connectBus(busId, deviceId, DEFAULT_TIMEOUT, isJoined);
    }

    public void connectBus(String busId, long deviceId, long timeout, boolean isJoined) {
        keepAliveTimeout = timeout;
        keepAliveRetry = timeout / 2;
        this.busId = busId;
        this.deviceId = deviceId;
        context().getEventBus().subscribe(busId, self());
        if (isJoined) {
            onBusJoined();
        } else {
            onBusCreated();
        }
        onBusStarted();
        isProcessing = false;
        unstashAll();
        keepAliveCancel = schedule(new KeepAlive(), keepAliveRetry);
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

    public void sendMessage(byte[] data) {
        request(new RequestPostToEventBus(busId, new ArrayList<>(), data));
    }

    public void sendMessage(ArrayList<Long> deviceIds, byte[] data) {
        request(new RequestPostToEventBus(busId, deviceIds, data));
    }

    public void sendMessage(long deviceId, byte[] data) {
        ArrayList<Long> devices = new ArrayList<>();
        devices.add(deviceId);
        request(new RequestPostToEventBus(busId, devices, data));
    }


    //
    // Keep Alive
    //

    private void doKeepAlive() {
        stopKeepAlive();
        keepAliveRequest = request(new RequestKeepAliveEventBus(busId, keepAliveTimeout), new RpcCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid response) {
                // Do Nothing
            }

            @Override
            public void onError(RpcException e) {
                dispose();
            }
        });
        keepAliveCancel = schedule(new KeepAlive(), keepAliveRetry);
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
        if (busId != null) {
            context().getEventBus().unsubscribe(busId, self());
        }
        onBusShutdown();
        onBusStopped();
    }

    public void dispose() {
        isProcessing = true;
        stopKeepAlive();
        if (busId != null) {
            context().getEventBus().unsubscribe(busId, self());
        }
        onBusDisposed();
        onBusStopped();
        busId = null;
        self().send(PoisonPill.INSTANCE);
    }

    @Override
    public void postStop() {
        super.postStop();
        if (busId != null) {
            request(new RequestKeepAliveEventBus(busId, 1L));
        }
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
