package in.uncod.android.bypass;

import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

public class MentionSpan extends BaseUrlSpan {

    public MentionSpan(String url, Context ctx, boolean hideUrlStyle) {
        super(url, ctx, hideUrlStyle);
    }


    @Override
    public void onClick(View widget) {
        if(hideUrlStyle){
            //Do nothing
        }else{
            super.onClick(widget);
        }
    }
}