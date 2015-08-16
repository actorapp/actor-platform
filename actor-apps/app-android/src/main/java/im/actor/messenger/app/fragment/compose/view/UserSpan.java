package im.actor.messenger.app.fragment.compose.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.util.Screen;
import im.actor.core.viewmodel.UserVM;

/**
* Created by ex3ndr on 26.03.15.
*/
public class UserSpan extends ReplacementSpan {

    private UserVM user;
    private int maxW;
    private String userText;
    private TextPaint textPaint;

    public UserSpan(UserVM user, int maxW) {
        this.user = user;
        this.maxW = maxW;
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            textPaint.setTextSize(Screen.sp(16));
            textPaint.setColor(AppContext.getContext().getResources().getColor(R.color.text_primary));
        }

        int padding = Screen.dp(18);
        int maxWidth = maxW - padding;
        userText = TextUtils.ellipsize(user.getName().get(), textPaint, maxWidth, TextUtils.TruncateAt.END).toString();
    }

    public UserVM getUser() {
        return user;
    }

    @Override
    public int getSize(Paint paint, CharSequence charSequence, int start, int end, Paint.FontMetricsInt fm) {
        if (fm != null) {
            // WTF???
            fm.ascent = -Screen.dp(21 + 3);
            fm.descent = Screen.dp(10 + 3);

            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }
        return (int) textPaint.measureText(userText) + Screen.dp(24 + 8);
    }

    @Override
    public void draw(Canvas canvas, CharSequence charSequence, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        int size = (int) textPaint.measureText(userText);
        Paint debug = new Paint();
        debug.setColor(0xffebebeb);
        debug.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRoundRect(new RectF(x + Screen.dp(4), y - Screen.dp(20), x + size + Screen.dp(4 + 24), y + Screen.dp(8)), Screen.dp(14), Screen.dp(14), debug);
        canvas.drawText(userText, x + Screen.dp(4 + 12), y, textPaint);
    }
}
