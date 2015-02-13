package im.actor.messenger.core.images;

import android.graphics.Bitmap;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;

import im.actor.api.scheme.rpc.ResponseGetFile;
import im.actor.messenger.storage.scheme.FileLocation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class AvatarActor extends BasicTaskActor<AvatarTask> {

    public AvatarActor(AvatarTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        FileLocation location = getTask().getAvatar().getSmallImage().getFileLocation();
        final String smallFile = "avatar:" + location.getFileId();
        String fileName = getLoader().getInternalDiskCache().lockFile(smallFile);
        if (fileName != null) {
            try {
                Bitmap res = ImageLoading.loadBitmap(fileName);
                completeTask(res);
                return;
            } catch (ImageLoadException e) {
                e.printStackTrace();
                // error(e);
            } finally {
                getLoader().getInternalDiskCache().unlockFile(smallFile);
            }
        }

        ask(requests().getFile(new im.actor.api.scheme.FileLocation(location.getFileId(),
                location.getAccessHash()), 0, 512 * 1024), new FutureCallback<ResponseGetFile>() {
            @Override
            public void onResult(ResponseGetFile result) {
                String fileName = getLoader().getInternalDiskCache().startWriteFile(smallFile);
                try {
                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    outputStream.write(result.getPayload());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getLoader().getInternalDiskCache().commitFile(smallFile);

                try {
                    Bitmap res = ImageLoading.loadBitmap(result.getPayload());
                    completeTask(res);
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                    error(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                error(throwable);
            }
        });
    }

    @Override
    public void onTaskObsolete() {

    }
}
