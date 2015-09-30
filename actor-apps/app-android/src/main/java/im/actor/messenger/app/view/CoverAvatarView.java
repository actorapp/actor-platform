package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 26.12.14.
 */
public class CoverAvatarView extends SimpleDraweeView {

    private FileVM fileVM;
    private FileVM fullFileVM;
    private boolean isLoaded;
    private long currentId;
    private boolean isSmallLoaded;
    private ImageView bkgrnd;

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

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(160)
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
//                .setBackground(getResources().getDrawable(R.drawable.img_profile_avatar_default))
                .setOverlay(new CoverOverlayDrawable(getContext()))
                .build();
        setHierarchy(hierarchy);
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

        if (setCommon(avatar)) return;


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
                    isSmallLoaded = true;
                    if (!isLoaded) {
                        setImageURI(Uri.fromFile(new File(reference.getDescriptor())));
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
                        if (bkgrnd != null && avatar != null && (avatar.getSmallImage() != null)) {
                            String downloadedDescriptor = messenger().findDownloadedDescriptor(avatar.getSmallImage().getFileReference().getFileId());
                            if (downloadedDescriptor != null && !downloadedDescriptor.isEmpty()) {
                                Drawable d = Drawable.createFromPath(downloadedDescriptor);
                                bkgrnd.setImageDrawable(d);
                            }
                        }
                        setImageURI(Uri.fromFile(new File(reference.getDescriptor())));

                    }
                });
            }
        }
    }

    private boolean setCommon(Avatar avatar) {
        if (avatar != null && (avatar.getFullImage() != null || avatar.getSmallImage() != null)) {
            String downloadedDescriptor = messenger().findDownloadedDescriptor(avatar.getFullImage() != null ? avatar.getFullImage().getFileReference().getFileId() : avatar.getSmallImage().getFileReference().getFileId());
            if (downloadedDescriptor != null && !downloadedDescriptor.isEmpty()) {
                Drawable d = Drawable.createFromPath(downloadedDescriptor);
                setScaleType(ScaleType.CENTER_CROP);
                setImageDrawable(d);
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

    public void setBkgrnd(ImageView bkgrnd) {
        this.bkgrnd = bkgrnd;
    }
}
