package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import im.actor.core.entity.Peer;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_CHAT_PEER = "chat_peer";
    private String quote;
    private ChatFragment chatFragment;

    public static Intent build(Peer peer, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        // For faster keyboard open/close
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //
        // Loading Layout
        //
        setContentView(R.layout.activity_dialog);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //
        // Loading Fragments if needed
        //
        if (saveInstance == null) {
            Peer peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));
            chatFragment = ChatFragment.create(peer);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.chatFragment, chatFragment)
                    .commitNow();
            quote = getIntent().getStringExtra("forward_text_raw");
        }
    }


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chatFragment);
        if (fragment instanceof ChatFragment) {
            if (!((ChatFragment) fragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (quote != null) {
            chatFragment.onMessageQuote(quote);
            quote = null;
        }
    }

    @Override
    public ActionMode startSupportActionMode(@NonNull final ActionMode.Callback callback) {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startSupportActionMode(callback);
        if (mode != null) {
            mode.invalidate();
        }
        return mode;
    }
}
