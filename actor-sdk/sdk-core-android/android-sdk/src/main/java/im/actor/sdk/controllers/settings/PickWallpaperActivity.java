package im.actor.sdk.controllers.settings;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

public class PickWallpaperActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = getIntent().getIntExtra("EXTRA_ID", 0);

        getSupportActionBar().setTitle(R.string.wallpaper);

        if (savedInstanceState == null) {
            showFragment(PickWallpaperFragment.chooseWallpaper(id), false, false);
        }
    }
}
