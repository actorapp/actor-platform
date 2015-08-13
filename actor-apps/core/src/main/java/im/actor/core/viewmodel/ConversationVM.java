/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.core.entity.Message;
import im.actor.core.entity.PeerEntity;
import im.actor.core.modules.Modules;
import im.actor.runtime.mvvm.PlatformDisplayList;

public class ConversationVM {
//    private DisplayList<Message> displayList;
//    private DisplayList.Listener listener;
//    private boolean isLoaded = false;

    public ConversationVM(final PeerEntity peer, final ConversationVMCallback callback,
                          final Modules modules, final PlatformDisplayList<Message> displayList) {
//        this.displayList = displayList;
//        this.listener = new DisplayList.Listener() {
//            @Override
//            public void onCollectionChanged() {
//                if (isLoaded) {
//                    return;
//                }
//
//                if (displayList.getSize() == 0) {
//                    return;
//                }
//
//                isLoaded = true;
//                long lastRead = modules.getMessagesModule().loadReadState(peer);
//
//                if (lastRead == 0) {
//                    // Already scrolled to bottom
//                    return;
//                }
//
//                int index = -1;
//                long unread = -1;
//                for (int i = displayList.getSize() - 1; i >= 0; i--) {
//                    Message message = displayList.getItem(i);
//                    if (message.getSenderId() == modules.getAuthModule().myUid()) {
//                        continue;
//                    }
//                    if (message.getSortDate() > lastRead) {
//                        index = i;
//                        unread = message.getRid();
//                        break;
//                    }
//                }
//
//                if (index >= 0) {
//                    callback.onLoaded(unread, index);
//                } else {
//                    callback.onLoaded(0, 0);
//                }
//            }
//        };
//        this.displayList.addListener(listener);
//        listener.onCollectionChanged();
    }

    @ObjectiveCName("releaseVM")
    public void release() {
        // displayList.removeListener(listener);
    }
}
