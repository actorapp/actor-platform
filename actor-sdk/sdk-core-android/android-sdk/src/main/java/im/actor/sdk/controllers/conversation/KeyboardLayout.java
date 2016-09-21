package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import im.actor.sdk.R;

public class KeyboardLayout extends FrameLayout {
    boolean showInternal = false;
    boolean sync = false;
    private RelativeLayout container;
    private int keyboardHeight;

    public KeyboardLayout(Context context) {
        super(context);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (container != null) {
            if (!showInternal) {
                if (container.getPaddingBottom() != 0) {
                    container.setPadding(0, 0, 0, 0);
                }
                sync = showInternal;
            }
        }

        super.onLayout(changed, left, top, right, bottom);

        if (container != null) {
            if (showInternal) {
                if (container.getPaddingBottom() != keyboardHeight) {
                    container.setPadding(0, 0, 0, keyboardHeight);
                }
                sync = showInternal;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (container == null) {
            container = (RelativeLayout) findViewById(R.id.container);
        }
    }

    public void showInternal(int keyboardHeight) {
        showInternal = true;
        this.keyboardHeight = keyboardHeight;
        sync = false;
    }

    public void dismissInternal() {
        showInternal = false;
        sync = true;
    }

    public boolean isSync() {
        return sync == showInternal;
    }

}
