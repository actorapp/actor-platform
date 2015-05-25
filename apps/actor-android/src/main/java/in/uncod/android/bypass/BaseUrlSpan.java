package in.uncod.android.bypass;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

class BaseUrlSpan extends URLSpan {

    Context ctx;
    boolean hideUrlStyle;

    public BaseUrlSpan(String url, Context ctx, boolean hideUrlStyle) {
        super(url);
        this.ctx = ctx;
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
        if(viewIntent.resolveActivity(ctx.getPackageManager())!=null){
            super.onClick(v);
        }else{
            Toast.makeText(ctx, "Unknown URL type", Toast.LENGTH_SHORT).show();
        }
    }
}