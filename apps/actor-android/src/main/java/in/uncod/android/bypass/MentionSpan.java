package in.uncod.android.bypass;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import im.actor.messenger.app.Intents;

public class MentionSpan extends BaseUrlSpan {

    public MentionSpan(String url, boolean hideUrlStyle) {
        super(url, hideUrlStyle);
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