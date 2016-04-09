package im.actor.core.modules.messaging.router;

import java.util.HashSet;
import java.util.List;

import im.actor.core.entity.Message;
import im.actor.core.entity.MessageState;
import im.actor.core.entity.Peer;
import im.actor.core.entity.Reaction;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.dialogs.DialogsActor;
import im.actor.core.modules.messaging.router.entity.RouterAppHidden;
import im.actor.core.modules.messaging.router.entity.RouterAppVisible;
import im.actor.core.modules.messaging.router.entity.RouterChangedContent;
import im.actor.core.modules.messaging.router.entity.RouterChangedReactions;
import im.actor.core.modules.messaging.router.entity.RouterConversationHidden;
import im.actor.core.modules.messaging.router.entity.RouterConversationVisible;
import im.actor.core.modules.messaging.router.entity.RouterDeletedMessages;
import im.actor.core.modules.messaging.router.entity.RouterNewMessages;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingError;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingMessage;
import im.actor.core.modules.messaging.router.entity.RouterOutgoingSent;
import im.actor.core.util.JavaUtil;
import im.actor.runtime.storage.ListEngine;

public class RouterActor extends ModuleActor {

    private static final String TAG = "RouterActor";

    private final HashSet<Peer> visiblePeers = new HashSet<>();
    private boolean isAppVisible = false;

    public RouterActor(ModuleContext context) {
        super(context);
    }


    //
    // Incoming Messages
    //

    private void onNewMessages(Peer peer, List<Message> messages) {
        Message topMessage = null;
        for (Message m : messages) {
            if (topMessage == null || topMessage.getSortDate() < m.getSortDate()) {
                topMessage = m;
            }
        }

        conversation(peer).addOrUpdateItems(messages);

        if (topMessage != null) {
            dialogsActor(new DialogsActor.InMessage(peer, topMessage, -1));
        }
    }


    //
    // Outgoing Messages
    //

    private void onOutgoingMessage(Peer peer, Message message) {
        conversation(peer).addOrUpdateItem(message);
    }

    private void onOutgoingSent(Peer peer, long rid, long date) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeAllDate(date)
                    .changeState(MessageState.SENT);
            conversation(peer).addOrUpdateItem(updatedMsg);

            // Notify dialogs
            dialogsActor(new DialogsActor.InMessage(peer, updatedMsg, -1));
        }
    }

    private void onOutgoingError(Peer peer, long rid) {
        Message msg = conversation(peer).getValue(rid);
        // If we have pending message
        if (msg != null && (msg.getMessageState() == MessageState.PENDING)) {

            // Updating message
            Message updatedMsg = msg
                    .changeState(MessageState.ERROR);
            conversation(peer).addOrUpdateItem(updatedMsg);
        }
    }


    //
    // Message Updating
    //

    private void onContentUpdate(Peer peer, long rid, AbsContent content) {
        Message message = conversation(peer).getValue(rid);

        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        conversation(peer).addOrUpdateItem(message.changeContent(content));
    }

    private void onReactionsUpdate(Peer peer, long rid, List<Reaction> reactions) {
        Message message = conversation(peer).getValue(rid);

        // Ignore if we already doesn't have this message
        if (message == null) {
            return;
        }

        conversation(peer).addOrUpdateItem(message.changeReactions(reactions));
    }


    //
    // Message Deletions
    //

    private void onMessageDeleted(Peer peer, List<Long> rids) {

        // Delete Messages
        conversation(peer).removeItems(JavaUtil.unbox(rids));

        // TODO: Update dialogs list
    }


    //
    // Reading State
    //

    private void onConversationVisible(Peer peer) {
        visiblePeers.add(peer);
    }

    private void onConversationHidden(Peer peer) {
        visiblePeers.remove(peer);
    }

    private void onAppVisible() {
        isAppVisible = true;
    }

    private void onAppHidden() {
        isAppVisible = false;
    }


    //
    // Tools
    //

    private void dialogsActor(Object message) {
        context().getMessagesModule().getDialogsActor().send(message);
    }

    private ListEngine<Message> conversation(Peer peer) {
        return context().getMessagesModule().getConversationEngine(peer);
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof RouterConversationVisible) {
            RouterConversationVisible conversationVisible = (RouterConversationVisible) message;
            onConversationVisible(conversationVisible.getPeer());
        } else if (message instanceof RouterConversationHidden) {
            RouterConversationHidden conversationHidden = (RouterConversationHidden) message;
            onConversationHidden(conversationHidden.getPeer());
        } else if (message instanceof RouterAppVisible) {
            onAppVisible();
        } else if (message instanceof RouterAppHidden) {
            onAppHidden();
        } else if (message instanceof RouterNewMessages) {
            RouterNewMessages routerNewMessages = (RouterNewMessages) message;
            onNewMessages(routerNewMessages.getPeer(), routerNewMessages.getMessages());
        } else if (message instanceof RouterOutgoingMessage) {
            RouterOutgoingMessage routerOutgoingMessage = (RouterOutgoingMessage) message;
            onOutgoingMessage(routerOutgoingMessage.getPeer(), routerOutgoingMessage.getMessage());
        } else if (message instanceof RouterOutgoingSent) {
            RouterOutgoingSent routerOutgoingSent = (RouterOutgoingSent) message;
            onOutgoingSent(routerOutgoingSent.getPeer(), routerOutgoingSent.getRid(), routerOutgoingSent.getDate());
        } else if (message instanceof RouterOutgoingError) {
            RouterOutgoingError outgoingError = (RouterOutgoingError) message;
            onOutgoingError(outgoingError.getPeer(), outgoingError.getRid());
        } else if (message instanceof RouterChangedContent) {
            RouterChangedContent routerChangedContent = (RouterChangedContent) message;
            onContentUpdate(routerChangedContent.getPeer(), routerChangedContent.getRid(), routerChangedContent.getContent());
        } else if (message instanceof RouterChangedReactions) {
            RouterChangedReactions routerChangedReactions = (RouterChangedReactions) message;
            onReactionsUpdate(routerChangedReactions.getPeer(), routerChangedReactions.getRid(), routerChangedReactions.getReactions());
        } else if (message instanceof RouterDeletedMessages) {
            RouterDeletedMessages routerDeletedMessages = (RouterDeletedMessages) message;
            onMessageDeleted(routerDeletedMessages.getPeer(), routerDeletedMessages.getRids());
        } else {
            super.onReceive(message);
        }
    }
}
