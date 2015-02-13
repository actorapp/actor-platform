package im.actor.messenger.app.fragment.compose;

import im.actor.messenger.app.fragment.base.BaseContactFragment;
import im.actor.messenger.app.intents.Intents;

/**
 * Created by ex3ndr on 23.09.14.
 */
public class ComposeFragment extends BaseContactFragment {
    @Override
    protected void onUserSelected(int uid) {
        getActivity().startActivity(Intents.openPrivateDialog(uid, true, getActivity()));
    }
}
