package im.actor.map;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import im.actor.maps.google.R;
import im.actor.sdk.controllers.activity.BaseFragmentActivity;
import im.actor.sdk.util.ViewUtils;
import im.actor.sdk.view.RTLUtils;

public class MapActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.map_title);

        Drawable back_icon = getResources().getDrawable(im.actor.sdk.R.drawable.ic_arrow_back_white_24dp);
        if(RTLUtils.isRTL(getApplicationContext())) {
            back_icon = ViewUtils.getRotateDrawable(back_icon, 180);
        }
        getSupportActionBar().setHomeAsUpIndicator(back_icon);

        double longitude = getIntent().getDoubleExtra("longitude", 0);
        double latitude = getIntent().getDoubleExtra("latitude", 0);

        if (savedInstanceState == null) {
            showFragment(MapFragment.create(longitude, latitude), false, false);
        }

    }

}