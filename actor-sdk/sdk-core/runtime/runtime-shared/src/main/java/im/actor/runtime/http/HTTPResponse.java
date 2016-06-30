package im.actor.runtime.http;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

public class HTTPResponse {

    @Property("readonly, nonatomic")
    private final int code;

    @Property("readonly, nonatomic")
    private final byte[] content;

    @ObjectiveCName("initWithCode:withContent:")
    public HTTPResponse(int code, byte[] content) {
        this.code = code;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public byte[] getContent() {
        return content;
    }
}
