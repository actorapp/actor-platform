package im.actor.messenger.app.fragment.media;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.media.DocumentsFragment;
import im.actor.messenger.app.Intents;
import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DocumentsActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.documents_title);

        Peer peer = Peer.fromUniqueId(getIntent().getLongExtra(Intents.EXTRA_CHAT_PEER, 0));
        showFragment(DocumentsFragment.open(peer), false, false);
    }
}
