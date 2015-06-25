package im.actor.images.loading.actors;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import im.actor.images.common.ImageLoadException;
import im.actor.images.common.ReuseResult;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.actors.base.BasicTaskActor;
import im.actor.images.loading.tasks.PreviewContentTask;
import im.actor.images.ops.ImageLoading;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class PreviewContentActor extends BasicTaskActor<PreviewContentTask> {

    public PreviewContentActor(PreviewContentTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        int w;
        int h;
        if (getTask().getKind() == MediaStore.Images.Thumbnails.MINI_KIND) {
            w = 512;
            h = 374;
        } else {
            w = 96;
            h = 96;
        }

        Bitmap bitmap = getLoader().getMemoryCache().findExactSize(w, h);
        if (bitmap != null) {
            try {
                ReuseResult result = ImageLoading.loadReuseExact(getTask().getUri(), getLoader().getContext(), bitmap);
                if (!result.isReused()) {
                    getLoader().getMemoryCache().putFree(bitmap);
                }
                completeTask(result.getRes());
            } catch (ImageLoadException e) {
                e.printStackTrace();
                getLoader().getMemoryCache().putFree(bitmap);
                error(e);
            }
        } else {
            try {
                Bitmap res = ImageLoading.loadBitmap(getTask().getUri(), getLoader().getContext());
                completeTask(res);
            } catch (ImageLoadException e) {
                e.printStackTrace();
                error(e);
            }
        }
    }

    @Override
    public void onTaskObsolete() {

    }
}
