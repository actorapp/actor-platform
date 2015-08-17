/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.entity;

import java.io.IOException;

import im.actor.runtime.bser.DataInput;
import im.actor.core.network.mtp.entity.rpc.Push;
import im.actor.core.network.mtp.entity.rpc.RpcError;
import im.actor.core.network.mtp.entity.rpc.RpcFloodWait;
import im.actor.core.network.mtp.entity.rpc.RpcInternalError;
import im.actor.core.network.mtp.entity.rpc.RpcOk;
import im.actor.core.network.mtp.entity.rpc.RpcRequest;

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
            case SessionLost.HEADER:
                return new SessionLost(bs);
            case AuthIdInvalid.HEADER:
                return new AuthIdInvalid(bs);

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
