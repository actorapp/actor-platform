/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import im.actor.model.HttpDownloaderProvider;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;
import im.actor.model.log.Log;

public class JsHttpProvider implements HttpDownloaderProvider {

    @Override
    public void downloadPart(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {

    }

    @Override
    public void uploadPart(final String url, byte[] contents, final FileUploadCallback callback) {
        Log.d("JsHttpProvider", "Uploading file: " + url);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
        builder.setHeader("Content-Type", "application/octet-stream");
        builder.setRequestData(new String(contents));
        builder.setTimeoutMillis(15000);
        builder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Log.d("JsHttpProvider", "Uploading success #" + response.getStatusCode() + " " + url);
                if (response.getStatusCode() == 200) {
                    callback.onUploaded();
                } else {
                    callback.onUploadFailure();
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Log.d("JsHttpProvider", "Uploading failure " + url);
                callback.onUploadFailure();
            }
        });
        try {
            builder.send();
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}
