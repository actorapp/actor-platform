package im.actor.sdk.view;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

import im.actor.sdk.ActorSDK;

public class BaseUrlSpan extends URLSpan {

    protected boolean hideUrlStyle;

    public BaseUrlSpan(String url, boolean hideUrlStyle) {
        super(url);
        this.hideUrlStyle = hideUrlStyle;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if (hideUrlStyle) {
            ds.setUnderlineText(false);
            ds.setColor(Color.BLACK);
        }
    }

    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";

    @Override
    public void onClick(View v) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getURL()));
        Bundle extras = new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            extras.putBinder(EXTRA_CUSTOM_TABS_SESSION, null);
        }
        extras.putInt(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, ActorSDK.sharedActor().style.getMainColor());
        viewIntent.putExtras(extras);
        if (viewIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
            super.onClick(v);
        } else {
            Toast.makeText(v.getContext(), "Unknown URL type", Toast.LENGTH_SHORT).show();
        }
    }
}