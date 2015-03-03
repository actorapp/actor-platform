package im.actor.messenger.app.images;

import android.graphics.Bitmap;

import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;

/**
 * Created by ex3ndr on 29.10.14.
 */
public class FullAvatarActor extends BasicTaskActor<FullAvatarTask> {

    public FullAvatarActor(FullAvatarTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        final String sourceFile = FileKeys.avatarKey(getTask().getAvatarImage().getFileLocation().getFileId());
        String fileName = getLoader().getInternalDiskCache().lockFile(sourceFile);
        if (fileName != null) {
            try {
                Bitmap res = ImageLoading.loadBitmap(fileName);
                completeTask(res);
                return;
            } catch (ImageLoadException e) {
                e.printStackTrace();
                // error(e);
            } finally {
                getLoader().getInternalDiskCache().unlockFile(sourceFile);
            }
        }

//        ask(requests().getFile(new FileLocation(getTask().getAvatarImage().getFileLocation().getFileId(),
//                getTask().getAvatarImage().getFileLocation().getAccessHash()), 0,512 * 1024),new FutureCallback<ResponseGetFile>() {
//            @Override
//            public void onResult(ResponseGetFile result) {
//                String fileName = getLoader().getInternalDiskCache().startWriteFile(sourceFile);
//                try {
//                    FileOutputStream outputStream = new FileOutputStream(fileName);
//                    outputStream.write(result.getPayload());
//                    outputStream.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                getLoader().getInternalDiskCache().commitFile(sourceFile);
//
//                try {
//                    Bitmap res = ImageLoading.loadBitmap(result.getPayload());
//                    completeTask(res);
//                } catch (ImageLoadException e) {
//                    e.printStackTrace();
//                    error(e);
//                }
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                error(throwable);
//            }
//        });
    }

    @Override
    public void onTaskObsolete() {

    }
}
