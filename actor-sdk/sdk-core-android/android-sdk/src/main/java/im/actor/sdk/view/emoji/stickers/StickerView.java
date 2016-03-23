package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.facebook.common.internal.Files;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Sticker;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.runtime.files.FileSystemReference;
import im.actor.sdk.controllers.conversation.view.FastThumbLoader;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;


public class StickerView extends SimpleDraweeView {

    private GenericDraweeHierarchyBuilder builder;
    private boolean loaded = false;
    FileReference fileReference;
    FileVM bindedFile;
    private File imageFile;
    Sticker sticker;
    private FastThumbLoader fastThumbLoader;
    public static final int STICKER_FULL = 512;
    public static final int STICKER_BIG = 256;
    public static final int STICKER_SMALL = 128;

    public StickerView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public StickerView(Context context) {
        super(context);
        init();
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        builder = new GenericDraweeHierarchyBuilder(getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(200)
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .build();
        setHierarchy(hierarchy);

        fastThumbLoader = new FastThumbLoader(this);

    }

    public void bind(FileReference fileReference, int size) {

        if (this.fileReference != null && this.fileReference.equals(fileReference)) {
            return;
        }

        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }

        setImageURI(null);

        this.fileReference = fileReference;

        bindedFile = messenger().bindFile(fileReference, true, new FileVMCallback() {

            private boolean isFastThumbLoaded = false;

            private void checkFastThumb() {
                if (!isFastThumbLoaded) {
                    isFastThumbLoaded = true;
//                    if (sticker.getThumb() != null) {
//                        fastThumbLoader.request(sticker.getThumb());
//                    }
                }
            }

            @Override
            public void onNotDownloaded() {
                checkFastThumb();
            }

            @Override
            public void onDownloading(float progress) {
                checkFastThumb();
            }

            @Override
            public void onDownloaded(FileSystemReference reference) {
                imageFile = new File(reference.getDescriptor());
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(imageFile))
                        .setAutoRotateEnabled(true)
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(getController())
                        .setImageRequest(request)
                        .build();
                setController(controller);
                loaded = true;


            }
        });
    }

    public byte[] getThumb() {
        try {
            return Files.toByteArray(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public void shortenFade() {
        builder.setFadeDuration(0);
        setHierarchy(builder.build());
    }

    public GenericDraweeHierarchyBuilder getBuilder() {
        return builder;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public FileReference getFileReference() {
        return fileReference;
    }

    public void unbind() {
        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }

        setImageURI(null);
        fastThumbLoader.cancel();
    }
}