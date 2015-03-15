package im.actor.messenger.app.fragment.contacts;

import android.app.Activity;
import android.content.Intent;

import im.actor.messenger.app.Intents;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class PickUserFragment extends BaseContactFragment {
    @Override
    protected void onUserSelected(int uid) {
        getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(Intents.EXTRA_UID, uid));
    }
}
