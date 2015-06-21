package im.actor.images.loading.actors;

import android.graphics.Bitmap;
import com.droidkit.actors.tasks.AskCallback;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.actors.base.BasicTaskActor;
import im.actor.images.loading.tasks.RawFileTask;

/**
 * Created by ex3ndr on 04.09.14.
 */
public class RawFileActor extends BasicTaskActor<RawFileTask> {

    public RawFileActor(RawFileTask task, ImageLoader loader) {
        super(task, loader);
    }

    @Override
    public void startTask() {
        ask(BitmapDecoderActor.decode(getTask().getFileName(), getLoader()), new AskCallback<Bitmap>() {
            @Override
            public void onResult(Bitmap result) {
                completeTask(result);
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
