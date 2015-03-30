package im.actor.messenger.app;

import android.app.Application;
import android.content.Intent;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.splunk.mint.Mint;

import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.ImageLoaderProvider;
import im.actor.messenger.app.service.KeepAliveService;

public class ActorApplication extends Application implements ImageLoaderProvider {

    @Override
    public void onCreate() {
        super.onCreate();
        Mint.initAndStartSession(this, "4345135a");
        Fresco.initialize(this);
        Core.init(this);
        startService(new Intent(this, KeepAliveService.class));
    }

    @Override
    public ImageLoader getImageLoader() {
        return Core.core().getImageLoader();
    }
}