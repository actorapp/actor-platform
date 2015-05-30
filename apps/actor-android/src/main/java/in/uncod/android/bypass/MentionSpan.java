package in.uncod.android.bypass;

import android.content.Context;
import android.view.View;

public class MentionSpan extends BaseUrlSpan {

    public MentionSpan(String url, boolean hideUrlStyle) {
        super(url, hideUrlStyle);
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