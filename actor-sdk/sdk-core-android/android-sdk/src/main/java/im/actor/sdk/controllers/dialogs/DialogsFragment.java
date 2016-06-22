package im.actor.sdk.controllers.dialogs;

import android.support.v4.app.Fragment;

import im.actor.core.entity.Dialog;
import im.actor.sdk.controllers.root.PeerSelectedCallback;

public class DialogsFragment extends BaseDialogFragment {

    protected void onItemClick(Dialog item) {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof PeerSelectedCallback) {
            ((PeerSelectedCallback) parent).onPeerClick(item.getPeer());
        }
    }

    protected boolean onItemLongClick(Dialog dialog) {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof PeerSelectedCallback) {
            return ((PeerSelectedCallback) parent).onPeerLongClick(dialog.getPeer());
        }
        return false;
    }
}