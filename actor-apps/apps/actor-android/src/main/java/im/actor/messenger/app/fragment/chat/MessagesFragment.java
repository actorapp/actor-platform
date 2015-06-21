package im.actor.messenger.app.fragment.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 01.09.14.
 */
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
