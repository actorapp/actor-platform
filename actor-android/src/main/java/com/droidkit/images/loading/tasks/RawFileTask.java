package com.droidkit.images.loading.tasks;

import com.droidkit.images.loading.AbsTask;
import com.droidkit.images.util.HashUtil;

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
