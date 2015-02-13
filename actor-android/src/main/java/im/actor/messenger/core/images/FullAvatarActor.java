package im.actor.messenger.core.images;

import android.graphics.Bitmap;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;

import org.apache.http.conn.scheme.Scheme;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import im.actor.api.scheme.FileLocation;
import im.actor.api.scheme.rpc.ResponseGetFile;

import static im.actor.messenger.core.Core.requests;

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

        ask(requests().getFile(new FileLocation(getTask().getAvatarImage().getFileLocation().getFileId(),
                getTask().getAvatarImage().getFileLocation().getAccessHash()), 0,512 * 1024),new FutureCallback<ResponseGetFile>() {
            @Override
            public void onResult(ResponseGetFile result) {
                String fileName = getLoader().getInternalDiskCache().startWriteFile(sourceFile);
                try {
                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    outputStream.write(result.getPayload());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getLoader().getInternalDiskCache().commitFile(sourceFile);

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
