package im.actor.messenger.storage.scheme.messages;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class FastThumb extends BserObject {
    private int w;
    private int h;
    private byte[] image;

    public FastThumb(int w, int h, byte[] image) {
        this.w = w;
        this.h = h;
        this.image = image;
    }

    public FastThumb() {
    }


    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public byte[] getImage() {
        return image;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        w = values.getInt(1);
        h = values.getInt(2);
        image = values.getBytes(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, w);
        writer.writeInt(2, h);
        writer.writeBytes(3, image);
    }
}
