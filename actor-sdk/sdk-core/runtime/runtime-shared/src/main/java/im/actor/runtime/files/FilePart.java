package im.actor.runtime.files;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

public class FilePart {

    @Property("readonly, nonatomic")
    private int offset;
    @Property("readonly, nonatomic")
    private int partLength;
    @NotNull
    @Property("readonly, nonatomic")
    private byte[] contents;

    @ObjectiveCName("initWithOffset:withLength:withContents:")
    public FilePart(int offset, int partLength, @NotNull byte[] contents) {
        this.offset = offset;
        this.partLength = partLength;
        this.contents = contents;
    }

    public int getOffset() {
        return offset;
    }

    public int getPartLength() {
        return partLength;
    }

    @NotNull
    public byte[] getContents() {
        return contents;
    }
}
