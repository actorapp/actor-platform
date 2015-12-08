package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class LocationContent extends AbsContent {

    private double latitude;
    private double longitude;
    private String street;
    private String place;
    private String rawJson;

    @NotNull
    public static LocationContent create(double longitude, double latitude, @Nullable String street, @Nullable String place) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("dataType", "location");
            JSONObject location = new JSONObject();
            location.put("latitude", latitude);
            location.put("longitude", longitude);
            if (street != null) {
                location.put("street", street);
            }
            if (place != null) {
                location.put("place", place);
            }
            JSONObject data = new JSONObject();
            data.put("location", location);
            obj.put("data", data);
            return new LocationContent(new ContentRemoteContainer(new ApiJsonMessage(obj.toString())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public LocationContent(ContentRemoteContainer contentContainer) throws JSONException {
        super(contentContainer);

        rawJson = ((ApiJsonMessage) contentContainer.getMessage()).getRawJson();
        JSONObject data = new JSONObject(rawJson).getJSONObject("data");
        JSONObject location = data.getJSONObject("location");
        latitude = location.getDouble("latitude");
        longitude = location.getDouble("longitude");
        street = location.optString("street");
        place = location.optString("place");
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getStreet() {
        return street;
    }

    @Nullable
    public String getPlace() {
        return place;
    }

    public String getRawJson() {
        return rawJson;
    }
}
