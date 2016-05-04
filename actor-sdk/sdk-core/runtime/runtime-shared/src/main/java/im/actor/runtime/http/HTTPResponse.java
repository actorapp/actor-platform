package im.actor.runtime.http;

public class HTTPResponse {
    
    private int code;
    private byte[] content;

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
