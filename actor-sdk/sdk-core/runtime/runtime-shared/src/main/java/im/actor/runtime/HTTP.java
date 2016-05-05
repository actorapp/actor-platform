package im.actor.runtime;

import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;

public class HTTP {

    private static HttpRuntime runtime = new HttpRuntimeProvider();

    public static Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize) {
        return runtime.getMethod(url, startOffset, size, totalSize);
    }

    public static Promise<HTTPResponse> putMethod(String url, byte[] contents) {
        return runtime.putMethod(url, contents);
    }
}
