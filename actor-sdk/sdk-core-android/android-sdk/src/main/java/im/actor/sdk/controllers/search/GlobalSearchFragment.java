package im.actor.sdk.controllers.search;

import android.support.v4.app.Fragment;

import im.actor.core.entity.Peer;

public class GlobalSearchFragment extends GlobalSearchBaseFragment {
    
    @Override
    protected void onPeerPicked(Peer peer) {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof GlobalSearchDelegate) {
            ((GlobalSearchDelegate) parent).onPeerClicked(peer);
        }
    }
}
