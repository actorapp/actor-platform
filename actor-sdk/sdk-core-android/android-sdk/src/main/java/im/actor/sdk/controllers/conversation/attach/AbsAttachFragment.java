package im.actor.sdk.controllers.conversation.attach;

import android.os.Bundle;

import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.BaseFragment;

public abstract class AbsAttachFragment extends BaseFragment {

    private Peer peer;

    public AbsAttachFragment(Peer peer) {
        Bundle bundle = new Bundle();
        bundle.putLong("peer", peer.getUnuqueId());
        setArguments(bundle);
    }

    public AbsAttachFragment() {

    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        peer = Peer.fromUniqueId(getArguments().getLong("peer"));
    }

    public abstract void show();

    public abstract void hide();

    public abstract boolean onBackPressed();
}
