package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.HttpRuntime;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;

public class CocoaHttpProxyProvider implements HttpRuntime {

    private static HttpRuntime runtime;

    @ObjectiveCName("setHttpRuntime:")
    public static void setHttpRuntime(HttpRuntime runtime) {
        CocoaHttpProxyProvider.runtime = runtime;
    }

    @Override
    public Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize) {
        return runtime.getMethod(url, startOffset, size, totalSize);
    }

    @Override
    public Promise<HTTPResponse> putMethod(String url, byte[] contents) {
        return runtime.putMethod(url, contents);
    }
}
