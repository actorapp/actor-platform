package im.actor.messenger.app.fragment.compose;

import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.contacts.BaseContactFragment;
import im.actor.core.entity.Contact;

/**
 * Created by ex3ndr on 23.09.14.
 */
public class ComposeFragment extends BaseContactFragment {

    public ComposeFragment() {
        super(true, true, false);
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
        getActivity().finish();
    }
}
