package im.actor.core.modules.calls.peers;

import org.jetbrains.annotations.NotNull;

public interface CallBusCallback {

    void onBusStarted(@NotNull String busId);

    void onCallConnected();

    void onCallEnabled();

    void onBusStopped();
}
