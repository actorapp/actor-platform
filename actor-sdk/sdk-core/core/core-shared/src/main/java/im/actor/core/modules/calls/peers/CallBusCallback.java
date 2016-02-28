package im.actor.core.modules.calls.peers;

public interface CallBusCallback {

    void onBusStarted(String busId);

    void onCallConnected();

    void onCallEnabled();

    void onBusStopped();
}
