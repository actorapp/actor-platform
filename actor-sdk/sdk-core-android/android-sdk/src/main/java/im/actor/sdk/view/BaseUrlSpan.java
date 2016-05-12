package im.actor.sdk.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Toast;

import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.ChatActivity;
import im.actor.sdk.receivers.ChromeCustomTabReceiver;

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
            ds.setColor(Color.BLACK);
        }
        ds.setColor(ActorSDK.sharedActor().style.getMainColor());
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View v) {
        Context ctx = v.getContext();
        Intent intent = buildChromeIntent().intent;
        intent.setData(Uri.parse(getURL()));
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(
                    intent);
        } else {
            intent.setData(Uri.parse("http://" + getURL()));
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(
                        intent);
            } else {
                Toast.makeText(ctx, "Unknown URL type", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static CustomTabsIntent buildChromeIntent() {
        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();

//        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setType("*/*");
//        PendingIntent pi = PendingIntent.getActivity(AndroidContext.getContext()    , 0, sendIntent, 0);

        Intent actionIntent = new Intent(
                AndroidContext.getContext(), ChromeCustomTabReceiver.class);
        PendingIntent pi =
                PendingIntent.getBroadcast(AndroidContext.getContext(), 0, actionIntent, 0);

        Bitmap back_icon = BitmapFactory.decodeResource(AndroidContext.getContext().getResources(), R.drawable.ic_arrow_back_white_24dp);
        if( RTLUtils.isRTL(AndroidContext.getContext()) ) {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            back_icon = Bitmap.createBitmap(back_icon, 0, 0, back_icon.getWidth(), back_icon.getHeight(), matrix, true);
        }

        customTabsIntent.setToolbarColor(ActorSDK.sharedActor().style.getMainColor())
                .setActionButton(BitmapFactory.decodeResource(AndroidContext.getContext().getResources(), R.drawable.ic_share_white_24dp), "Share", pi)
                .setCloseButtonIcon(back_icon);

        return customTabsIntent.build();
    }
}