package im.actor.model.network.mtp.entity.rpc;


import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.droidkit.bser.DataInput;
import im.actor.model.droidkit.bser.DataOutput;

import java.io.IOException;

/**
 * Created by ex3ndr on 03.09.14.
 */
public class Push extends ProtoStruct {
    public int updateType;
    public byte[] body;

    public Push(DataInput stream) throws IOException {
        super(stream);
    }

    public Push(int updateType, byte[] body) {
        this.updateType = updateType;
        this.body = body;
    }

    @Override
    public byte getHeader() {
        return 0;
    }

    @Override
    protected void writeBody(DataOutput bs) throws IOException {
        bs.writeInt(updateType);
        bs.writeProtoBytes(body, 0, body.length);
    }

    @Override
    protected void readBody(DataInput bs) throws IOException {
        updateType = bs.readInt();
        body = bs.readProtoBytes();
    }

    @Override
    public String toString() {
        return "Update[" + updateType + "]";
    }
}
