package im.actor.model.network.mtp.entity;

import im.actor.model.network.mtp.entity.rpc.*;
import im.actor.model.droidkit.bser.DataInput;

import java.io.IOException;

public class ProtoSerializer {
    public static ProtoStruct readMessagePayload(byte[] bs) throws IOException {
        return readMessagePayload(new DataInput(bs, 0, bs.length));
    }

    public static ProtoStruct readMessagePayload(DataInput bs) throws IOException {
        final int header = bs.readByte();

        switch (header) {
            case Ping.HEADER:
                return new Ping(bs);
            case Pong.HEADER:
                return new Pong(bs);
            case Drop.HEADER:
                return new Drop(bs);
            case Container.HEADER:
                return new Container(bs);
            case MTRpcRequest.HEADER:
                return new MTRpcRequest(bs);
            case MTRpcResponse.HEADER:
                return new MTRpcResponse(bs);
            case MessageAck.HEADER:
                return new MessageAck(bs);
            case NewSessionCreated.HEADER:
                return new NewSessionCreated(bs);
            case MTPush.HEADER:
                return new MTPush(bs);
            case UnsentMessage.HEADER:
                return new UnsentMessage(bs);
            case UnsentResponse.HEADER:
                return new UnsentResponse(bs);
            case RequestResend.HEADER:
                return new UnsentResponse(bs);
        }

        throw new IOException("Unable to read proto object with header #" + header);
    }

    public static ProtoStruct readRpcResponsePayload(byte[] data) throws IOException {
        DataInput bs = new DataInput(data, 0, data.length);
        final int header = bs.readByte();
        switch (header) {
            case RpcOk.HEADER:
                return new RpcOk(bs);
            case RpcError.HEADER:
                return new RpcError(bs);
            case RpcFloodWait.HEADER:
                return new RpcFloodWait(bs);
            case RpcInternalError.HEADER:
                return new RpcInternalError(bs);
        }
        throw new IOException("Unable to read proto object");
    }

    public static ProtoStruct readRpcRequestPayload(DataInput bs) throws IOException {
        final int header = bs.readByte();
        switch (header) {
            case RpcRequest.HEADER:
                return new RpcRequest(bs);
        }
        throw new IOException("Unable to read proto object with header #" + header);
    }

    public static Push readUpdate(byte[] bs) throws IOException {
        return readUpdate(new DataInput(bs, 0, bs.length));
    }

    public static Push readUpdate(DataInput bs) throws IOException {
        return new Push(bs);
    }
}
