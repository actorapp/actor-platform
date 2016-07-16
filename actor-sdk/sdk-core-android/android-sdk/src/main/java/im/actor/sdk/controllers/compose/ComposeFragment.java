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
        setRootFragment(true);
        setTitle(R.string.compose_title);
        setHomeAsUp(true);
    }

    @Override
    protected void addFootersAndHeaders() {
        super.addFootersAndHeaders();

        addFooterOrHeaderAction(ActorSDK.sharedActor().style.getActionShareColor(),
                R.drawable.ic_group_white_24dp, R.string.main_fab_new_group, false, () -> {
                    startActivity(new Intent(getActivity(), CreateGroupActivity.class)
                            .putExtra(CreateGroupActivity.EXTRA_IS_CHANNEL, false));
                    getActivity().finish();
                }, true);

        addFooterOrHeaderAction(ActorSDK.sharedActor().style.getActionShareColor(),
                R.drawable.ic_megaphone_18dp_black, R.string.main_fab_new_channel, false, () -> {
                    startActivity(new Intent(getActivity(), CreateGroupActivity.class)
                            .putExtra(CreateGroupActivity.EXTRA_IS_CHANNEL, true));
                    getActivity().finish();
                }, true);
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
        getActivity().finish();
    }
}
