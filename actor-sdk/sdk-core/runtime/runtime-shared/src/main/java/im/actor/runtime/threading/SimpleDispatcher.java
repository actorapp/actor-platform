package im.actor.runtime.threading;

import org.jetbrains.annotations.NotNull;

public interface SimpleDispatcher {
    void dispatch(@NotNull Runnable runnable);
}
