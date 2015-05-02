/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.concurrency;

public interface Command<T> {
    public void start(CommandCallback<T> callback);
}
