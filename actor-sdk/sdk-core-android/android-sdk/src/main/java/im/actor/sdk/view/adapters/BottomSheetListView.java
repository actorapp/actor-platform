package im.actor.sdk.view.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

public class BottomSheetListView extends RecyclerListView {

    private View header;
    private View underlyingView;
    private int minHeight = 0;

    public BottomSheetListView(Context context) {
        super(context);
        init();
    }

    public BottomSheetListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomSheetListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BottomSheetListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    @Override
    public int getCount() {
        return super.getCount() - 1;
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        setVerticalScrollBarEnabled(false);
        header = new View(getContext());
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        addHeaderView(header);

        setOnTouchListener(new View.OnTouchListener() {
            boolean delegateTouch = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean stopDelegate = false;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        delegateTouch = isWithinHeaderBounds(motionEvent.getRawX(), motionEvent.getRawY());
                        break;

                    case MotionEvent.ACTION_UP:
                        stopDelegate = true;
                }

                if (delegateTouch && underlyingView != null) {
                    delegateTouch = !stopDelegate;
                    underlyingView.dispatchTouchEvent(motionEvent);
                    return true;
                }


                return false;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resizeHeader();
    }

    protected void resizeHeader() {
        if (header.getLayoutParams().height != getHeight() - minHeight) {
            header.getLayoutParams().height = getHeight() - minHeight;
            header.requestLayout();
        }
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        resizeHeader();
    }

    public View getHeader() {
        return header;
    }

    boolean isWithinHeaderBounds(float xPoint, float yPoint) {
        int[] l = new int[2];
        header.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = header.getWidth();
        int h = header.getHeight();

        if (xPoint < x || xPoint > x + w || yPoint < y || yPoint > y + h) {
            return false;
        }
        return true;
    }

    public void setUnderlyingView(View underlyingView) {
        this.underlyingView = underlyingView;
    }


}
