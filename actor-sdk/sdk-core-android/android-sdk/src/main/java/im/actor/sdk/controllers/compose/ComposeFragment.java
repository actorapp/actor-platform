package im.actor.sdk.controllers.compose;

import android.content.Intent;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.contacts.BaseContactFragment;
import im.actor.core.entity.Contact;

public class ComposeFragment extends BaseContactFragment {

    public ComposeFragment() {
        super(true, true, false);
    }

    @Override
    protected void addFootersAndHeaders() {
        super.addFootersAndHeaders();

        addFooterOrHeaderAction(ActorSDK.sharedActor().style.getActionShareColor(),
                R.drawable.ic_group_white_24dp, R.string.main_fab_new_group, false, () -> {
                    startActivity(new Intent(getActivity(), CreateGroupActivity.class));
                    getActivity().finish();
                }, true);
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
        getActivity().finish();
    }
}
