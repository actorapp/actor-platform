package im.actor.sdk.controllers.tools;

import android.net.Uri;

import java.util.List;

public interface MediaPickerCallback {
    void onUriPicked(Uri uri);

    void onFilesPicked(List<String> paths);

    void onPhotoPicked(String path);

    void onVideoPicked(String path);

    void onContactPicked(String name, List<String> phones, List<String> emails, byte[] avatar);

    void onLocationPicked(double latitude, double longitude, String street, String place);
}
