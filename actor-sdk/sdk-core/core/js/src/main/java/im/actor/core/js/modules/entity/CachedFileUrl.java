/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules.entity;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.KeyValueItem;

/**
 * Created by ex3ndr on 01.05.15.
 */
public class CachedFileUrl extends BserObject implements KeyValueItem {

    public static CachedFileUrl fromBytes(byte[] data) throws IOException {
        return Bser.parse(new CachedFileUrl(), data);
    }

    private long fid;
    private String url;
    private long timeout;

    public CachedFileUrl(long fid, String url, long timeout) {
        this.fid = fid;
        this.url = url;
        this.timeout = timeout;
    }

    public CachedFileUrl() {
    }

    public long getFid() {
        return fid;
    }

    public String getUrl() {
        return url;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        fid = values.getLong(1);
        url = values.getString(2);
        timeout = values.getLong(3);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, fid);
        writer.writeString(2, url);
        writer.writeLong(3, timeout);
    }

    @Override
    public long getEngineId() {
        return fid;
    }
}