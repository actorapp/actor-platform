package im.actor.core.modules.encryption;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.api.ApiEncryptedDataMessage;
import im.actor.core.api.updates.UpdateEncryptedPackage;
import im.actor.core.api.updates.UpdatePublicKeyGroupAdded;
import im.actor.core.api.updates.UpdatePublicKeyGroupRemoved;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.KeyManagerActor;
import im.actor.core.modules.internal.messages.ConversationActor;
import im.actor.core.modules.sequence.Processor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.function.Consumer;

public class EncryptedProcessor extends AbsModule implements Processor {

    public EncryptedProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public boolean process(ActorRef ref, final Object update) {
        if (update instanceof UpdatePublicKeyGroupAdded) {
            context().getEncryption().getKeyManagerInt()
                    .onKeyGroupsAdded(((UpdatePublicKeyGroupAdded) update).getUid(),
                            ((UpdatePublicKeyGroupAdded) update).getKeyGroup());
            return true;
        } else if (update instanceof UpdatePublicKeyGroupRemoved) {
            context().getEncryption().getKeyManagerInt().onKeyGroupsRemoved(((UpdatePublicKeyGroupRemoved) update).getUid(),
                    ((UpdatePublicKeyGroupRemoved) update).getKeyGroupId());
            return true;
        } else if (update instanceof UpdateEncryptedPackage) {
            final UpdateEncryptedPackage encryptedPackage = (UpdateEncryptedPackage) update;
            context().getEncryption().getEncrypted().doDecrypt(encryptedPackage.getSenderId(),
                    encryptedPackage.getEncryptedBox()).then(new Consumer<EncryptedActor.PlainTextPackage>() {
                @Override
                public void apply(EncryptedActor.PlainTextPackage plainTextPackage) {
                    if (plainTextPackage.getData() instanceof ApiEncryptedDataMessage) {
                        ApiEncryptedDataMessage encryptedDataMessage = (ApiEncryptedDataMessage) plainTextPackage.getData();
                        try {
                            ArrayList<Message> messages = new ArrayList<>();
                            messages.add(new Message(
                                            encryptedPackage.getRandomId(),
                                            encryptedPackage.getDate(),
                                            encryptedPackage.getDate(),
                                            encryptedPackage.getSenderId(),
                                            MessageState.UNKNOWN,
                                            AbsContent.fromMessage(encryptedDataMessage.getMessage()),
                                            new ArrayList<Reaction>())
                            );
                            conversationActor(Peer.userEncrypted(((UpdateEncryptedPackage) update).getSenderId())).send(new ConversationActor.Messages(messages));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).failure(new Consumer<Exception>() {
                @Override
                public void apply(Exception e) {

                }
            }).done(ref);
            return true;
        }
        return false;
    }
}
