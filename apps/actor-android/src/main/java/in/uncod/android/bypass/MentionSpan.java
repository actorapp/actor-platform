package in.uncod.android.bypass;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

public class MentionSpan extends URLSpan {

    public MentionSpan(String url) {
        super(url);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setColor(Color.BLACK);
    }

    @Override
    public void onClick(View widget) {
        //Do nothing
    }
}