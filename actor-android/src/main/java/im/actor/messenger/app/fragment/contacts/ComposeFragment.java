package im.actor.messenger.app.fragment.contacts;

import im.actor.messenger.app.Intents;
import im.actor.model.entity.Contact;

/**
 * Created by ex3ndr on 23.09.14.
 */
public class ComposeFragment extends BaseContactFragment {

    public ComposeFragment() {
        super(true, true);
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()));
        getActivity().finish();
    }
}
