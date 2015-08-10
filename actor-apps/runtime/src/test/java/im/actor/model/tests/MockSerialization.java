package im.actor.core.tests;

import im.actor.core.droidkit.bser.BserParser;
import im.actor.core.droidkit.bser.BserWriter;
import im.actor.core.droidkit.bser.DataInput;
import im.actor.core.droidkit.bser.DataOutput;
import im.actor.core.droidkit.bser.util.SparseArray;

import java.io.IOException;

/**
* Created by ex3ndr on 08.03.15.
*/
public abstract class MockSerialization {

    public byte[] build() throws IOException {
        DataOutput dataOutput = new DataOutput();
        BserWriter bserWriter = new BserWriter(dataOutput);
        perform(bserWriter);
        return dataOutput.toByteArray();
    }

    public SparseArray<Object> buildArray() throws IOException {
        byte[] src = build();
        return BserParser.deserialize(new DataInput(src, 0, src.length));
    }

    protected abstract void perform(BserWriter writer) throws IOException;
}
