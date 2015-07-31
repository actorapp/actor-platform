package im.actor.messenger.app.fragment.chat.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.text.BidiFormatter;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class BubbleTextContainer extends FrameLayout {
    public BubbleTextContainer(Context context) {
        super(context);
        setClipToPadding(false);
    }

    public BubbleTextContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipToPadding(false);
    }

    public BubbleTextContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setClipToPadding(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Rect bounds = new Rect();
        Drawable background = getBackground();
        if (background != null) {
            background.getPadding(bounds);
        }

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int maxW = MeasureSpec.getSize(widthMeasureSpec) - bounds.left - bounds.right;

        TextView messageView = (TextView) getChildAt(0);
        messageView.measure(MeasureSpec.makeMeasureSpec(maxW, wMode), heightMeasureSpec);
        View timeView = getChildAt(1);
        timeView.measure(MeasureSpec.makeMeasureSpec(maxW, wMode), heightMeasureSpec);

        Layout textLayout = messageView.getLayout();

        int contentW = messageView.getMeasuredWidth();
        int timeW = timeView.getMeasuredWidth();
        boolean isRtl = BidiFormatter.getInstance().isRtl(messageView.getText().toString());

        if (messageView.getLayout().getLineCount() < 5 && !isRtl) {
            contentW = 0;
            for (int i = 0; i < textLayout.getLineCount(); i++) {
                contentW = Math.max(contentW, (int) textLayout.getLineWidth(i));
            }
        }

        int lastLineW = (int) textLayout.getLineWidth(textLayout.getLineCount() - 1);

        if (isRtl) {
            lastLineW = contentW;
        }

        int fullContentW, fullContentH;

        if (isRtl) {
            fullContentW = contentW;
            fullContentH = messageView.getMeasuredHeight() + timeView.getMeasuredHeight();
        } else {
            if (lastLineW + timeW < contentW) {
                // Nothing to do
                fullContentW = contentW;
                fullContentH = messageView.getMeasuredHeight();
            } else if (lastLineW + timeW < maxW) {
                fullContentW = lastLineW + timeW;
                fullContentH = messageView.getMeasuredHeight();
            } else {
                fullContentW = contentW;
                fullContentH = messageView.getMeasuredHeight() + timeView.getMeasuredHeight();
            }
        }

        setMeasuredDimension(fullContentW + bounds.left + bounds.right, fullContentH + bounds.top + bounds.bottom);
    }
}
