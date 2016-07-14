package im.actor.core.modules.messaging;

import im.actor.core.api.ApiEncryptedContent;
import im.actor.core.api.ApiEncryptedMessageContent;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.EncryptedSequenceProcessor;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class MessagesProcessorEncrypted extends AbsModule implements EncryptedSequenceProcessor {

    public MessagesProcessorEncrypted(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> onUpdate(int senderId, long date, ApiEncryptedContent update) {
        if (update instanceof ApiEncryptedMessageContent) {
            ApiEncryptedMessageContent content = (ApiEncryptedMessageContent) update;

            Message msg = new Message(content.getRid(), date, date, senderId,
                    MessageState.UNKNOWN, AbsContent.fromMessage(content.getMessage()));

            int destId = senderId;
            if (senderId == myUid()) {
                destId = content.getReceiverId();
            }

            return context().getMessagesModule().getRouter()
                    .onNewMessage(Peer.secret(destId), msg);
        } else {
            return null;
        }
    }
}