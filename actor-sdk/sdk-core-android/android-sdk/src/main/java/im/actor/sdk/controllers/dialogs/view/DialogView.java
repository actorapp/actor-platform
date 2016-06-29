package im.actor.sdk.controllers.dialogs.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;

import im.actor.core.entity.Dialog;
import im.actor.core.entity.PeerType;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.ListItemBackgroundView;
import im.actor.sdk.view.emoji.SmileProcessor;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;
import static im.actor.sdk.view.emoji.SmileProcessor.emoji;

public class DialogView extends ListItemBackgroundView<Dialog, DialogView.DialogLayout> {

    private static boolean isStylesLoaded = false;
    private static TextPaint titlePaint;
    private static TextPaint datePaint;
    private static TextPaint textPaint;

    public DialogView(Context context) {
        super(context);
        initStyles();
    }

    public DialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyles();
    }

    public DialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStyles();
    }

    public DialogView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initStyles();
    }

    protected void initStyles() {
        setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(72)));
        setDividerPaddingLeft(Screen.dp(72));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DialogLayout layout = getLayout();
        if (layout != null) {
            canvas.save();
            canvas.translate(Screen.dp(72), Screen.dp(12));
            layout.getTitleLayout().draw(canvas);
            canvas.restore();

            if (layout.getDate() != null) {
                canvas.drawText(layout.getDate(), getWidth() - Screen.dp(16) - layout.getDateWidth(), Screen.dp(26), datePaint);
            }

            if (layout.getTextLayout() != null) {
                canvas.save();
                canvas.translate(Screen.dp(72), Screen.dp(42));
                layout.getTextLayout().draw(canvas);
                canvas.restore();
            }
        }
    }

    //
    // Binding
    //

    public void bind(Dialog dialog) {
        requestLayout(dialog);
    }

    @Override
    public DialogLayout buildLayout(Dialog arg, int width, int height) {
        if (!isStylesLoaded) {
            ActorStyle style = ActorSDK.sharedActor().style;
            titlePaint = createTextPaint(Fonts.medium(), 16, style.getDialogsTitleColor());
            datePaint = createTextPaint(Fonts.regular(), 14, style.getDialogsTimeColor());
            textPaint = createTextPaint(Fonts.regular(), 16, style.getDialogsTimeColor());
        }

        DialogLayout res = new DialogLayout();

        // Date
        int maxTitleWidth = (width - Screen.dp(72)) - Screen.dp(8);
        if (arg.getDate() > 0) {
            String dateText = messenger().getFormatter().formatShortDate(arg.getDate());
            int dateWidth = (int) datePaint.measureText(dateText);
            res.setDate(dateText, dateWidth);
            maxTitleWidth -= dateWidth + Screen.dp(16);
        }

        // Title
        res.setTitleLayout(singleLineText(arg.getDialogTitle(), titlePaint, maxTitleWidth));

        // Text
        if (arg.getSenderId() > 0) {

            String contentText = messenger().getFormatter().formatContentText(arg.getSenderId(),
                    arg.getMessageType(), arg.getText().replace("\n", " "), arg.getRelatedUid());
            CharSequence contentResult;
            if (arg.getPeer().getPeerType() == PeerType.GROUP) {
                if (messenger().getFormatter().isLargeDialogMessage(arg.getMessageType())) {
                    // Is Large Service Message
                    contentResult = contentText;
                } else {
                    contentResult = users().get(arg.getSenderId()).getName().get() + ": " + contentText;
                }
            } else {
                contentResult = contentText;
            }
            
            res.setTextLayout(singleLineText(contentResult, textPaint, width - Screen.dp(72) - Screen.dp(8)));

//            CharSequence text = messenger().getFormatter().formatDialogText(arg).replace("\n", " ");
//            if (SmileProcessor.containsEmoji(text)) {
//                if (emoji().isLoaded()) {
//                    text = emoji().processEmojiCompatMutable(text, SmileProcessor.CONFIGURATION_BUBBLES);
//                }
//            }

//            res.setTextLayout(singleLineText(text, textPaint, width - Screen.dp(72) - Screen.dp(8)));
        }

        return res;
    }

    private void unbind() {
        cancelLayout();
    }

    public static class DialogLayout {

        private String shortName;
        private Layout titleLayout;
        private String date;
        private int dateWidth;
        private Layout textLayout;

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public Layout getTitleLayout() {
            return titleLayout;
        }

        public void setTitleLayout(Layout titleLayout) {
            this.titleLayout = titleLayout;
        }

        public String getDate() {
            return date;
        }

        public int getDateWidth() {
            return dateWidth;
        }

        public void setDate(String date, int width) {
            this.date = date;
            this.dateWidth = width;
        }

        public Layout getTextLayout() {
            return textLayout;
        }

        public void setTextLayout(Layout textLayout) {
            this.textLayout = textLayout;
        }
    }
}
