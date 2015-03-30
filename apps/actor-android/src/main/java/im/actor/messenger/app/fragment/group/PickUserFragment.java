package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.content.Intent;

import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.contacts.BaseContactFragment;
import im.actor.model.entity.Contact;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class PickUserFragment extends BaseContactFragment {
    public PickUserFragment() {
        super(true, true, false);
    }

    @Override
    public void onItemClicked(Contact contact) {
        getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(Intents.EXTRA_UID, contact.getUid()));
        getActivity().finish();
    }
}