package im.actor.messenger.core.images;

import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.BasicTaskActor;
import com.droidkit.images.ops.ImageLoading;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class FastImageActor extends BasicTaskActor<FastImageTask> {
    public FastImageActor(FastImageTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        try {
            completeTask(ImageLoading.loadBitmap(getTask().getData()));
        } catch (ImageLoadException e) {
            e.printStackTrace();
            error(e);
        }
    }

    @Override
    public void onTaskObsolete() {

    }
}
