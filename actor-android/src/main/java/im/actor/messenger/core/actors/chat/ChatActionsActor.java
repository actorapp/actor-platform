package im.actor.messenger.core.actors.chat;

import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.core.actors.messages.ClearChatActor;
import im.actor.messenger.core.actors.messages.DeleteMessagesActor;
import im.actor.messenger.model.DialogType;

/**
 * Created by ex3ndr on 25.11.14.
 */
public class ChatActionsActor extends TypedActor<ChatActionsInt> implements ChatActionsInt {

    private static final TypedActorHolder<ChatActionsInt> HOLDER = new TypedActorHolder<ChatActionsInt>(ChatActionsInt.class,
            ChatActionsActor.class, "chat_actions");

    public static ChatActionsInt actions() {
        return HOLDER.get();
    }

    public ChatActionsActor() {
        super(ChatActionsInt.class);
    }

    @Override
    public void deleteMessages(int chatType, int chatId, long[] messages) {
        for (long l : messages) {
            system().actorOf(DeleteMessagesActor.messageReader())
                    .send(new DeleteMessagesActor.Delete(chatType, chatId, l));
        }
        ConversationActor.conv(chatType, chatId).deleteMessage(messages);
    }

    @Override
    public void clearChat(int chatType, int chatId) {
        ConversationActor.conv(chatType, chatId).clearChat();
        system().actorOf(ClearChatActor.clearChat()).send(new ClearChatActor.ClearChat(chatType, chatId));
    }

    @Override
    public void deleteChat(int chatType, int chatId) {
        ConversationActor.conv(chatType, chatId).deleteChat();
        system().actorOf(ClearChatActor.clearChat()).send(new ClearChatActor.DeleteChat(chatType, chatId));
    }
}
