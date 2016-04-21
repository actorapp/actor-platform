package im.actor.map;

import android.os.Bundle;

import im.actor.maps.google.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;

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