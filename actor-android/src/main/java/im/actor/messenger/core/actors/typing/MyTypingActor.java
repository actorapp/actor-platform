package im.actor.messenger.core.actors.typing;

import android.os.SystemClock;

import com.droidkit.actors.typed.TypedActor;

import im.actor.api.scheme.OutPeer;
import im.actor.api.scheme.PeerType;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 10.10.14.
 */
public class MyTypingActor extends TypedActor<MyTypingInt> implements MyTypingInt {

    private static final TypedActorHolder<MyTypingInt> HOLDER = new TypedActorHolder<MyTypingInt>(MyTypingInt.class,
            MyTypingActor.class, "typing");


    public static MyTypingInt myTyping() {
        return HOLDER.get();
    }

    private static final long TYPING_DELAY = 1000L;

    private long lastRequest = 0;

    public MyTypingActor() {
        super(MyTypingInt.class);
    }

    @Override
    public void onType(int chatType, int chatId) {
        if (SystemClock.uptimeMillis() - lastRequest < TYPING_DELAY) {
            return;
        }
        lastRequest = SystemClock.uptimeMillis();

        if (chatType == DialogType.TYPE_USER) {
            UserModel userModel = users().get(chatId);
            if (userModel == null) {
                return;
            }
            requests().typing(new OutPeer(PeerType.PRIVATE, chatId, userModel.getAccessHash()),
                    0, 5000);
        } else if (chatType == DialogType.TYPE_GROUP) {
            GroupModel groupModel = groups().get(chatId);
            if (groupModel == null) {
                return;
            }
            requests().typing(new OutPeer(PeerType.GROUP, chatId, groupModel.getAccessHash()),
                    0, 5000);
        }
    }
}
