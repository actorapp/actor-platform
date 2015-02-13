package im.actor.messenger.core.images;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageSaveException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageScaling;

import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;

import static im.actor.messenger.core.actors.files.DownloadManager.downloader;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class VideoPreviewActor extends BasicTaskActor<VideoPreviewTask> {

    private FileLocation fileLocation;

    public VideoPreviewActor(VideoPreviewTask task, ImageLoader loader) {
        super(task, loader);
        fileLocation = ((AbsFileMessage) task.getMessage().getContent()).getLocation();
    }

    @Override
    public void startTask() {
        ask(downloader().downloadedFileName(fileLocation), new FutureCallback<String>() {
            @Override
            public void onResult(String fileName) {
                loadFile(fileName);
            }

            @Override
            public void onError(Throwable throwable) {
                error(new RuntimeException(throwable));
            }
        });
    }

    private void loadFile(String downloadedFileName) {
        String cached = getLoader().getInternalDiskCache().lockFile(downloadedFileName);
        if (cached != null) {
            try {
                completeTask(ImageLoading.loadBitmap(cached));
                return;
            } catch (ImageLoadException e) {
                e.printStackTrace();
            } finally {
                getLoader().getInternalDiskCache().unlockFile(downloadedFileName);
            }
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(downloadedFileName);
        Bitmap img = retriever.getFrameAtTime(0);

        Bitmap res = ImageScaling.scaleFit(img, 800, 800);
        img.recycle();

        String fileName = getLoader().getInternalDiskCache().startWriteFile(downloadedFileName);
        try {
            ImageLoading.save(res, fileName);
            getLoader().getInternalDiskCache().commitFile(downloadedFileName);
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