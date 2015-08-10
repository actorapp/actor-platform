/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

public interface ConversationVMCallback {
    void onLoaded(long unreadId, int index);
}
