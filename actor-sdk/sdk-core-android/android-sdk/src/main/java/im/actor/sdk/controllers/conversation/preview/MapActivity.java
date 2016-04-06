package im.actor.sdk.controllers.conversation.preview;

import android.os.Bundle;

import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

/**
 * Created by root on 10/22/15.
 */
public class MapActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.map_title);

        double longitude = getIntent().getDoubleExtra("longitude", 0);
        double latitude = getIntent().getDoubleExtra("latitude", 0);

        if (savedInstanceState == null) {
            showFragment(MapFragment.create(longitude, latitude), false, false);
        }

    }

}