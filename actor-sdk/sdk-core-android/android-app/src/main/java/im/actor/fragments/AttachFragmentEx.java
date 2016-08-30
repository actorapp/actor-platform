package im.actor.fragments;

import android.widget.Toast;

import java.util.List;

import im.actor.core.entity.Peer;
import im.actor.develop.R;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.conversation.attach.AttachFragment;
import im.actor.sdk.controllers.conversation.attach.ShareMenuField;

public class AttachFragmentEx extends AttachFragment {

    public AttachFragmentEx(Peer peer) {
        super(peer);
    }

    public AttachFragmentEx() {
    }

    @Override
    protected List<ShareMenuField> onCreateFields() {
        List<ShareMenuField> res = super.onCreateFields();
        res.add(new ShareMenuField(R.id.share_test, R.drawable.ic_edit_white_24dp, ActorSDK.sharedActor().style.getAccentColor(), "lol"));
        return res;
    }

    @Override
    protected void onItemClicked(int id) {
        super.onItemClicked(id);
        if (id == R.id.share_test) {
            Toast.makeText(getContext(), "Hey", Toast.LENGTH_LONG).show();
        }
    }
}
