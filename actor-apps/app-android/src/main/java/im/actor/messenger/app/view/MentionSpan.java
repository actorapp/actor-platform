package im.actor.messenger.app.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.Intents;

public class MentionSpan extends BaseUrlSpan {
    int[] colors;
    int userId;

    public static Typeface tf;

    public MentionSpan(String nick, int userId, boolean hideUrlStyle) {
        super(nick, hideUrlStyle);
        this.userId = userId;
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
        if (hideUrlStyle) {
            ds.setUnderlineText(false);
            ds.setColor(Color.BLACK);
        }

        if (tf == null) {
            tf = Typeface.createFromAsset(AppContext.getContext().getAssets(), "Roboto-Medium.ttf");
        }

        ds.setColor(colors[Math.abs(userId) % colors.length]);
        ds.setTypeface(tf);
        ds.setUnderlineText(false);
    }

    private String url;

    public void setUrl(String s) {
        this.url = s;
    }

    @Override
    public void onClick(View widget) {
        if (hideUrlStyle) {
            //Do nothing
        } else {

            widget.getContext().startActivity(Intents.openProfile(userId, widget.getContext()));
        }
    }
}