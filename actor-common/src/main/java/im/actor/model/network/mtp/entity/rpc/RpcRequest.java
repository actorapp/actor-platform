package im.actor.model.network.mtp.entity.rpc;


import im.actor.model.network.mtp.entity.ProtoStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class RpcRequest extends ProtoStruct {

    public static final byte HEADER = (byte) 0x01;

    public int requestType;

    public byte[] payload;

    public RpcRequest(InputStream stream) throws IOException {
        super(stream);
    }

    public RpcRequest(int requestType, byte[] payload) {
        this.requestType = requestType;
        this.payload = payload;
    }

    public int getRequestType() {
        return requestType;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public int getLength() {
        return 1 + 4 + varintSize(payload.length) + payload.length;
    }

    @Override
    protected byte getHeader() {
        return HEADER;
    }

    @Override
    protected void writeBody(OutputStream bs) throws IOException {
        writeInt(requestType, bs);
        writeProtoBytes(payload, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        requestType = readInt(bs);
        payload = readProtoBytes(bs);
    }

    @Override
    public String toString() {
        return "RpcRequest[" + requestType + "]";
    }
}
