package im.actor.android;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import im.actor.model.HttpDownloaderProvider;
import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;

/**
 * Created by ex3ndr on 23.04.15.
 */
public class AndroidHttpSupport implements HttpDownloaderProvider {

    private final OkHttpClient client = new OkHttpClient();

    private final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");

    @Override
    public void downloadPart(String url, int startOffset, int size, int totalSize, final FileDownloadCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Range", "bytes " + startOffset + "-" + (startOffset + size) + "/" + totalSize)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onDownloadFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    callback.onDownloaded(response.body().bytes());
                } else {
                    callback.onDownloadFailure();
                }
            }
        });
    }

    @Override
    public void uploadPart(String url, byte[] contents, final FileUploadCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .method("PUT", RequestBody.create(MEDIA_TYPE, contents))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onUploadFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    callback.onUploadFailure();
                } else {
                    callback.onUploaded();
                }
            }
        });
    }
}
