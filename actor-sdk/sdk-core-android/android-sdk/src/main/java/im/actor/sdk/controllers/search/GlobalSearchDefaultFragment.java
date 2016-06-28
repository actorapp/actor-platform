package im.actor.sdk.controllers.search;

import android.app.Activity;

import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.Intents;

public class GlobalSearchDefaultFragment extends GlobalSearchBaseFragment {

    @Override
    protected void onPeerPicked(Peer peer) {
        Activity activity = getActivity();
        if (activity != null) {
            startActivity(Intents.openDialog(peer, false, activity));
        }
    }
}
