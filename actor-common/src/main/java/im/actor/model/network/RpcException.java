package im.actor.model.network;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class RpcException extends Exception {
    private String tag;
    private int code;
    private String message;
    private byte[] relatedData;

    public RpcException(String tag, int code, String message, byte[] relatedData) {
        this.tag = tag;
        this.code = code;
        this.message = message;
        this.relatedData = relatedData;
    }

    public byte[] getRelatedData() {
        return relatedData;
    }

    public String getTag() {
        return tag;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
