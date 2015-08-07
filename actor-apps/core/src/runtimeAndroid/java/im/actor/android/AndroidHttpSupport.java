/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import im.actor.model.HttpProvider;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;
import im.actor.model.log.Log;

public class AndroidHttpSupport implements HttpProvider {

    private static final String TAG = "AndroidHTTP";

    private final OkHttpClient client = new OkHttpClient();

    private final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");

    @Override
    public void getMethod(String url, int startOffset, int size, int totalSize, final FileDownloadCallback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=" + startOffset + "-" + (startOffset + size))
                .build();
        Log.d(TAG, "Downloading part: " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "Downloading part error: " + request.toString());
                e.printStackTrace();
                callback.onDownloadFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, "Downloading part response: " + request.toString() + " -> " + response.toString());
                if (response.code() == 206) {
                    callback.onDownloaded(response.body().bytes());
                } else {
                    callback.onDownloadFailure();
                }
            }
        });
    }

    @Override
    public void putMethod(String url, byte[] contents, final FileUploadCallback callback) {
        final Request request = new Request.Builder()
                .url(url)
                .method("PUT", RequestBody.create(MEDIA_TYPE, contents))
                .build();
        Log.d(TAG, "Uploading part: " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "Uploading part error: " + request.toString());
                e.printStackTrace();
                callback.onUploadFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, "Upload part response: " + request.toString() + " -> " + response.toString());
                if (response.code() == 200) {
                    callback.onUploaded();
                } else {
                    callback.onUploadFailure();
                }
            }
        });
    }
}
