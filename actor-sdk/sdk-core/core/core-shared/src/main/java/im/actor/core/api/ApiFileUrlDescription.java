package im.actor.core.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ApiFileUrlDescription extends BserObject {

    private long fileId;
    private String url;
    private int timeout;
    private String unsignedUrl;
    private List<ApiHTTPHeader> unsignedUrlHeaders;

    public ApiFileUrlDescription(long fileId, @NotNull String url, int timeout, @Nullable String unsignedUrl, @NotNull List<ApiHTTPHeader> unsignedUrlHeaders) {
        this.fileId = fileId;
        this.url = url;
        this.timeout = timeout;
        this.unsignedUrl = unsignedUrl;
        this.unsignedUrlHeaders = unsignedUrlHeaders;
    }

    public ApiFileUrlDescription() {

    }

    public long getFileId() {
        return this.fileId;
    }

    @NotNull
    public String getUrl() {
        return this.url;
    }

    public int getTimeout() {
        return this.timeout;
    }

    @Nullable
    public String getUnsignedUrl() {
        return this.unsignedUrl;
    }

    @NotNull
    public List<ApiHTTPHeader> getUnsignedUrlHeaders() {
        return this.unsignedUrlHeaders;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.fileId = values.getLong(1);
        this.url = values.getString(2);
        this.timeout = values.getInt(3);
        this.unsignedUrl = values.optString(4);
        List<ApiHTTPHeader> _unsignedUrlHeaders = new ArrayList<ApiHTTPHeader>();
        for (int i = 0; i < values.getRepeatedCount(5); i ++) {
            _unsignedUrlHeaders.add(new ApiHTTPHeader());
        }
        this.unsignedUrlHeaders = values.getRepeatedObj(5, _unsignedUrlHeaders);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, this.fileId);
        if (this.url == null) {
            throw new IOException();
        }
        writer.writeString(2, this.url);
        writer.writeInt(3, this.timeout);
        if (this.unsignedUrl != null) {
            writer.writeString(4, this.unsignedUrl);
        }
        writer.writeRepeatedObj(5, this.unsignedUrlHeaders);
    }

    @Override
    public String toString() {
        String res = "struct FileUrlDescription{";
        res += "fileId=" + this.fileId;
        res += ", url=" + this.url;
        res += ", timeout=" + this.timeout;
        res += "}";
        return res;
    }

}
