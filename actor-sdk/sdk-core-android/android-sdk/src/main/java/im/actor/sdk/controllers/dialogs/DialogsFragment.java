package im.actor.sdk.controllers.dialogs;

import android.support.v4.app.Fragment;

import im.actor.core.entity.Dialog;

public class DialogsFragment extends BaseDialogFragment {

    protected void onItemClick(Dialog item) {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof DialogsFragmentDelegate) {
            ((DialogsFragmentDelegate) parent).onPeerClicked(item.getPeer());
        }
    }

    protected boolean onItemLongClick(Dialog dialog) {
        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof DialogsFragmentDelegate) {
            return ((DialogsFragmentDelegate) parent).onPeerLongClicked(dialog.getPeer());
        }
        return false;
    }
}