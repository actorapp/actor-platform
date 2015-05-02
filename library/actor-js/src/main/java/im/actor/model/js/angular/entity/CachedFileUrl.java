package im.actor.model.js.angular.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.KeyValueItem;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class CachedFileUrl extends BserObject implements KeyValueItem {

    public static CachedFileUrl fromBytes(byte[] data) throws IOException {
        return Bser.parse(new CachedFileUrl(), data);
    }

    private long fid;
    private String url;

    public CachedFileUrl(long fid, String url) {
        this.fid = fid;
        this.url = url;
    }

    public CachedFileUrl() {
    }

    public long getFid() {
        return fid;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fid = values.getInt(1);
        url = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fid);
        writer.writeString(2, url);
    }

    @Override
    public long getEngineId() {
        return fid;
    }
}