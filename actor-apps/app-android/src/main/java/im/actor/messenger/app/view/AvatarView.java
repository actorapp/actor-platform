package im.actor.messenger.app.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.PublicGroup;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class AvatarView extends SimpleDraweeView {

    private FileVM bindedFile;
    private int size;
    private float placeholderTextSize;
    private long currentId;

    public AvatarView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public AvatarView(Context context) {
        super(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(int size, float placeholderTextSize) {
        this.size = size;
        this.placeholderTextSize = placeholderTextSize;

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(200)
                .setRoundingParams(new RoundingParams()
                        .setRoundAsCircle(true)
                        .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY))
                        // .setOverlay(new AvatarBorderDrawable())
                .build();
        setHierarchy(hierarchy);
    }

    public void bind(Dialog dialog) {
        bind(dialog.getDialogAvatar(), dialog.getDialogTitle(), dialog.getPeer().getPeerId());
    }

    public void bind(Contact contact) {
        bind(contact.getAvatar(), contact.getName(), contact.getUid());
    }

    public void bind(UserVM user) {
        bind(user.getAvatar().get(), user.getName().get(), user.getId());
    }

    public void bind(GroupVM group) {
        bind(group.getAvatar().get(), group.getName().get(), group.getId());
    }

    public void bind(PublicGroup group) {
        bind(group.getAvatar(), group.getTitle(), group.getId());
    }


    public void bind(Avatar avatar, String title, int id) {
        // Same avatar
        if (avatar != null && avatar.getSmallImage() != null
                && avatar.getSmallImage().getFileReference().getFileId() == currentId) {
            return;
        }

        getHierarchy().setPlaceholderImage(new AvatarPlaceholderDrawable(title, id, placeholderTextSize, getContext()));

        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }

        setImageURI(null);

        if (avatar == null || avatar.getSmallImage() == null) {
            currentId = 0;
            return;
        }
        currentId = avatar.getSmallImage().getFileReference().getFileId();

        bindedFile = messenger().bindFile(avatar.getSmallImage().getFileReference(), true, new FileVMCallback() {
            @Override
            public void onNotDownloaded() {

            }

            @Override
            public void onDownloading(float progress) {

            }

            @Override
            public void onDownloaded(FileSystemReference reference) {

                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(reference.getDescriptor())))
                        .setResizeOptions(new ResizeOptions(size, size))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(getController())
                        .setImageRequest(request)
                        .build();
                setController(controller);
            }
        });
    }

    public void bindRaw(String fileName) {
        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }
        currentId = 0;

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(fileName)))
                .setResizeOptions(new ResizeOptions(size, size))
                .setAutoRotateEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .build();
        setController(controller);
    }

    public void unbind() {
        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }
        currentId = 0;

        setImageURI(null);
    }
}
