package im.actor.messenger.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import im.actor.images.cache.DiskCache;
import im.actor.images.common.ImageLoadException;
import im.actor.images.loading.view.ImageReceiverView;
import im.actor.images.ops.ImageLoading;

import im.actor.messenger.R;
import im.actor.messenger.app.Core;
import im.actor.messenger.app.images.FileKeys;
import im.actor.messenger.app.images.FullAvatarTask;
import im.actor.messenger.app.util.Screen;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.FileReference;

/**
 * Created by ex3ndr on 26.12.14.
 */
public class CoverAvatarView extends ImageReceiverView {

    private Paint BITMAP_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG);

    private Bitmap background;
    // private Palette palette;

    private Bitmap bitmap;
    private Rect rect1 = new Rect();
    private Rect rect2 = new Rect();

    // private ValueModel<Integer> tintColor;

    private Drawable bottomShadow;

    private int offset;

    public CoverAvatarView(Context context) {
        super(context);
        init();
    }

    public CoverAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //tintColor = new ValueModel<>("??", getResources().getColor(R.color.primary));
        bottomShadow = getResources().getDrawable(R.drawable.profile_avatar_bottom_shadow);
        background = ((BitmapDrawable) getResources().getDrawable(R.drawable.img_profile_avatar_default)).getBitmap();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        invalidate();
    }

    public void request(Avatar avatar) {
        clear();

        {
            FileReference fileReference = avatar.getLargeImage().getFileReference();
            DiskCache diskCache = Core.getImageLoader().getInternalDiskCache();
            String avatarKey = FileKeys.avatarKey(fileReference.getFileId());
            String file = diskCache.lockFile(avatarKey);
            if (file != null) {
                try {
                    Bitmap bitmap = ImageLoading.loadBitmapOptimized(file);
                    bindResult(bitmap);
                    return;
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                }
            }
        }
        {
            FileReference fileReference = avatar.getSmallImage().getFileReference();
            DiskCache diskCache = Core.getImageLoader().getInternalDiskCache();
            String avatarKey = FileKeys.avatarKey(fileReference.getFileId());
            String file = diskCache.lockFile(avatarKey);
            if (file != null) {
                try {
                    Bitmap bitmap = ImageLoading.loadBitmapOptimized(file);
                    bindResult(bitmap);
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                }
            }
        }

        request(new FullAvatarTask(avatar));
    }

    private void bindResult(Bitmap bitmap) {
        this.bitmap = bitmap;
        // this.palette = Palette.generate(bitmap);
        // this.tintColor.change(palette.getVibrantSwatch().getRgb());
        postInvalidate();
    }

    @Override
    protected void onImageLoadedImpl(Bitmap bitmap) {
        bindResult(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int imgW;
        int imgH;

        if (bitmap != null) {
            imgW = bitmap.getWidth();
            imgH = bitmap.getHeight();
        } else {
            imgW = background.getWidth();
            imgH = background.getHeight();
        }

        int topPadding = Math.abs(offset);
        int height = getHeight() - topPadding;

        float scale = Math.min((float) imgW / (float) getWidth(), (float) imgH / (float) height);
        int realW = (int) (getWidth() * scale);
        int realH = (int) (height * scale);
        int paddingW = (imgW - realW) / 2;
        int paddingH = (imgH - realH) / 2;

        if (bitmap != null) {
            rect1.set(paddingW, paddingH, paddingW + realW, paddingH + realH);
            rect2.set(0, topPadding, getWidth(), getHeight());
            canvas.drawBitmap(bitmap, rect1, rect2, BITMAP_PAINT);

            bottomShadow.setBounds(0, getHeight() - Screen.dp(64), getWidth(), getHeight());
            bottomShadow.draw(canvas);
        } else {
            rect1.set(paddingW, paddingH, paddingW + realW, paddingH + realH);
            rect2.set(0, topPadding, getWidth(), getHeight());
            canvas.drawBitmap(background, rect1, rect2, BITMAP_PAINT);
        }
    }
}
