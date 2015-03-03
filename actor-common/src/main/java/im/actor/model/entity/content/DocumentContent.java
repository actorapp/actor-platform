package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class DocumentContent extends AbsContent {

    public static DocumentContent docFromBytes(byte[] data) throws IOException {
        return Bser.parse(new DocumentContent(), data);
    }

    protected FileSource source;
    protected String mimetype;
    protected String name;
    protected FastThumb fastThumb;

    public DocumentContent(FileSource source, String mimetype, String name, FastThumb fastThumb) {
        this.source = source;
        this.mimetype = mimetype;
        this.name = name;
        this.fastThumb = fastThumb;
    }

    protected DocumentContent() {

    }

    public FileSource getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public FastThumb getFastThumb() {
        return fastThumb;
    }

    public String getExt() {
        String ext = "";
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = name.substring(dotIndex + 1);
        }
        return ext;
    }

    public String getMimetype() {
        return mimetype;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.DOCUMENT;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        source = FileSource.fromBytes(values.getBytes(2));
        mimetype = values.getString(3);
        name = values.getString(4);
        byte[] ft = values.optBytes(5);
        if (ft != null) {
            fastThumb = FastThumb.fromBytes(ft);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        writer.writeBytes(2, source.toByteArray());
        writer.writeString(3, mimetype);
        writer.writeString(4, name);
        if (fastThumb != null) {
            writer.writeObject(5, fastThumb);
        }
    }
}
