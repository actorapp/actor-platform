/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.notification;

public interface PushSubscribeResult {
    void onSubscribedChrome(String token);

    void onSubscriptionFailure();
}
