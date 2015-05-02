/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.concurrency;

public interface CommandCallback<T> {
    public void onResult(T res);

    public void onError(Exception e);
}
