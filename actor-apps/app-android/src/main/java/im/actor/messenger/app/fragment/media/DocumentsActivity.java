package im.actor.messenger.app.fragment.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;

import im.actor.core.entity.Peer;
import im.actor.messenger.app.activity.BaseFragmentActivity;

public class DocumentsActivity extends BaseFragmentActivity {

    private Peer peer;
    public static final String EXTRA_CHAT_PEER = "chat_peer";

    public static Intent build(Peer peer, Context context) {

        final Intent intent = new Intent(context, DocumentsActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));
        getSupportActionBar().setTitle("Documents");

        if (savedInstanceState == null) {
            showFragment(new DocumentsFragment(peer), false, false);
        }
    }

    @Override
    public ActionMode startSupportActionMode(final ActionMode.Callback callback) {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startSupportActionMode(callback);
        if (mode != null) {
            mode.invalidate();
        }
        return mode;
    }
}

