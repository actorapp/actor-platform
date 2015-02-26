package im.actor.messenger.core.images;

import com.droidkit.images.loading.AbsTask;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class FastImageTask extends AbsTask {

    private byte[] data;
    private long fileId;

    public FastImageTask(byte[] data, long fileId) {
        this.data = data;
        this.fileId = fileId;
    }

    public byte[] getData() {
        return data;
    }

    public long getFileId() {
        return fileId;
    }

    @Override
    public String getKey() {
        return "fast#" + fileId;
    }
}
