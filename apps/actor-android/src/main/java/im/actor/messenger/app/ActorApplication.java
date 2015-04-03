package im.actor.messenger.app;

import android.app.Application;

import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.ImageLoaderProvider;

public class ActorApplication extends Application implements ImageLoaderProvider {

    @Override
    public void onCreate() {
        super.onCreate();
        Core.init(this);
    }

    @Override
    public ImageLoader getImageLoader() {
        return Core.core().getImageLoader();
    }
}