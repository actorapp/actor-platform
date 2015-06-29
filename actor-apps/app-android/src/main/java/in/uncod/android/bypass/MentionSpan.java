package in.uncod.android.bypass;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.view.View;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.Intents;

public class MentionSpan extends BaseUrlSpan {
    int[] colors;

    public MentionSpan(String url, boolean hideUrlStyle) {
        super(url, hideUrlStyle);
        colors = new int[]{
                AppContext.getContext().getResources().getColor(R.color.placeholder_0),
                AppContext.getContext().getResources().getColor(R.color.placeholder_1),
                AppContext.getContext().getResources().getColor(R.color.placeholder_2),
                AppContext.getContext().getResources().getColor(R.color.placeholder_3),
                AppContext.getContext().getResources().getColor(R.color.placeholder_4),
                AppContext.getContext().getResources().getColor(R.color.placeholder_5),
                AppContext.getContext().getResources().getColor(R.color.placeholder_6),
        };

    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if(hideUrlStyle){
            ds.setUnderlineText(false);
            ds.setColor(Color.BLACK);
        }
        if(getURL().startsWith("people://")){
            int userId = Integer.parseInt(getURL().replace("people://", ""));
            ds.setColor(colors[Math.abs(userId) % colors.length]);
        }
    }

    private String url;

    public void setUrl(String s){
        this.url = s;
    }

    @Override
    public void onClick(View widget) {
        if(hideUrlStyle){
            //Do nothing
        }else{
//            super.onClick(widget);
            int id = Integer.parseInt(getURL().split("://")[1]);
            widget.getContext().startActivity(Intents.openProfile(id, widget.getContext()));
        }
    }
}