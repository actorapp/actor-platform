package im.actor.sdk.controllers.conversation.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.view.BackgroundPreviewView;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ChatBackgroundView extends View {

    private Drawable background;
    SharedPreferences shp;

    public ChatBackgroundView(Context context) {
        super(context);
        bind();
    }

    public ChatBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bind();
    }

    public ChatBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bind();
    }

    public void release() {
        background = null;
    }

    public void bind() {
        if (background == null) {
            shp = getContext().getSharedPreferences("wallpaper", Context.MODE_PRIVATE);
            if (shp.getInt("wallpaper", 0) == ActorSDK.sharedActor().style.getDefaultBackgrouds().length) {
                background = Drawable.createFromPath(BaseActorSettingsFragment.getWallpaperFile());
            } else {
                background = getResources().getDrawable(BackgroundPreviewView.getBackground(BackgroundPreviewView.getBackgroundIdByUri(messenger().getSelectedWallpaper(), getContext(), shp.getInt("wallpaper", 0))));
            }
        }
    }

    public void bind(int i) {
        background = getResources().getDrawable(BackgroundPreviewView.getBackground(i));
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (background != null) {
            int w = background.getIntrinsicWidth();
            int h = background.getIntrinsicHeight();

            int screenW = getResources().getDisplayMetrics().widthPixels;
            int screenH = getResources().getDisplayMetrics().heightPixels;

            float scale = Math.min((float) w / (float) screenW, (float) h / (float) screenH);
            int realW = (int) (screenW * scale);
            int realH = (int) (screenH * scale);
            int paddingW = (w - realW) / 2;
            int paddingH = (h - realH) / 2;

            background.setBounds(
                    (int) (-paddingW / scale),
                    (int) (-paddingH / scale),
                    (int) (screenW + paddingW / scale),
                    (int) (screenH + paddingH / scale));
            background.draw(canvas);
        }
    }
}
