package im.actor.runtime;

import im.actor.runtime.http.FileDownloadCallback;
import im.actor.runtime.http.FileUploadCallback;

public class HTTP {

    private static HttpRuntime runtime = new HttpRuntimeProvider();

    public static void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {
        runtime.getMethod(url, startOffset, size, totalSize, callback);
    }

    public static void putMethod(String url, byte[] contents, FileUploadCallback callback) {
        runtime.putMethod(url, contents, callback);
    }
}
