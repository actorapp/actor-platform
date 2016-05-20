package im.actor.sdk.view;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import im.actor.sdk.ActorSDK;

public class CustomClicableSpan extends ClickableSpan {

    protected boolean hideUrlStyle;
    private SpanClickListener clickListener;

    public CustomClicableSpan(SpanClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if (hideUrlStyle) {
            ds.setColor(Color.BLACK);
        }
        ds.setColor(ActorSDK.sharedActor().style.getMainColor());
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick();
    }

    public interface SpanClickListener {
        void onClick();
    }
}