package im.actor.map;

import com.google.android.gms.maps.model.LatLng;

public class MapItem {
    public String id;
    public String icon;
    public Geometry geometry;
    public String name;
    public String vicinity;
    public String[] types;


    public LatLng getLatLng() {
        return new LatLng(geometry.location.lat, geometry.location.lng);
    }

    public static class Geometry {
        public Location location;
    }

    public static class Location {
        public double lat;
        public double lng;
    }
}
