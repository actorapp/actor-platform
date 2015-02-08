package im.actor.model.network.mtp.entity.rpc;


import im.actor.model.network.mtp.entity.ProtoStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static im.actor.model.util.StreamingUtils.*;

public class RpcOk extends ProtoStruct {

    public static final byte HEADER = (byte) 0x01;

    public int responseType;
    public byte[] payload;

    public RpcOk(InputStream stream) throws IOException {
        super(stream);
    }

    public RpcOk(int responseType, byte[] payload) {
        this.responseType = responseType;
        this.payload = payload;
    }

    public int getResponseType() {
        return responseType;
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
        writeInt(responseType, bs);
        writeProtoBytes(payload, bs);
    }

    @Override
    protected void readBody(InputStream bs) throws IOException {
        responseType = readInt(bs);
        payload = readProtoBytes(bs);
    }


    @Override
    public String toString() {
        return "RpcOk{" + responseType + "}";
    }
}
