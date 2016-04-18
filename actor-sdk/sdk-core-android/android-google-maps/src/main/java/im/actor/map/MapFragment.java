package im.actor.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import im.actor.maps.google.R;
import im.actor.sdk.controllers.fragment.BaseFragment;

public class MapFragment extends BaseFragment {
    double longitude;
    double latitude;

    public static MapFragment create(double longitude, double latitude) {
        MapFragment res = new MapFragment();
        Bundle arguments = new Bundle();
        arguments.putDouble("longitude", longitude);
        arguments.putDouble("latitude", latitude);
        res.setArguments(arguments);
        return res;
    }

    GoogleMap mapController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        longitude = getArguments().getDouble("longitude");
        latitude = getArguments().getDouble("latitude");


        final MapView map = new MapView(getActivity());

        map.onCreate(null);

        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapController = googleMap;
                googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(""));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(latitude, longitude),
                        16));
                map.onResume();
            }
        });

        return map;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            String uri = "geo:" + latitude + ","
                    + longitude + "?q=" + latitude
                    + "," + longitude;
            startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(uri)));
            return true;

        } else if (mapController != null) {
            if (item.getItemId() == R.id.roadmap) {
                mapController.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else if (item.getItemId() == R.id.satellite) {
                mapController.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (item.getItemId() == R.id.hybrid) {
                mapController.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
