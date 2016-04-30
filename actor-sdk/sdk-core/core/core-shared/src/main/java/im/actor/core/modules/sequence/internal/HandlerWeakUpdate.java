package im.actor.core.modules.sequence.internal;

import org.jetbrains.annotations.NotNull;

import im.actor.core.network.parser.Update;

public class HandlerWeakUpdate {

    @NotNull
    private Update update;
    private long date;

    public HandlerWeakUpdate(@NotNull Update update, long date) {
        this.update = update;
        this.date = date;
    }

    @NotNull
    public Update getUpdate() {
        return update;
    }

    public long getDate() {
        return date;
    }
}
