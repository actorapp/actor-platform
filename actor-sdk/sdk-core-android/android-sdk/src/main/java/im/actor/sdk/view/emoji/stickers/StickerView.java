package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.FileReference;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;


public class StickerView extends SimpleDraweeView {

    private GenericDraweeHierarchyBuilder builder;
    private boolean loaded = false;
    FileReference fileReference;
    FileVM bindedFile;

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
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .build();
        setHierarchy(hierarchy);
    }

    public void bind(FileReference fileReference, final int size) {

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
            @Override
            public void onNotDownloaded() {

            }

            @Override
            public void onDownloading(float progress) {

            }

            @Override
            public void onDownloaded(FileSystemReference reference) {

                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(reference.getDescriptor())))
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
    }
}