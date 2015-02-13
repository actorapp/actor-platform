package im.actor.messenger.app.activity;

import android.os.Bundle;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.media.DocumentsFragment;
import im.actor.messenger.app.intents.Intents;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DocumentsActivity extends BaseBarFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.documents_title);

        int chatType = getIntent().getIntExtra(Intents.EXTRA_CHAT_TYPE, 0);
        int chatId = getIntent().getIntExtra(Intents.EXTRA_CHAT_ID, 0);
        showFragment(DocumentsFragment.open(chatType, chatId), false, false);
    }
}
