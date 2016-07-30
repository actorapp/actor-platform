package im.actor.core.modules.messaging.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.entity.DestructPendingMessage;
import im.actor.core.modules.messaging.actions.entity.DestructPendingStorage;
import im.actor.core.modules.messaging.actions.entity.DestructQueueMessage;
import im.actor.core.modules.messaging.actions.entity.DestructQueueStorage;
import im.actor.core.modules.messaging.actions.entity.MessageDesc;
import im.actor.core.util.JavaUtil;
import im.actor.runtime.Runtime;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCancellable;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;
import im.actor.runtime.storage.KeyValueStorage;

public class DestructorActor extends ModuleActor {

    private static final int DEFAULT_DELAY = 15000;

    private KeyValueStorage keyValueStorage;
    private DestructQueueStorage destructQueueStorage = new DestructQueueStorage();
    private ActorCancellable checkCancellable = null;
    private boolean isDestructing = false;

    public DestructorActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();

        keyValueStorage = Storage.createKeyValue("self_destruct");

        // Load Destruction Queue
        byte[] data = keyValueStorage.loadItem(0);
        if (data != null) {
            destructQueueStorage = DestructQueueStorage.fromBytes(data);
        }

        scheduleCheck();
    }

    //
    // Receiving messages
    //

    public Promise<Void> onMessages(Peer peer, List<MessageDesc> messages) {
        DestructPendingStorage storage = getStorage(peer);
        if (storage == null) {
            storage = new DestructPendingStorage();
        }
        for (MessageDesc d : messages) {
            DestructPendingMessage p = new DestructPendingMessage(d.getRid(),
                    d.getDate(), d.isOut(), d.getTimer());
            if (d.isNeedExplicitRead()) {
                storage.getIndividualMessages().add(p);
            } else {
                storage.getMessages().add(p);
            }
        }
        saveStorage(peer, storage);
        return Promise.success(null);
    }

    //
    // Reading and starting self-destroy
    //

    public Promise<Void> onMessageRead(Peer peer, long readDate) {
        return onMessageRead(peer, readDate, true);
    }

    public Promise<Void> onMessageReadByMe(Peer peer, long readDate) {
        return onMessageRead(peer, readDate, false);
    }

    public Promise<Void> onMessageRead(Peer peer, long readDate, boolean isOut) {
        DestructPendingStorage pendingStorage = getStorage(peer);
        if (pendingStorage != null) {
            ArrayList<DestructPendingMessage> queue = new ArrayList<>();
            for (DestructPendingMessage dp : pendingStorage.getMessages()) {
                if (dp.isOut() == isOut && dp.getDate() <= readDate) {
                    queue.add(dp);
                }
            }

            if (queue.size() > 0) {

                // Adding to queue
                for (DestructPendingMessage dp : queue) {
                    destructQueueStorage.getQueue().add(new DestructQueueMessage(peer,
                            dp.getRid(), dp.getTimer() + readDate));
                }
                Collections.sort(destructQueueStorage.getQueue(), (destructQueueMessage, t1) -> {
                    return JavaUtil.compare(destructQueueMessage.getDestructDate(), t1.getDestructDate());
                });
                keyValueStorage.addOrUpdateItem(0, destructQueueStorage.toByteArray());

                // Remove from pending
                pendingStorage.getMessages().removeAll(queue);
                saveStorage(peer, pendingStorage);

                // Checking queue
                scheduleCheck();
            }
        }

        return Promise.success(null);
    }

    //
    // Queue Checking
    //

    private void scheduleCheck() {
        if (checkCancellable != null) {
            checkCancellable.cancel();
            checkCancellable = null;
        }
        if (isDestructing) {
            return;
        }
        if (destructQueueStorage.getQueue().size() == 0) {
            checkCancellable = schedule(new CheckQueue(), DEFAULT_DELAY);
        } else {
            long time = Runtime.getCurrentSyncedTime();
            long delta = destructQueueStorage.getQueue().get(0).getDestructDate() - time;
            if (delta < 0) {
                checkQueue();
            } else {
                checkCancellable = schedule(new CheckQueue(), delta);
            }
        }
    }

    private void checkQueue() {
        if (isDestructing) {
            return;
        }
        long time = Runtime.getCurrentSyncedTime();
        List<DestructQueueMessage> pendingMessages = null;
        for (DestructQueueMessage q : destructQueueStorage.getQueue()) {
            if (q.getDestructDate() <= time) {
                if (pendingMessages == null) {
                    pendingMessages = new ArrayList<>();
                }
                pendingMessages.add(q);
            }
        }
        if (pendingMessages != null) {
            isDestructing = true;
            HashMap<Peer, ArrayList<Long>> messages = new HashMap<>();
            for (DestructQueueMessage p : pendingMessages) {
                if (!messages.containsKey(p.getPeer())) {
                    messages.put(p.getPeer(), new ArrayList<>());
                }
                messages.get(p.getPeer()).add(p.getRid());
            }

            ArrayList<Promise<Void>> res = new ArrayList<>();
            for (Peer p : messages.keySet()) {
                res.add(context().getMessagesModule().getRouter().onMessagesDestructed(p,
                        messages.get(p)));
            }
            final List<DestructQueueMessage> finalPendingMessages = pendingMessages;
            PromisesArray.ofPromises(res)
                    .zip(r -> null)
                    .then(r -> {
                        destructQueueStorage.getQueue().removeAll(finalPendingMessages);
                        keyValueStorage.addOrUpdateItem(0, destructQueueStorage.toByteArray());
                        isDestructing = false;
                        scheduleCheck();
                    });
        } else {
            scheduleCheck();
        }
    }

    //
    // Tools
    //

    private DestructPendingStorage getStorage(Peer peer) {
        byte[] data = keyValueStorage.loadItem(peer.getUnuqueId());
        if (data == null) {
            return null;
        }
        try {
            return DestructPendingStorage.fromBytes(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveStorage(Peer peer, DestructPendingStorage storage) {
        if (storage.getMessages().size() == 0 && storage.getIndividualMessages().size() == 0) {
            keyValueStorage.removeItem(peer.getUnuqueId());
        } else {
            keyValueStorage.addOrUpdateItem(peer.getUnuqueId(), storage.toByteArray());
        }
    }

    //
    // Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof MessageRead) {
            MessageRead messageRead = (MessageRead) message;
            return onMessageRead(messageRead.peer, messageRead.readDate);
        } else if (message instanceof MessageReadByMe) {
            MessageReadByMe readByMe = (MessageReadByMe) message;
            return onMessageReadByMe(readByMe.peer, readByMe.readDate);
        } else if (message instanceof NewMessages) {
            NewMessages newMessages = (NewMessages) message;
            return onMessages(newMessages.peer, newMessages.messages);
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof CheckQueue) {
            checkQueue();
        } else {
            super.onReceive(message);
        }
    }

    public static class MessageRead implements AskMessage<Void> {
        private final Peer peer;
        private final long readDate;

        public MessageRead(Peer peer, long readDate) {
            this.peer = peer;
            this.readDate = readDate;
        }
    }

    public static class MessageReadByMe implements AskMessage<Void> {
        private final Peer peer;
        private final long readDate;

        public MessageReadByMe(Peer peer, long readDate) {
            this.peer = peer;
            this.readDate = readDate;
        }
    }

    public static class NewMessages implements AskMessage<Void> {
        private final Peer peer;
        private final List<MessageDesc> messages;

        public NewMessages(Peer peer, List<MessageDesc> messages) {
            this.peer = peer;
            this.messages = messages;
        }
    }

    private static class CheckQueue {

    }
}