package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import im.actor.core.entity.Peer;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.ActorToolbar;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_CHAT_PEER = "chat_peer";
    private String quote;
    private ChatFragment chatFragment;

    private Toolbar toolbar;

    public static Intent build(Peer peer, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        //
        // For faster keyboard open/close
        //

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //
        // Loading Layout
        //

        RelativeLayout rootLayout = new RelativeLayout(this);
        View antiFocus = new View(this);
        antiFocus.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
        antiFocus.setFocusable(true);
        antiFocus.setFocusableInTouchMode(true);
        rootLayout.addView(antiFocus);

        FrameLayout chatFragmentCont = new FrameLayout(this);
        chatFragmentCont.setId(R.id.chatFragment);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.toolbar);
        chatFragmentCont.setLayoutParams(params);
        rootLayout.addView(chatFragmentCont);

        ActorToolbar toolbar = new ActorToolbar(this);
        // Toolbar toolbar = new Toolbar(this);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        toolbar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarSize));
        toolbar.setMinimumHeight(actionBarSize);
        toolbar.setId(R.id.toolbar);
        toolbar.setBackgroundColor(ActorSDK.sharedActor().style.getToolBarColor());
        toolbar.setItemColor(Color.WHITE);
        rootLayout.addView(toolbar);
        this.toolbar = toolbar;

        FrameLayout overlay = new FrameLayout(this);
        overlay.setId(R.id.overlay);
        overlay.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(overlay);

        setContentView(rootLayout);
        setSupportActionBar(toolbar);

        //
        // Loading Fragments if needed
        //

        if (saveInstance == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        Peer peer = Peer.fromUniqueId(intent.getExtras().getLong(EXTRA_CHAT_PEER));
        if (chatFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(chatFragment).commitNow();
        }
        chatFragment = ActorSDK.sharedActor().getDelegate().fragmentForChat(peer);
        if (chatFragment == null) {
            chatFragment = ChatFragment.create(peer);
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chatFragment, chatFragment)
                .commitNow();
        quote = intent.getStringExtra("forward_text_raw");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        toolbar.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, actionBarSize));
        toolbar.setMinimumHeight(actionBarSize);
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
