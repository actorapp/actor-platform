package im.actor.messenger.core.actors.send;

import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;

/**
 * Created by ex3ndr on 26.11.14.
 */
public class MessageDeliveryActor extends TypedActor<MessageDeliveryInt> implements MessageDeliveryInt {

    private static final TypedActorHolder<MessageDeliveryInt> HOLDER = new TypedActorHolder<MessageDeliveryInt>(
            MessageDeliveryInt.class, MessageDeliveryActor.class, "message_delivery");

    public static MessageDeliveryInt messageSender() {
        return HOLDER.get();
    }

    public MessageDeliveryActor() {
        super(MessageDeliveryInt.class);
    }

    @Override
    public void sendText(int chatType, int chatId, String text) {
        if (chatType == DialogType.TYPE_USER) {
            MessageSendActor.messageSender().sendText(chatType, chatId, text, true);
        } else if (chatType == DialogType.TYPE_GROUP) {
            MessageSendActor.messageSender().sendText(chatType, chatId, text, false);
        }
    }

    @Override
    public void sendOpus(int chatType, int chatId, String fileName, int duration) {
        if (chatType == DialogType.TYPE_USER) {
            MediaSenderActor.mediaSender().sendOpus(chatType, chatId, fileName, duration, true);
        } else if (chatType == DialogType.TYPE_GROUP) {
            MediaSenderActor.mediaSender().sendOpus(chatType, chatId, fileName, duration, false);
        }
    }

    @Override
    public void sendDocument(int chatType, int chatId, String fileName, String name) {
        if (chatType == DialogType.TYPE_USER) {
            MediaSenderActor.mediaSender().sendDocument(chatType, chatId, fileName, name, true);
        } else if (chatType == DialogType.TYPE_GROUP) {
            MediaSenderActor.mediaSender().sendDocument(chatType, chatId, fileName, name, false);
        }
    }

    @Override
    public void sendPhoto(int chatType, int chatId, String fileName) {
        if (chatType == DialogType.TYPE_USER) {
            MediaSenderActor.mediaSender().sendPhoto(chatType, chatId, fileName, true);
        } else if (chatType == DialogType.TYPE_GROUP) {
            MediaSenderActor.mediaSender().sendPhoto(chatType, chatId, fileName, false);
        }
    }

    @Override
    public void sendVideo(int chatType, int chatId, String fileName) {
        if (chatType == DialogType.TYPE_USER) {
            MediaSenderActor.mediaSender().sendVideo(chatType, chatId, fileName, true);
        } else if (chatType == DialogType.TYPE_GROUP) {
            MediaSenderActor.mediaSender().sendVideo(chatType, chatId, fileName, false);
        }
    }

    @Override
    public void mediaTryAgain(int chatType, int chatId, long rid) {
        MediaSenderActor.mediaSender().tryAgain(chatType, chatId, rid);
    }

    @Override
    public void mediaPause(int chatType, int chatId, long rid) {
        MediaSenderActor.mediaSender().pause(chatType, chatId, rid);
    }

    @Override
    public void mediaCancel(int chatType, int chatId, long rid) {
        MediaSenderActor.mediaSender().cancel(chatType, chatId, rid);
    }

    @Override
    public void mediaCancelAll(int chatType, int chatId) {
        MediaSenderActor.mediaSender().cancelAll(chatType, chatId);
    }
}
