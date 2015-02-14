package im.actor.messenger.core.images;

import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;

import im.actor.messenger.util.Logger;
import im.actor.model.entity.content.FileSource;
import im.actor.model.entity.content.PhotoContent;

/**
 * Created by ex3ndr on 05.09.14.
 */
public class ImagePreviewActor extends BasicTaskActor<ImagePreviewTask> {

    private static final String TAG = "ImageActor";

    private FileSource fileLocation;

    public ImagePreviewActor(ImagePreviewTask task, ImageLoader loader) {
        super(task, loader);
        Logger.d(TAG, "Actor creating");
        fileLocation = ((PhotoContent) task.getMessage().getContent()).getSource();
        Logger.d(TAG, "Actor creating end");
    }

    @Override
    public void startTask() {
        Logger.d(TAG, getPath() + "|Starting task");
//        ask(downloader().downloadedFileName(fileLocation), new FutureCallback<String>() {
//            @Override
//            public void onResult(String fileName) {
//                Logger.d(TAG, getPath() + "|Loaded file name " + fileName);
//                loadFile(fileName);
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                Logger.d(TAG, getPath() + "|Not downloaded");
//                error(new RuntimeException(throwable));
//            }
//        });
    }

//    private void loadFile(String fileName) {
//        try {
//            completeTask(ImageLoading.loadBitmapOptimized(fileName));
//            Logger.d(TAG, getPath() + "|Completed");
//        } catch (ImageLoadException e) {
//            Logger.d(TAG, "Loading from file failed #" + fileLocation.getFileId());
//            e.printStackTrace();
//            error(e);
//        }
//    }

    @Override
    public void onTaskObsolete() {
        Logger.d(TAG, getPath() + "|Task obsolete");
    }
}
