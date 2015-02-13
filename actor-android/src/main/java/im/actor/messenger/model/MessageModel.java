package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import im.actor.messenger.core.Core;
import im.actor.messenger.storage.scheme.messages.ConversationMessage;
import im.actor.messenger.storage.scheme.messages.types.AbsMessage;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;

/**
 * Created by ex3ndr on 15.09.14.
 */
public class MessageModel {
    private ConversationMessage conversationMessage;
    private ValueModel<AudioState> audioState;

    private long rid;
    private long sortingKey;

    public MessageModel(ConversationMessage conversationMessage) {
        this.conversationMessage = conversationMessage;
        this.rid = conversationMessage.getRid();
        this.sortingKey = conversationMessage.getSortKey();

        if (conversationMessage.getContent() instanceof AudioMessage
                && ((AudioMessage) conversationMessage.getContent()).isDownloaded()) {
            audioState = new ValueModel<AudioState>("audio." + rid + ".state", new AudioState(AudioState.State.STOPPED));
        }
    }

    public AbsMessage getContent() {
        return getRaw().getContent();
    }

    public long getRid() {
        return rid;
    }

    public long getSortingKey() {
        return sortingKey;
    }

    public synchronized void update(ConversationMessage message) {
        this.conversationMessage = message;
        this.sortingKey = message.getSortKey();
        if (audioState == null) {
            if (conversationMessage.getContent() instanceof AudioMessage
                    && ((AudioMessage) conversationMessage.getContent()).isDownloaded()) {
                audioState = new ValueModel<AudioState>("audio." + rid + ".state", new AudioState(AudioState.State.STOPPED));
            }
        }
    }

    public ValueModel<AudioState> getAudioState() {
        return audioState;
    }


    public ConversationMessage getRaw() {
        return conversationMessage;
    }

    public boolean isOut() {
        return getRaw().getSenderId() == Core.myUid();
    }
}
