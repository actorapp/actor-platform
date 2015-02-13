package com.droidkit.images.loading.actors;

import android.graphics.Bitmap;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.dispatch.RunnableDispatcher;
import com.droidkit.images.cache.MemoryCache;
import com.droidkit.images.common.ImageMetadata;
import com.droidkit.images.common.ReuseResult;
import com.droidkit.images.loading.ImageLoader;
import com.droidkit.images.loading.actors.base.WorkerActor;
import com.droidkit.images.loading.log.Log;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.sources.FileSource;
import com.droidkit.images.util.HashUtil;

/**
 * Created by ex3ndr on 27.08.14.
 */
public class BitmapDecoderActor extends WorkerActor<Bitmap> {

    public static ActorSelection decode(final String fileName, final ImageLoader loader) {
        return new ActorSelection(Props.create(BitmapDecoderActor.class, new ActorCreator<BitmapDecoderActor>() {
            @Override
            public BitmapDecoderActor create() {
                return new BitmapDecoderActor(fileName, loader);
            }
        }), "dec_" + HashUtil.md5(fileName));
    }

    private static RunnableDispatcher dispatcher = new RunnableDispatcher("bitmaps", Runtime.getRuntime().availableProcessors());

    private String fileName;
    private MemoryCache memoryCache;

    public BitmapDecoderActor(String fileName, ImageLoader loader) {
        super(dispatcher);
        this.fileName = fileName;
        this.memoryCache = loader.getMemoryCache();
    }

    @Override
    protected Bitmap doWork() throws Exception {
        long start = System.currentTimeMillis();
        FileSource fileSource = new FileSource(fileName);
        ImageMetadata metadata = fileSource.getImageMetadata();
        Bitmap reuse = memoryCache.findExactSize(metadata.getW(), metadata.getH());
        if (reuse == null) {
            return ImageLoading.loadBitmapOptimized(fileName);
        }
        ReuseResult result = ImageLoading.loadReuseExact(fileName, reuse);
        if (!result.isReused()) {
            memoryCache.putFree(result.getRes());
        }
        Log.d("Image loaded in " + (System.currentTimeMillis() - start) + " ms");
        return result.getRes();
    }
}
