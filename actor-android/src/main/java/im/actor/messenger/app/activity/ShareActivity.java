package im.actor.messenger.app.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;

import im.actor.messenger.app.base.BaseBarFragmentActivity;
import im.actor.messenger.app.base.BaseFragmentActivity;
import im.actor.messenger.app.fragment.dialogs.ShareFragment;

/**
 * Created by ex3ndr on 20.10.14.
 */
public class ShareActivity extends BaseBarFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle("Share to...");

        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
                String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                showFragment(ShareFragment.share("text/plain", sharedText), false, false);
                return;
            } else if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
                Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                showFragment(ShareFragment.share(type, imageUri.toString()), false, false);
                return;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (imageUris != null) {
                String[] uris = new String[imageUris.size()];
                for (int i = 0; i < uris.length; i++) {
                    uris[i] = imageUris.get(i).toString();
                }
                showFragment(ShareFragment.share(type, uris), false, false);
                return;
            }
        }

        finish();
    }
}