package im.actor.core.modules.encryption;

import org.jetbrains.annotations.NotNull;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiEncryptedChatTimerSet;
import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptionKeyGroup;
import im.actor.core.api.ApiServiceMessage;
import im.actor.core.api.ApiServiceTimerChanged;
import im.actor.core.entity.EncryptedConversationState;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.ratchet.EncryptedMsg;
import im.actor.core.modules.encryption.ratchet.KeyManager;
import im.actor.core.modules.encryption.updates.EncryptedUpdates;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.KeyValueEngine;

public class EncryptedRouterActor extends ModuleActor {

    private KeyManager keyManager;
    private EncryptedMsg encryptedMsg;
    private EncryptedUpdates updates;
    private KeyValueEngine<EncryptedConversationState> stateKeyValue;

    public EncryptedRouterActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        updates = new EncryptedUpdates(context());
        keyManager = context().getEncryption().getKeyManager();
        encryptedMsg = context().getEncryption().getEncryption();
        stateKeyValue = context().getEncryption().getConversationState().getEngine();
    }

    public Promise<Void> onKeyGroupAdded(int uid, ApiEncryptionKeyGroup group) {
        return keyManager.onKeyGroupAdded(uid, group);
    }

    public Promise<Void> onKeyGroupRemoved(int uid, int keyGroupId) {
        return keyManager.onKeyGroupRemoved(uid, keyGroupId);
    }

    public Promise<Void> onTimerSet(long randomId, long date, int uid, int timerInMs) {
        EncryptedConversationState state = stateKeyValue.getValue(uid);
        if (state.getTimer() != timerInMs && state.getTimerDate() < date) {
            stateKeyValue.addOrUpdateItem(state.editTimer(timerInMs, date));
        }

        return context().getMessagesModule().getRouter()
                .onNewMessage(Peer.secret(uid), new Message(randomId, date, date,
                        myUid(), MessageState.SENT, AbsContent.fromMessage(new ApiServiceMessage("Timer set",
                        new ApiServiceTimerChanged(timerInMs)))));
    }

    // Messages

    public Promise<Void> onEncryptedUpdate(int uid, long date, ApiEncryptedContent update) {
        Promise<Void> res = Promise.success(null);
        if (update instanceof ApiEncryptedChatTimerSet) {
            ApiEncryptedChatTimerSet timerSet = (ApiEncryptedChatTimerSet) update;
            int peerId = uid;
            if (timerSet.getReceiverId() != myUid()) {
                peerId = timerSet.getReceiverId();
            }
            int timer = 0;
            if (timerSet.getTimerMs() != null) {
                timer = timerSet.getTimerMs();
            }
            res = onTimerSet(timerSet.getRid(), date, peerId, timer);
        }
        return res.chain(r -> updates.onUpdate(uid, date, update));
    }

    public Promise<Void> onEncryptedBox(long date, int senderId, @NotNull ApiEncryptedBox encryptedBox) {
        return encryptedMsg.decrypt(senderId, encryptedBox)
                .flatMap(message -> onEncryptedUpdate(senderId, date, message))
                .fallback(e -> Promise.success(null));
    }

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof KeyGroupAdded) {
            KeyGroupAdded groupAdded = (KeyGroupAdded) message;
            return onKeyGroupAdded(groupAdded.getUid(), groupAdded.getGroup());
        } else if (message instanceof KeyGroupRemoved) {
            KeyGroupRemoved removed = (KeyGroupRemoved) message;
            return onKeyGroupRemoved(removed.getUid(), removed.getKeyGroupId());
        } else if (message instanceof EncryptedUpdate) {
            EncryptedUpdate update = (EncryptedUpdate) message;
            return onEncryptedUpdate(update.getUid(), update.getDate(), update.getUpdate());
        } else if (message instanceof EncryptedPackageUpdate) {
            EncryptedPackageUpdate update = (EncryptedPackageUpdate) message;
            return onEncryptedBox(update.getDate(), update.getSenderId(), update.getEncryptedBox());
        } else {
            return super.onAsk(message);
        }
    }

    public static class KeyGroupAdded implements AskMessage<Void> {

        private int uid;
        private ApiEncryptionKeyGroup group;

        public KeyGroupAdded(int uid, ApiEncryptionKeyGroup group) {
            this.uid = uid;
            this.group = group;
        }

        public int getUid() {
            return uid;
        }

        public ApiEncryptionKeyGroup getGroup() {
            return group;
        }
    }

    public static class KeyGroupRemoved implements AskMessage<Void> {

        private int uid;
        private int keyGroupId;

        public KeyGroupRemoved(int uid, int keyGroupId) {
            this.uid = uid;
            this.keyGroupId = keyGroupId;
        }

        public int getUid() {
            return uid;
        }

        public int getKeyGroupId() {
            return keyGroupId;
        }
    }

    public static class EncryptedUpdate implements AskMessage<Void> {

        private int uid;
        private long date;
        private ApiEncryptedContent update;

        public EncryptedUpdate(int uid, long date, ApiEncryptedContent update) {
            this.uid = uid;
            this.date = date;
            this.update = update;
        }

        public int getUid() {
            return uid;
        }

        public long getDate() {
            return date;
        }

        public ApiEncryptedContent getUpdate() {
            return update;
        }
    }

    public static class EncryptedPackageUpdate implements AskMessage<Void> {

        private long date;
        private int senderId;
        private ApiEncryptedBox encryptedBox;

        public EncryptedPackageUpdate(long date, int senderId, @NotNull ApiEncryptedBox encryptedBox) {
            this.date = date;
            this.senderId = senderId;
            this.encryptedBox = encryptedBox;
        }

        public long getDate() {
            return date;
        }

        public int getSenderId() {
            return senderId;
        }

        public ApiEncryptedBox getEncryptedBox() {
            return encryptedBox;
        }
    }
}
