package im.actor.messenger.core.images;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageSaveException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageScaling;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class VideoActor extends BasicTaskActor<VideoTask> {

    public VideoActor(VideoTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        String cached = getLoader().getInternalDiskCache().lockFile(getTask().getKey());
        if (cached != null) {
            try {
                completeTask(ImageLoading.loadBitmap(cached));
                return;
            } catch (ImageLoadException e) {
                e.printStackTrace();
            } finally {
                getLoader().getInternalDiskCache().unlockFile(getTask().getKey());
            }
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getTask().getFileName());
        Bitmap img = retriever.getFrameAtTime(0);

        Bitmap res = ImageScaling.scaleFit(img, 800, 800);
        img.recycle();

        String fileName = getLoader().getInternalDiskCache().startWriteFile(getTask().getKey());
        try {
            ImageLoading.save(res, fileName);
            getLoader().getInternalDiskCache().commitFile(getTask().getKey());
            completeTask(res);
        } catch (ImageSaveException e) {
            e.printStackTrace();
            error(e);
        }
    }

    @Override
    public void onTaskObsolete() {

    }
}
