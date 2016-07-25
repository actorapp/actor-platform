package im.actor.sdk.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

public class BlockView extends LinearLayout {

    private Drawable topDrawable;
    private Drawable bottomDrawable;

    public BlockView(Context context) {
        super(context);
        init();
    }

    public BlockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BlockView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setPadding(0, Screen.dp(8), 0, Screen.dp(8));
        setWillNotDraw(false);

        bottomDrawable = getResources().getDrawable(R.drawable.card_shadow_bottom);
        topDrawable = getResources().getDrawable(R.drawable.card_shadow_top);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        bottomDrawable.setBounds(0, getHeight() - Screen.dp(8), getWidth(), getHeight() - Screen.dp(4));
        bottomDrawable.draw(canvas);

        topDrawable.setBounds(0, Screen.dp(7), getWidth(), Screen.dp(8));
        topDrawable.draw(canvas);

        super.onDraw(canvas);
    }
}
