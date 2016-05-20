package im.actor.map;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import im.actor.map.MapItem;

/**
 * Created by kiolt_000 on 22/09/2014.
 */
public class PlaceFetchingTask extends AsyncTask<Void, Void, Object> {
    private static final String LOG_TAG = "ExampleApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String METHOD_NAME = "/search";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBV6Kul7Ybt_7yeEd6Im7gSjN_0fNu_Psw";
    private final String query;
    private final int radius;
    private final double latitude;
    private final double longitude;
    // todo google places api key ?


    public PlaceFetchingTask(String query, int radius, double latitude, double longitude) {
        this.query = query;
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    protected Object doInBackground(Void... voids) {

        ArrayList<MapItem> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + METHOD_NAME + OUT_JSON);
            sb.append("?key=" + API_KEY);
            // sb.append("&radius="+radius);
            sb.append("&rankby=distance");
            sb.append("&location=" + latitude + "," + longitude);
            if (query != null)
                sb.append("&keyword=" + URLEncoder.encode(query, "utf8"));
            else
                sb.append("&types=cafe");
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            Log.i(LOG_TAG, "Response: " + jsonResults.toString());
        } catch (MalformedURLException e) {
            if (conn != null) {
                conn.disconnect();
            }
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return e;
        } catch (IOException e) {
            if (conn != null) {
                conn.disconnect();
            }
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonResult = new JSONObject(jsonResults.toString());
            JSONArray jsonResultItems = jsonResult.getJSONArray("results");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<MapItem>(jsonResultItems.length());

            for (int i = 0; i < jsonResultItems.length(); i++) {
                // todo json parser

                final JSONObject jsonResultItem = jsonResultItems.getJSONObject(i);
                MapItem item = new MapItem() {
                    {
                        id = jsonResultItem.optString("id", null);
                        name = jsonResultItem.optString("name", null);
                        vicinity = jsonResultItem.optString("vicinity", null);
                        icon = jsonResultItem.optString("icon", null);
                        if (jsonResultItem.has("geometry")) {
                            geometry = new Geometry();
                            geometry.location = new Location() {
                                {
                                    lat = jsonResultItem.optJSONObject("geometry").optJSONObject("location").optDouble("lat", 0.0);
                                    lng = jsonResultItem.optJSONObject("geometry").optJSONObject("location").optDouble("lng", 0.0);
                                }
                            };
                        }
                    }
                };
                resultList.add(item);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
            return e;
        }

        return resultList;

    }
}
