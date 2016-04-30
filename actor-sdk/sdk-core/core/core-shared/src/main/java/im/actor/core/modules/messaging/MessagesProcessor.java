/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiMessageReaction;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateMessageDelete;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateMessageSent;
import im.actor.core.api.updates.UpdateReactionsUpdate;
import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.actions.SenderActor;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.promise.Promise;

import static im.actor.core.entity.EntityConverter.convert;

public class MessagesProcessor extends AbsModule implements SequenceProcessor {

    public MessagesProcessor(ModuleContext context) {
        super(context);
    }


    //
    // Differences
    //

    @Verified
    public Promise<Void> onDifferenceStart() {
        return context().getMessagesModule().getRouter().onDifferenceStart();
    }

    @Verified
    public Promise<Void> onDifferenceMessages(ApiPeer _peer, List<UpdateMessage> messages) {

        Peer peer = convert(_peer);

        ArrayList<Message> nMessages = new ArrayList<>();
        for (UpdateMessage u : messages) {

            AbsContent msgContent = AbsContent.fromMessage(u.getMessage());

            nMessages.add(new Message(
                    u.getRid(),
                    u.getDate(),
                    u.getDate(),
                    u.getSenderUid(),
                    myUid() == u.getSenderUid() ? MessageState.SENT : MessageState.UNKNOWN,
                    msgContent));
        }


        return context().getMessagesModule().getRouter().onNewMessages(peer, nMessages);
    }

    @Verified
    public Promise<Void> onDifferenceEnd() {
        return context().getMessagesModule().getRouter().onDifferenceEnd();
    }


    //
    // Update Handling
    //

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateMessage ||
                update instanceof UpdateMessageRead ||
                update instanceof UpdateMessageReadByMe ||
                update instanceof UpdateMessageReceived ||
                update instanceof UpdateMessageDelete ||
                update instanceof UpdateMessageContentChanged ||
                update instanceof UpdateChatClear ||
                update instanceof UpdateChatDelete ||
                update instanceof UpdateChatGroupsChanged ||
                update instanceof UpdateReactionsUpdate ||
                update instanceof UpdateMessageSent) {

            return context().getMessagesModule().getRouter().onUpdate(update);
        }
        return null;
    }
}
