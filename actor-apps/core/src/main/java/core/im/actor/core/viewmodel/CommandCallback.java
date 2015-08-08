/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

public interface CommandCallback<T> {
    @ObjectiveCName("onResult:")
    void onResult(T res);

    @ObjectiveCName("onError:")
    void onError(Exception e);
}
