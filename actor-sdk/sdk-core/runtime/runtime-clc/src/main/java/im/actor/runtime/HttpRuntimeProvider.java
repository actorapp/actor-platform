package im.actor.runtime;

import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;

public class HttpRuntimeProvider implements HttpRuntime {

    @Override
    public Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize) {
        return Promise.failure(new RuntimeException("Dumb"));
    }

    @Override
    public Promise<HTTPResponse> putMethod(String url, byte[] contents) {
        return Promise.failure(new RuntimeException("Dumb"));
    }
}
