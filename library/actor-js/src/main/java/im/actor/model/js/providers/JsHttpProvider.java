/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

import im.actor.model.HttpDownloaderProvider;
import im.actor.model.crypto.CryptoUtils;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;
import im.actor.model.js.providers.http.JsXmlHttpRequest;
import im.actor.model.js.providers.http.JsXmlHttpRequestHandler;
import im.actor.model.log.Log;

public class JsHttpProvider implements HttpDownloaderProvider {

    @Override
    public void downloadPart(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {

    }

    @Override
    public void uploadPart(final String url, byte[] contents, final FileUploadCallback callback) {
        Log.d("JsHttpProvider", "Uploading file: " + url);
        JsXmlHttpRequest request = JsXmlHttpRequest.create();
        request.open("PUT", url);
        request.setRequestHeader("Content-Type", "application/octet-stream");
        request.setOnLoadHandler(new JsXmlHttpRequestHandler() {
            @Override
            public void onStateChanged(JsXmlHttpRequest request) {
                Log.d("JsHttpProvider", "Upload progress: " + request.getReadyState() + " " + request.getStatus());
                if (request.getReadyState() == 4) {
                    if (request.getStatus() == 200) {
                        callback.onUploaded();
                    } else {
                        callback.onUploadFailure();
                    }
                }
            }
        });
        Uint8Array push = TypedArrays.createUint8Array(contents.length);
        for (int i = 0; i < contents.length; i++) {
            push.set(i, contents[i]);
        }
        Log.d("JsHttpProvider", CryptoUtils.hex(contents));
        request.send(push.buffer());
//        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
//        builder.setHeader("Content-Type", "application/octet-stream");
//        builder.setRequestData(new String(contents));
//        builder.setTimeoutMillis(15000);
//        builder.setCallback(new RequestCallback() {
//            @Override
//            public void onResponseReceived(Request request, Response response) {
//                Log.d("JsHttpProvider", "Uploading success #" + response.getStatusCode() + " " + url);
//                if (response.getStatusCode() == 200) {
//                    callback.onUploaded();
//                } else {
//                    callback.onUploadFailure();
//                }
//            }
//
//            @Override
//            public void onError(Request request, Throwable exception) {
//                Log.d("JsHttpProvider", "Uploading failure " + url);
//                callback.onUploadFailure();
//            }
//        });
//        try {
//            builder.send();
//        } catch (RequestException e) {
//            e.printStackTrace();
//        }
    }
}
