package im.actor.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;

public class DividerView extends View {

    public DividerView(Context context) {
        super(context);
        initStyles();
    }

    public DividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyles();
    }

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyles();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DividerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initStyles();
    }

    private void initStyles() {
        setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getResources().getDimensionPixelSize(R.dimen.div_size));
    }
}
