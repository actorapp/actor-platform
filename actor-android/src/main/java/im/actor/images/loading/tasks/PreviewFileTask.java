package im.actor.images.loading.tasks;

import im.actor.images.loading.AbsTask;
import im.actor.images.util.HashUtil;

/**
 * Preview task for file
 */
public class PreviewFileTask extends AbsTask {
    private String fileName;
    private int w;
    private int h;

    public PreviewFileTask(String fileName, int w, int h) {
        this.fileName = fileName;
        this.w = w;
        this.h = h;
    }

    public String getFileName() {
        return fileName;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    @Override
    public String getKey() {
        return "file:" + w + ":" + h + HashUtil.md5(fileName);
    }
}
