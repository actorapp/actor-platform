package im.actor.runtime;

import im.actor.runtime.http.FileDownloadCallback;
import im.actor.runtime.http.FileUploadCallback;

/**
 * Created by ex3ndr on 07.08.15.
 */
public class HttpRuntimeProvider implements HttpRuntime {

    @Override
    public void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {
        throw new RuntimeException("Dumb");
    }

    @Override
    public void putMethod(String url, byte[] contents, FileUploadCallback callback) {
        throw new RuntimeException("Dumb");
    }
}
