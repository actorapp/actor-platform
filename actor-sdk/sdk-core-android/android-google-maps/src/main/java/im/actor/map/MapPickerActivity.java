package im.actor.map;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import im.actor.maps.google.R;

public class MapPickerActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationChangeListener,
        AdapterView.OnItemClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, AbsListView.OnScrollListener {

    private static final String LOG_TAG = "MapPickerActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location currentLocation;
    private LatLng geoData;

    private PlaceFetchingTask fetchingTask;
    private Marker currentPick;

    View select;
    private ListView list;
    private TextView status;
    private View header;
    private ProgressBar loading;
    private SearchView searchView;
    private ImageView fullSizeButton;
    private View listHolder;
    private View mapHolder;
    private View defineMyLocationButton;
    private TextView accuranceView;
    private View pickCurrent;

    private HashMap<String, Marker> markers;
    private ArrayList<MapItem> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_map_picker);
        list = (ListView) findViewById(R.id.list);
        list.setOnScrollListener(this);
        list.setOnItemClickListener(this);
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        loading = (ProgressBar) findViewById(R.id.loading);
        status = (TextView) findViewById(R.id.status);
        header = findViewById(R.id.header);
        listHolder = findViewById(R.id.listNearbyHolder);
        mapHolder = findViewById(R.id.mapholder);
        accuranceView = (TextView) findViewById(R.id.accurance);

        setUpMapIfNeeded();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        fullSizeButton = (ImageView) findViewById(R.id.full);
        fullSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePlacesList();
            }
        });

        defineMyLocationButton = findViewById(R.id.define_my_location);
        defineMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location location = mMap.getMyLocation();

                if (location != null) {

                    LatLng target = new LatLng(location.getLatitude(), location.getLongitude());

                    CameraPosition.Builder builder = new CameraPosition.Builder();
                    builder.zoom(17);
                    builder.target(target);

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

                } else {
                    Toast.makeText(getBaseContext(), R.string.picker_map_pick_my_wait, Toast.LENGTH_SHORT).show();

                }
            }
        });

        pickCurrent = findViewById(R.id.pick_current);
        pickCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", currentLocation.getLatitude());
                    returnIntent.putExtra("longitude", currentLocation.getLongitude());

                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });


        // we dont need these buttons
        select = findViewById(R.id.select);
        select.setEnabled(false);
        findViewById(R.id.select_text).setEnabled(false);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude", geoData.latitude);
                returnIntent.putExtra("longitude", geoData.longitude);

                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        View cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private int defaultHeight = 0;

    protected void togglePlacesList() {
        // todo animate it
        if (listHolder.getVisibility() == View.GONE) {

            fullSizeButton.setEnabled(false);
            float startSize = findViewById(R.id.container).getHeight();

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(startSize, defaultHeight);
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mapHolder.getLayoutParams().height = ((Float) valueAnimator.getAnimatedValue()).intValue();
                    mapHolder.requestLayout();
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fullSizeButton.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            valueAnimator.start();

            listHolder.setVisibility(View.VISIBLE);
            fullSizeButton.setImageResource(R.drawable.picker_map_fullscreen_icon);
        } else {

            fullSizeButton.setEnabled(false);
            float endSize = findViewById(R.id.container).getHeight();
            defaultHeight = mapHolder.getHeight();

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(defaultHeight, endSize);
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mapHolder.getLayoutParams().height = ((Float) valueAnimator.getAnimatedValue()).intValue();
                    mapHolder.requestLayout();
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fullSizeButton.setEnabled(true);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });


            AlphaAnimation hideAnimation = new AlphaAnimation(1, 0);
            hideAnimation.setDuration(300);
            hideAnimation.setInterpolator(new AccelerateInterpolator());
            hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fullSizeButton.setImageResource(R.drawable.picker_map_halfscreen_icon);
                    listHolder.setVisibility(View.GONE);
                    valueAnimator.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            listHolder.startAnimation(hideAnimation);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picker_map, menu);
        searchView = (SearchView) menu.getItem(0).getActionView();
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                fetchPlaces(s);
                hideKeyBoard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                hideKeyBoard();
                return false;
            }
        });
        return true;
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        for (String provider : locationManager.getAllProviders()) {
            currentLocation = locationManager.getLastKnownLocation(provider);
            if (currentLocation != null) {
                break;
            }
        }

        if (currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14));
            fetchPlaces(null);
        }
        mMap.setOnMyLocationChangeListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    private void fetchPlaces(String query) {

        mMap.clear();
        if (currentLocation == null) {
            Toast.makeText(this, R.string.picker_map_sory_notdefined, Toast.LENGTH_SHORT).show();
            return;
        }
        list.setAdapter(null);
        status.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        fetchingTask = new PlaceFetchingTask(query, 50, currentLocation.getLatitude(), currentLocation.getLongitude()) {
            @Override
            protected void onPostExecute(Object o) {
                Log.i(LOG_TAG, o.toString());
                if (o instanceof ArrayList) {
                    loading.setVisibility(View.GONE);
                    status.setVisibility(View.GONE);
                    header.setVisibility(View.VISIBLE);
                    list.setVisibility(View.VISIBLE);
                    places = (ArrayList<MapItem>) o;
                    if (places.isEmpty()) {
                        status.setText(R.string.picker_map_nearby_empty);
                    } else {
                        list.setAdapter(new PlacesAdapter(MapPickerActivity.this, places));
                        showItemsOnTheMap(places);
                    }
                } else {
                    places = new ArrayList<MapItem>();
                    list.setAdapter(null);
                    header.setVisibility(View.GONE);
                    status.setText(R.string.picker_internalerror);
                    Toast.makeText(MapPickerActivity.this, o.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        fetchingTask.execute();
    }

    void hideKeyBoard() {
        searchView.clearFocus();
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showItemsOnTheMap(ArrayList<MapItem> array) {
        markers = new HashMap<String, Marker>();
        for (MapItem mapItem : array) {

            markers.put(mapItem.id,
                    mMap.addMarker(new MarkerOptions()
                                    .position(mapItem.getLatLng())
                                            // .title(mapItem.name)
                                    .draggable(false)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.picker_map_marker))
                    ));
        }
    }

    @Override
    public void onMyLocationChange(Location location) {

        if (currentLocation == null) {
            // do we need to attach our location on the start?
            this.currentLocation = location;
            fetchPlaces(null);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
        }
        this.currentLocation = location;
        ;
        accuranceView.setText(getString(R.string.picker_map_pick_my_accuracy, (int) currentLocation.getAccuracy()));
        Log.d("Location changed", location.toString());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        MapItem mapItem = (MapItem) adapterView.getItemAtPosition(position);


        Intent returnIntent = new Intent();
        returnIntent.putExtra("latitude", mapItem.getLatLng().latitude);
        returnIntent.putExtra("longitude", mapItem.getLatLng().longitude);
        returnIntent.putExtra("street", mapItem.vicinity);
        returnIntent.putExtra("place", mapItem.name);

        setResult(RESULT_OK, returnIntent);
        finish();

        /*
        select.setEnabled(true);
        findViewById(R.id.select_text).setEnabled(true);
        geoData = mapItem.getLatLng();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(geoData, 16));
        */

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        select.setEnabled(true);
        findViewById(R.id.select_text).setEnabled(true);
        //mMap.clear();

        // geoData = latLng;
        if (currentPick == null) {
            MarkerOptions currentPickOptions = new MarkerOptions()
                    .draggable(true)
                    .position(geoData);

            //currentPick = mMap.addMarker(currentPickOptions);
        } else {

            //currentPick.setPosition(geoData);
        }


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (currentPick != null)
            currentPick.remove();
        String placeId = null;
        for (Map.Entry<String, Marker> markerIterator : markers.entrySet()) {
            if (markerIterator.getValue().equals(marker)) {
                placeId = markerIterator.getKey();
                break;
            }
        }
        int position = -1;
        for (int i = 0; i < places.size(); i++) {
            MapItem place = places.get(i);
            if (place.id.equals(placeId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            list.setItemChecked(position, true);
            list.smoothScrollToPosition(position);
            if (listHolder.getVisibility() == View.GONE) {
                togglePlacesList();
            }
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(marker.getPosition().latitude, marker.getPosition().longitude),
                16));

        //currentPick = marker;
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        switch (i) {
            case SCROLL_STATE_TOUCH_SCROLL:
                hideKeyBoard();
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {

    }
}
