package im.actor.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import im.actor.runtime.function.CancellableSimple;
import im.actor.sdk.util.Screen;

public class AsyncView<T, L> extends View {

    private ThreadLocal<InvalidationContext> contexts = new ThreadLocal<>();
    private ViewAsyncDispatch asyncDispatch = new ViewAsyncDispatch();
    private int currentWidth;
    private int currentHeight;
    private boolean isMeasured;
    private boolean isRequested;
    private T currentObject;
    private InvalidationContext currentLayoutingContext;
    private L currentLayout;

    public AsyncView(Context context) {
        super(context);
    }

    public AsyncView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AsyncView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AsyncView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!isMeasured || currentWidth != getWidth() || currentHeight != getHeight()) {
            isMeasured = true;
            currentWidth = getWidth();
            currentHeight = getHeight();
            if (currentObject != null) {
                startLayout();
            }
        }
    }

    public void requestLayout(T arg) {
        requestLayout(arg, true);
    }

    public void requestLayout(T arg, boolean clearOld) {
        if (clearOld) {
            this.currentLayout = null;
        }
        this.currentObject = arg;

        if (currentLayoutingContext != null) {
            currentLayoutingContext.cancel();
            currentLayoutingContext = null;
        }

        invalidate();

        if (isMeasured) {
            startLayout();
        }
    }

    public void cancelLayout() {
        this.currentObject = null;
        if (currentLayoutingContext != null) {
            currentLayoutingContext.cancel();
            currentLayoutingContext = null;
        }
    }

    private void startLayout() {
        T arg = this.currentObject;
        isRequested = true;
        int w = currentWidth;
        int h = currentHeight;
        if (currentLayoutingContext != null) {
            currentLayoutingContext.cancel();
            currentLayoutingContext = null;
        }

        currentLayoutingContext = new InvalidationContext(arg);
        InvalidationContext context = currentLayoutingContext;
        contexts.set(context);
        asyncDispatch.dispatch(currentLayoutingContext, () -> {
            contexts.set(currentLayoutingContext);
            contexts.set(context);
            L res = buildLayout(arg, w, h);
            contexts.set(null);
            asyncDispatch.complete(() -> {
                currentLayout = res;
                layoutReady(res);
                invalidate();
            });
        });
    }

    public InvalidationContext getCurrentLayoutContext() {
        return contexts.get();
    }

    public L getLayout() {
        return currentLayout;
    }

    public L buildLayout(T arg, int width, int height) {
        return null;
    }

    public void layoutReady(L res) {

    }

    public TextPaint createTextPaint(Typeface typeface, int size, int color) {
        TextPaint res = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        res.setSubpixelText(true);
        res.setTypeface(typeface);
        res.setTextSize(Screen.sp(size));
        res.setColor(color);
        return res;
    }

    public Paint createFilledPaint(int color) {
        Paint res = new Paint(Paint.ANTI_ALIAS_FLAG);
        res.setColor(color);
        res.setStyle(Paint.Style.FILL);
        return res;
    }

    public StaticLayout singleLineText(CharSequence sequence, TextPaint paint, int maxWidth) {
        CharSequence ellipsizedTitle = TextUtils.ellipsize(sequence, paint, maxWidth - Screen.dp(2),
                TextUtils.TruncateAt.END);
        return new StaticLayout(ellipsizedTitle, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL,
                1.0f, 0.0f, false);
    }

    public class InvalidationContext extends CancellableSimple {

        private T arg;
        private boolean isInvalidated = false;

        private InvalidationContext(T arg) {
            this.arg = arg;
        }

        public void invalidate() {
            if (isCancelled()) {
                return;
            }
            if (isInvalidated) {
                return;
            }
            isInvalidated = true;
            post(() -> {
                if (currentLayoutingContext == this) {
                    requestLayout(arg, false);
                }
            });
        }
    }
}
