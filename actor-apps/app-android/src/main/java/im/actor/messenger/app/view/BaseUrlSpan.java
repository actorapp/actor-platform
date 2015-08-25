package im.actor.messenger.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

class BaseUrlSpan extends URLSpan {

    boolean hideUrlStyle;

    public BaseUrlSpan(String url, boolean hideUrlStyle) {
        super(url);
        this.hideUrlStyle = hideUrlStyle;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if(hideUrlStyle){
            ds.setUnderlineText(false);
            ds.setColor(Color.BLACK);
        }
    }


    @Override
    public void onClick(View v) {
        Intent viewIntent  = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL()));
        if(viewIntent.resolveActivity(v.getContext().getPackageManager())!=null){
            super.onClick(v);
        }else{
            Toast.makeText(v.getContext(), "Unknown URL type", Toast.LENGTH_SHORT).show();
        }
    }
}