package im.actor.sdk.view.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

public class BottomSheetListView extends RecyclerListView {

    private FrameLayout header;
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
        setSelector(new ColorDrawable(Color.TRANSPARENT));
        setOverScrollMode(OVER_SCROLL_NEVER);
        setVerticalScrollBarEnabled(false);
        header = new FrameLayout(getContext());
//        header.setBackgroundColor(ActorSDK.sharedActor().style.getAccentColor());
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        ImageView shadow = new ImageView(getContext());
        shadow.setScaleType(ImageView.ScaleType.FIT_XY);
        shadow.setImageResource(R.drawable.conv_field_shadow);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Screen.dp(2), Gravity.BOTTOM);
        header.addView(shadow, params);
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
        setVisibility(minHeight == 0 ? GONE : VISIBLE);
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

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        super.setOnItemClickListener((adapterView, view, i, l) -> listener.onItemClick(adapterView, view, i - 1, l));
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        super.setOnItemLongClickListener((adapterView, view, i, l) -> listener.onItemLongClick(adapterView, view, i - 1, l));
    }
}
