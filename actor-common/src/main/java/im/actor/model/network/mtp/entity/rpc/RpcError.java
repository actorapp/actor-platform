package im.actor.model.network.mtp.entity.rpc;

import im.actor.model.network.mtp.entity.ProtoStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class RpcError extends ProtoStruct {

    public static final byte HEADER = (byte) 0x02;

    public int errorCode;
    public String errorTag;
    public String userMessage;
    public boolean canTryAgain;
    public byte[] relatedData;

    public RpcError(InputStream stream) throws IOException {
        super(stream);
    }

    public RpcError(int errorCode, String errorTag, String userMessage, boolean canTryAgain, byte[] relatedData) {
        this.errorCode = errorCode;
        this.errorTag = errorTag;
        this.userMessage = userMessage;
        this.canTryAgain = canTryAgain;
        this.relatedData = relatedData;
    }

    @Override
    public int getLength() {
        return 1 + 4 + stringSize(errorTag) + stringSize(userMessage) + 1;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeInt(errorCode, bs);
        writeProtoString(errorTag, bs);
        writeProtoString(userMessage, bs);
        writeProtoBool(canTryAgain, bs);
        writeProtoBytes(relatedData, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        errorCode = readInt(bs);
        errorTag = readProtoString(bs);
        userMessage = readProtoString(bs);
        canTryAgain = readProtoBool(bs);
        relatedData = readProtoBytes(bs);
    }

    @Override
    public String toString() {
        return "RpcError [#" + errorCode + " " + errorTag + "]";
    }
}
