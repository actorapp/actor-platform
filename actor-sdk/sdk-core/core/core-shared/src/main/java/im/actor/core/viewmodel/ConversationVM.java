package im.actor.core.viewmodel;

import im.actor.core.entity.Peer;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.generics.BooleanValueModel;

public class ConversationVM {

    private BooleanValueModel isLoaded;

    public ConversationVM(Peer peer, ModuleContext context) {
        isLoaded = new BooleanValueModel("chat.state." + peer, context.getMessagesModule().isLoaded(peer));
    }

    public BooleanValueModel getIsLoaded() {
        return isLoaded;
    }
}