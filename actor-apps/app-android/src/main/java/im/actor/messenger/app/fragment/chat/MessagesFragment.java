package im.actor.messenger.app.fragment.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

import im.actor.core.entity.Peer;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MessagesFragment extends BaseMessagesFragment {

    public static MessagesFragment create(Peer peer) {
        return new MessagesFragment(peer);
    }

    public MessagesFragment() {

    }

    @SuppressLint("ValidFragment")
    public MessagesFragment(Peer peer) {
        super(peer);
    }
}
