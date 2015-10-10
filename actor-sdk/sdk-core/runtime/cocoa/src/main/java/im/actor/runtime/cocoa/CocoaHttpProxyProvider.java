package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.HttpRuntime;
import im.actor.runtime.http.FileDownloadCallback;
import im.actor.runtime.http.FileUploadCallback;

public class CocoaHttpProxyProvider implements HttpRuntime {

    private static HttpRuntime runtime;

    @ObjectiveCName("setHttpRuntime:")
    public static void setHttpRuntime(HttpRuntime runtime) {
        CocoaHttpProxyProvider.runtime = runtime;
    }

    @Override
    public void getMethod(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback) {
        runtime.getMethod(url, startOffset, size, totalSize, callback);
    }

    @Override
    public void putMethod(String url, byte[] contents, FileUploadCallback callback) {
        runtime.putMethod(url, contents, callback);
    }
}
