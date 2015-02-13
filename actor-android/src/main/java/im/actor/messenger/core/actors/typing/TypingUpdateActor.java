package im.actor.messenger.core.actors.typing;

import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.MailboxCreator;
import com.droidkit.actors.Props;
import com.droidkit.actors.mailbox.Envelope;
import com.droidkit.actors.mailbox.Mailbox;
import com.droidkit.actors.mailbox.MailboxesQueue;
import com.droidkit.actors.typed.TypedActor;

import im.actor.api.scheme.Peer;
import im.actor.api.scheme.PeerType;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.TypingModel;

/**
 * Created by ex3ndr on 10.10.14.
 */
public class TypingUpdateActor extends TypedActor<TypingUpdateInt> implements TypingUpdateInt {

    private static final ActorSelection SELECTION = new ActorSelection(
            Props.create(TypingUpdateActor.class, new MailboxCreator() {
                @Override
                public Mailbox createMailbox(MailboxesQueue queue) {
                    return new TypingMailbox(queue);
                }
            }).changeDispatcher("updates"), "updates/typing");

    private static final TypedActorHolder<TypingUpdateInt> HOLDER =
            new TypedActorHolder<TypingUpdateInt>(TypingUpdateInt.class, SELECTION);

    public static TypingUpdateInt typingUpdates() {
        return HOLDER.get();
    }

    private static final int TYPING_TEXT_TIMEOUT = 3000;

    public TypingUpdateActor() {
        super(TypingUpdateInt.class);
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof CancelTyping) {
            CancelTyping cancelTyping = (CancelTyping) message;
            if (cancelTyping.chatType == DialogType.TYPE_USER) {
                stopTyping(cancelTyping.chatId);
            } else {
                stopTyping(cancelTyping.chatId, cancelTyping.uid);
            }
        }
    }

    @Override
    public void onTypingUpdate(Peer peer, int uid) {
        if (peer.getType() == PeerType.PRIVATE) {
            startTyping(uid);
            self().sendOnce(new CancelTyping(DialogType.TYPE_USER, peer.getId(), uid),
                    TYPING_TEXT_TIMEOUT);
        } else if (peer.getType() == PeerType.GROUP) {
            startTyping(peer.getId(), uid);
            self().sendOnce(new CancelTyping(DialogType.TYPE_GROUP, peer.getId(), uid),
                    TYPING_TEXT_TIMEOUT);
        }
    }

    @Override
    public void onInMessage(Peer peer, int senderId) {
        if (peer.getType() == PeerType.PRIVATE) {
            stopTyping(peer.getId());
        } else if (peer.getType() == PeerType.GROUP) {
            stopTyping(peer.getId(), senderId);
        }
    }

    private void stopTyping(int uid) {
        TypingModel.privateChatTyping(uid).change(false);
    }

    private void startTyping(int uid) {
        TypingModel.privateChatTyping(uid).change(true);
    }

    private void stopTyping(int groupId, int uid) {
        int[] val = TypingModel.groupChatTyping(groupId).getValue();
        boolean founded = false;
        for (int u : val) {
            if (u == uid) {
                founded = true;
                break;
            }
        }
        if (!founded) {
            return;
        }
        int[] val2 = new int[val.length - 1];
        int index = 0;
        for (int u : val) {
            if (u != uid) {
                val2[index++] = u;
            }
        }
        TypingModel.groupChatTyping(groupId).change(val2);
    }

    private void startTyping(int groupId, int uid) {
        int[] val = TypingModel.groupChatTyping(groupId).getValue();
        int[] val2 = new int[val.length + 1];

        for (int i = 0; i < val.length; i++) {
            val2[i] = val[i];
            if (val[i] == uid) {
                return;
            }
        }

        val2[val2.length - 1] = uid;

        TypingModel.groupChatTyping(groupId).change(val2);
    }

    private static class CancelTyping {
        private int chatType;
        private int chatId;
        private int uid;

        private CancelTyping(int chatType, int chatId, int uid) {
            this.chatType = chatType;
            this.chatId = chatId;
            this.uid = uid;
        }
    }

    private static class TypingMailbox extends Mailbox {

        public TypingMailbox(MailboxesQueue queue) {
            super(queue);
        }

        @Override
        protected boolean isEqualEnvelope(Envelope a, Envelope b) {
            if (a.getMessage() instanceof CancelTyping && b.getMessage() instanceof CancelTyping) {
                return ((CancelTyping) a.getMessage()).uid == ((CancelTyping) b.getMessage()).uid;
            }
            return super.isEqualEnvelope(a, b);
        }
    }
}
