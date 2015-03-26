package im.actor.images.loading.tasks;

import im.actor.images.loading.AbsTask;
import im.actor.images.util.HashUtil;

/**
 * Raw file task
 */
public class RawFileTask extends AbsTask {
    private final String fileName;

    public RawFileTask(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String getKey() {
        return "file:" + HashUtil.md5(fileName);
    }
}
