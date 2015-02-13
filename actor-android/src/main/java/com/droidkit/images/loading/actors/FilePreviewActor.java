package com.droidkit.images.loading.actors;

import android.graphics.Bitmap;
import com.droidkit.images.cache.MemoryCache;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageMetadata;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.loading.tasks.PreviewFileTask;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageScaling;

/**
 * Created by ex3ndr on 04.10.14.
 */
public class FilePreviewActor extends BasicTaskActor<PreviewFileTask> {

    private MemoryCache memoryCache;

    public FilePreviewActor(PreviewFileTask task, ImageLoader loader) {
        super(task, loader);
        this.memoryCache = loader.getMemoryCache();
    }

    @Override
    public void startTask() {
        Bitmap reuse = memoryCache.findExactSize(getTask().getW(), getTask().getH());
        if (reuse == null) {
            reuse = Bitmap.createBitmap(getTask().getW(), getTask().getH(), Bitmap.Config.ARGB_8888);
        }

        try {
            Bitmap tmp = ImageLoading.loadBitmap(getTask().getFileName(), getTask().getW(), getTask().getH());
            ImageScaling.scaleFit(tmp, reuse);
            completeTask(tmp);
        } catch (ImageLoadException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskObsolete() {

    }
}
