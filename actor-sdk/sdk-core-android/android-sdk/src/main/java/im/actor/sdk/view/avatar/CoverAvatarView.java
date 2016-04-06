package im.actor.sdk.view.avatar;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.runtime.files.FileSystemReference;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.view.avatar.CoverOverlayDrawable;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class CoverAvatarView extends SimpleDraweeView {

    private FileVM fileVM;
    private FileVM fullFileVM;
    private String smallDescriptor;
    private boolean isLoaded;
    private long currentId;

    public CoverAvatarView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public CoverAvatarView(Context context) {
        super(context);
        init();
    }

    public CoverAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverAvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        if (isInEditMode()) {
            return;
        }

        GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(getResources());

        builder.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        builder.setOverlay(new CoverOverlayDrawable(getContext()));

        if (ActorSDK.sharedActor().style.getAvatarBackgroundResourse() != 0) {
            builder.setPlaceholderImage(getResources()
                    .getDrawable(ActorSDK.sharedActor().style.getAvatarBackgroundResourse()));
        } else {
            builder.setPlaceholderImage(new ColorDrawable(
                    ActorSDK.sharedActor().style.getAvatarBackgroundColor()));
        }

        builder.setFadeDuration(0);

        setHierarchy(builder.build());
    }

    public void bind(final Avatar avatar) {
        // Same avatar
        if (avatar != null && avatar.getSmallImage() != null
                && avatar.getSmallImage().getFileReference().getFileId() == currentId) {
            return;
        }

        if (fileVM != null) {
            fileVM.detach();
            fileVM = null;
        }
        if (fullFileVM != null) {
            fullFileVM.detach();
            fullFileVM = null;
        }
        isLoaded = false;
        smallDescriptor = null;

        if (tryToSetFast(avatar)) {
            return;
        }

        if (avatar != null && avatar.getSmallImage() != null) {
            currentId = avatar.getSmallImage().getFileReference().getFileId();

            fileVM = messenger().bindFile(avatar.getSmallImage().getFileReference(), true, new FileVMCallback() {
                @Override
                public void onNotDownloaded() {
                }

                @Override
                public void onDownloading(float progress) {
                }

                @Override
                public void onDownloaded(FileSystemReference reference) {
                    if (!isLoaded) {
                        smallDescriptor = reference.getDescriptor();
                        setImageURI(Uri.fromFile(new File(smallDescriptor)));
                    }
                }
            });
            if (avatar.getFullImage() != null) {
                fullFileVM = messenger().bindFile(avatar.getFullImage().getFileReference(), true, new FileVMCallback() {
                    @Override
                    public void onNotDownloaded() {
                    }

                    @Override
                    public void onDownloading(float progress) {
                    }

                    @Override
                    public void onDownloaded(FileSystemReference reference) {
                        isLoaded = true;

                        PipelineDraweeControllerBuilder dController = Fresco.newDraweeControllerBuilder();
                        if (smallDescriptor != null) {
                            dController.setLowResImageRequest(ImageRequest.fromUri(Uri.fromFile(new File(smallDescriptor))));
                        }
                        dController.setOldController(getController());
                        dController.setImageRequest(ImageRequest.fromUri(Uri.fromFile(new File(reference.getDescriptor()))));

                        setController(dController.build());
                    }
                });
            }
        }
    }

    private boolean tryToSetFast(Avatar avatar) {
        if (avatar != null && avatar.getFullImage() != null) {
            String downloadedDescriptor = messenger().findDownloadedDescriptor(avatar.getFullImage().getFileReference().getFileId());
            if (downloadedDescriptor != null) {
                setImageURI(Uri.fromFile(new File(downloadedDescriptor)));
                return true;
            }
        }
        return false;
    }

    public void setOffset(int offset) {
        setScrollY(-offset / 2);
    }

    public void unbind() {
        if (fileVM != null) {
            fileVM.detach();
            fileVM = null;
        }
        if (fullFileVM != null) {
            fullFileVM.detach();
            fullFileVM = null;
        }
        currentId = 0;
    }
}
