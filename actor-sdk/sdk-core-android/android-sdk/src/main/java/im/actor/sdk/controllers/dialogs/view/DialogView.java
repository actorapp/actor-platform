package im.actor.sdk.controllers.dialogs.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.AvatarImage;
import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.FileCallback;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.ListItemBackgroundView;
import im.actor.sdk.view.TintDrawable;
import im.actor.sdk.view.emoji.SmileProcessor;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.view.emoji.SmileProcessor.emoji;

public class DialogView extends ListItemBackgroundView<Dialog, DialogView.DialogLayout> {

    private static boolean isStylesLoaded = false;
    private static TextPaint titlePaint;
    private static TextPaint titleSecurePaint;
    private static TextPaint datePaint;
    private static TextPaint textPaint;
    private static TextPaint textActivePaint;
    private static TextPaint counterTextPaint;
    private static Paint counterBgPaint;
    private static int senderTextColor;
    private static Drawable groupIcon;
    private static Drawable secretIcon;
    private static Drawable channelIcon;
    private static Drawable botIcon;
    private static int[] placeholderColors;
    private static Paint avatarBorder;
    private static Paint fillPaint;
    private static TextPaint avatarTextColor;
    private static String typingText;
    private static Drawable stateSent;
    private static Drawable stateReceived;
    private static Drawable stateRead;

    private long bindedId;
    private DraweeHolder<GenericDraweeHierarchy> draweeHolder;
    private RectF tmpRect = new RectF();
    private int bindedUid;
    private int bindedGid;
    private ValueChangedListener<Boolean> privateTypingListener;
    private ValueChangedListener<int[]> groupTypingListener;
    private boolean isPrivateTyping;

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
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setFadeDuration(0)
                .setRoundingParams(new RoundingParams()
                        .setRoundAsCircle(true))
                .build();
        draweeHolder = DraweeHolder.create(hierarchy, getContext());
        draweeHolder.getTopLevelDrawable().setCallback(this);

        setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(72)));
        setDividerPaddingLeft(Screen.dp(72));

//        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DialogLayout layout = getLayout();
        if (layout != null) {

            //
            // Avatar
            //

            fillPaint.setColor(placeholderColors[layout.getPlaceholderIndex()]);
            if (layout.getImageRequest() != null) {
                Drawable drawable = draweeHolder.getTopLevelDrawable();
                drawable.setBounds(Screen.dp(12), Screen.dp(12), Screen.dp(60), Screen.dp(60));
                drawable.draw(canvas);
            } else {
                canvas.drawCircle(Screen.dp(36), Screen.dp(36), Screen.dp(24), fillPaint);
                canvas.drawText(layout.getShortName(),
                        0, layout.getShortName().length(),
                        Screen.dp(36), Screen.dp(44), avatarTextColor);
            }
            canvas.drawCircle(Screen.dp(36), Screen.dp(36), Screen.dp(24), avatarBorder);


            //
            // Title
            //

            if (layout.getTitleIcon() != null) {
                int left = Screen.dp(72) + (Screen.dp(16) - layout.getTitleIcon().getIntrinsicWidth()) / 2;
                int bottom = layout.getTitleIconTop();
                layout.getTitleIcon().setBounds(left, bottom - layout.getTitleIcon().getIntrinsicHeight(),
                        left + layout.getTitleIcon().getIntrinsicWidth(), bottom);
                layout.getTitleIcon().draw(canvas);
            }

            canvas.save();
            if (layout.getTitleIcon() == null) {
                canvas.translate(Screen.dp(72), Screen.dp(14));
            } else {
                canvas.translate(Screen.dp(72 + 16 + 4), Screen.dp(14));
            }
            layout.getTitleLayout().draw(canvas);
            canvas.restore();

            if (layout.getState() != null) {
                int left = getWidth() - Screen.dp(38) - layout.getDateWidth();
                layout.getState().setBounds(left, Screen.dp(18), left + Screen.dp(16), Screen.dp(18 + 9));
                layout.getState().draw(canvas);
            }

            if (layout.getDate() != null) {
                canvas.drawText(layout.getDate(), getWidth() - Screen.dp(16) - layout.getDateWidth(), Screen.dp(28), datePaint);
            }


            //
            // Content
            //

            // TODO: Implement Group Typing
            if (isPrivateTyping) {
                canvas.drawText(typingText, Screen.dp(72), Screen.dp(54), textActivePaint);
            } else {
                if (layout.getTextLayout() != null) {
                    canvas.save();
                    canvas.translate(Screen.dp(72), Screen.dp(40));
                    layout.getTextLayout().draw(canvas);
                    canvas.restore();
                }

                if (layout.getCounter() != null) {
                    int left = getWidth() - Screen.sp(12) - layout.getCounterWidth();
                    int top = Screen.dp(37);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        canvas.drawRoundRect(left, top, left + layout.getCounterWidth(), top + Screen.dp(22), Screen.dp(11), Screen.dp(11),
                                counterBgPaint);
                    } else {
                        tmpRect.set(left, top, left + layout.getCounterWidth(), top + Screen.dp(22));
                        canvas.drawRoundRect(tmpRect, Screen.dp(11), Screen.dp(11), counterBgPaint);
                    }
                    canvas.drawText(layout.getCounter(), left + layout.getCounterWidth() / 2, top + Screen.dp(16), counterTextPaint);
                }
            }
        }
    }

    //
    // Binding
    //

    public void bind(Dialog dialog) {

        requestLayout(dialog, bindedId != dialog.getEngineId());
        bindedId = dialog.getEngineId();

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid).unsubscribe(privateTypingListener);
            privateTypingListener = null;
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid).unsubscribe(groupTypingListener);
            groupTypingListener = null;
        }

        if (dialog.getPeer().getPeerType() == PeerType.PRIVATE) {
            bindedUid = dialog.getPeer().getPeerId();

            ValueModel<Boolean> typingModel = messenger().getTyping(bindedUid);

            privateTypingListener = (val, Value) -> {
                isPrivateTyping = val;
                invalidate();
            };
            typingModel.subscribe(privateTypingListener, false);
            isPrivateTyping = typingModel.get();
        } else if (dialog.getPeer().getPeerType() == PeerType.GROUP) {
            bindedGid = dialog.getPeer().getPeerId();
            isPrivateTyping = false;
//            groupTypingListener = (val, Value) -> {
//                // TODO: Implement Group Typing
//            };
//            messenger().getGroupTyping(bindedGid).subscribe(groupTypingListener);
        } else {
            isPrivateTyping = false;
        }
    }

    @Override
    public DialogLayout buildLayout(Dialog arg, int width, int height) {
        if (!isStylesLoaded) {
            isStylesLoaded = true;
            ActorStyle style = ActorSDK.sharedActor().style;
            Context context = getContext();
            titlePaint = createTextPaint(Fonts.medium(), 16, style.getDialogsTitleColor());
            titleSecurePaint = createTextPaint(Fonts.medium(), 16, style.getDialogsTitleSecureColor());
            datePaint = createTextPaint(Fonts.regular(), 14, style.getDialogsTimeColor());
            textPaint = createTextPaint(Fonts.regular(), 16, style.getDialogsTimeColor());
            textActivePaint = createTextPaint(Fonts.regular(), 16, style.getDialogsActiveTextColor());
            senderTextColor = style.getDialogsActiveTextColor();
            groupIcon = new TintDrawable(context.getResources().getDrawable(R.drawable.ic_group_black_18dp),
                    style.getDialogsTitleColor());
            channelIcon = new TintDrawable(context.getResources().getDrawable(R.drawable.ic_megaphone_18dp_black),
                    style.getDialogsTitleColor());
            botIcon = new TintDrawable(context.getResources().getDrawable(R.drawable.ic_robot_black_18dp),
                    style.getDialogsTitleColor());
            secretIcon = new TintDrawable(context.getResources().getDrawable(R.drawable.ic_lock_black_18dp),
                    style.getDialogsTitleSecureColor());
            counterTextPaint = createTextPaint(Fonts.medium(), 14, style.getDialogsCounterTextColor());
            counterTextPaint.setTextAlign(Paint.Align.CENTER);
            counterBgPaint = createFilledPaint(style.getDialogsCounterBackgroundColor());
            fillPaint = createFilledPaint(Color.BLACK);
            placeholderColors = ActorSDK.sharedActor().style.getDefaultAvatarPlaceholders();
            avatarBorder = new Paint();
            avatarBorder.setStyle(Paint.Style.STROKE);
            avatarBorder.setAntiAlias(true);
            avatarBorder.setColor(0x19000000);
            avatarBorder.setStrokeWidth(1);
            avatarTextColor = createTextPaint(Fonts.regular(), 20, Color.WHITE);
            avatarTextColor.setTextAlign(Paint.Align.CENTER);
            typingText = messenger().getFormatter().formatTyping();

            stateSent = new TintDrawable(context.getResources().getDrawable(R.drawable.msg_check_1),
                    style.getDialogsStateSentColor());
            stateReceived = new TintDrawable(context.getResources().getDrawable(R.drawable.msg_check_2),
                    style.getDialogsStateDeliveredColor());
            stateRead = new TintDrawable(context.getResources().getDrawable(R.drawable.msg_check_2),
                    style.getDialogsStateReadColor());
        }

        DialogLayout res = new DialogLayout();

        res.setPlaceholderIndex(Math.abs(arg.getPeer().getPeerId()) % placeholderColors.length);
        res.setShortName(buildShortName(arg.getDialogTitle()));
        if (arg.getDialogAvatar() != null) {
            AvatarImage image = getAvatarImage(arg.getDialogAvatar());
            if (image != null) {
                String desc = messenger().findDownloadedDescriptor(image.getFileReference().getFileId());
                if (desc != null) {
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(
                            Uri.fromFile(new File(desc)))
                            .setResizeOptions(new ResizeOptions(Screen.dp(52), Screen.dp(52)))
                            .setImageType(ImageRequest.ImageType.SMALL)
                            .build();
                    res.setImageRequest(request);
                } else {
                    InvalidationContext invalidationContext = getCurrentLayoutContext();
                    messenger().bindRawFile(image.getFileReference(), true, new FileCallback() {
                        @Override
                        public void onNotDownloaded() {
                            if (invalidationContext.isCancelled()) {
                                messenger().unbindRawFile(image.getFileReference().getFileId(), false, this);
                            }
                        }

                        @Override
                        public void onDownloading(float progress) {
                            if (invalidationContext.isCancelled()) {
                                messenger().unbindRawFile(image.getFileReference().getFileId(), false, this);
                            }
                        }

                        @Override
                        public void onDownloaded(FileSystemReference reference) {
                            messenger().unbindRawFile(image.getFileReference().getFileId(), false, this);
                            invalidationContext.invalidate();
                        }
                    });
                }
            }
        }

        // Top Row
        int maxTitleWidth = (width - Screen.dp(72)) - Screen.dp(8);
        if (arg.getDate() > 0) {
            String dateText = messenger().getFormatter().formatShortDate(arg.getDate());
            int dateWidth = (int) datePaint.measureText(dateText);
            res.setDate(dateText, dateWidth);
            maxTitleWidth -= dateWidth + Screen.dp(16);
        }

        if (arg.getPeer().getPeerType() == PeerType.GROUP) {
            if (arg.isChannel()) {
                res.setTitleIcon(channelIcon);
                res.setTitleIconTop(Screen.dp(33));
            } else {
                res.setTitleIcon(groupIcon);
                res.setTitleIconTop(Screen.dp(33));
            }
            maxTitleWidth -= Screen.dp(16/*icon width*/ + 4/*padding*/);
        } else if (arg.getPeer().getPeerType() == PeerType.PRIVATE_ENCRYPTED) {
            res.setTitleIcon(secretIcon);
            res.setTitleIconTop(Screen.dp(31));
            maxTitleWidth -= Screen.dp(16/*icon width*/ + 4/*padding*/);
        } else if (arg.getPeer().getPeerType() == PeerType.PRIVATE) {
            if (arg.isBot()) {
                res.setTitleIcon(botIcon);
                res.setTitleIconTop(Screen.dp(33));
                maxTitleWidth -= Screen.dp(16/*icon width*/ + 4/*padding*/);
            }
        }

        if (arg.getSenderId() == messenger().myUid()) {
            if (arg.isRead()) {
                res.setState(stateRead);
            } else if (arg.isReceived()) {
                res.setState(stateReceived);
            } else {
                res.setState(stateSent);
            }
            maxTitleWidth -= Screen.dp(20);
        }

        res.setTitleLayout(singleLineText(arg.getDialogTitle(),
                arg.getPeer().getPeerType() == PeerType.PRIVATE_ENCRYPTED
                        ? titleSecurePaint
                        : titlePaint,
                maxTitleWidth));

        // Second Row
        int maxWidth = width - Screen.dp(72) - Screen.dp(8);

        if (arg.getUnreadCount() > 0) {
            String counterText = "" + arg.getUnreadCount();
            int counterWidth = (int) counterTextPaint.measureText(counterText) + Screen.sp(10);
            counterWidth = Math.max(counterWidth, Screen.dp(22));
            res.setCounter(counterText, counterWidth);
            maxWidth -= counterWidth + Screen.dp(8);
        }

        if (arg.getSenderId() > 0) {

            String contentText = messenger().getFormatter().formatContentText(arg.getSenderId(),
                    arg.getMessageType(), arg.getText().replace("\n", " "), arg.getRelatedUid(),
                    arg.isChannel());

            if (arg.getPeer().getPeerType() == PeerType.GROUP) {
                if (messenger().getFormatter().isLargeDialogMessage(arg.getMessageType())) {
                    res.setTextLayout(singleLineText(handleEmoji(contentText), textActivePaint, maxWidth));
                } else {
                    String senderName = messenger().getFormatter().formatPerformerName(arg.getSenderId()) + ": ";
                    if (arg.getMessageType() == ContentType.TEXT) {
                        SpannableStringBuilder builder = new SpannableStringBuilder();
                        builder.append(senderName);
                        builder.setSpan(new ForegroundColorSpan(senderTextColor), 0, senderName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        builder.append(contentText);
                        res.setTextLayout(singleLineText(builder, textPaint, maxWidth));
                    } else {
                        CharSequence contentResult = handleEmoji(senderName, contentText);
                        res.setTextLayout(singleLineText(contentResult, textActivePaint, maxWidth));
                    }
                }
            } else {
                if (arg.getMessageType() == ContentType.TEXT) {
                    res.setTextLayout(singleLineText(handleEmoji(contentText), textPaint, maxWidth));
                } else {
                    res.setTextLayout(singleLineText(handleEmoji(contentText), textActivePaint, maxWidth));
                }
            }
        }

        return res;
    }

    @Override
    public void layoutReady(DialogLayout res) {
        super.layoutReady(res);

        draweeHolder.onAttach();
        if (res.getImageRequest() != null) {
            draweeHolder.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(res.getImageRequest())
                    .setOldController(draweeHolder.getController())
                    .build());
        } else {
            draweeHolder.setController(Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeHolder.getController())
                    .build());
        }
    }

    private CharSequence handleEmoji(CharSequence... args) {
        StringBuilder builder = new StringBuilder();
        for (CharSequence seq : args) {
            if (SmileProcessor.containsEmoji(seq)) {
                if (emoji().isLoaded()) {
                    builder.append(emoji().processEmojiCompatMutable(seq, SmileProcessor.CONFIGURATION_BUBBLES));
                } else {
                    builder.append(seq);
                }
            } else {
                builder.append(seq);
            }
        }
        return builder;
    }

    private CharSequence buildShortName(String name) {
        if (name == null) {
            name = "?";
        } else if (name.length() == 0) {
            name = "?";
        } else {
            String[] parts = name.trim().split(" ", 2);
            if (parts.length == 0 || parts[0].length() == 0) {
                name = "?";
            } else {
                name = parts[0].substring(0, 1).toUpperCase();
                if (parts.length == 2 && parts[1].length() > 0) {
                    name += parts[1].substring(0, 1).toUpperCase();
                }
            }
        }

        return handleEmoji(name);
    }

    private AvatarImage getAvatarImage(Avatar avatar) {
        return Screen.dp(52) >= 100 ? avatar.getLargeImage() : avatar.getSmallImage();
    }

    public void unbind() {
        cancelLayout();

        bindedId = -1;

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid).unsubscribe(privateTypingListener);
            isPrivateTyping = false;
            privateTypingListener = null;
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid).unsubscribe(groupTypingListener);
            groupTypingListener = null;
        }

        draweeHolder.onDetach();
    }

    //
    // Drawee
    //

//    @Override
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        draweeHolder.onDetach();
//    }
//
//    @Override
//    public void onStartTemporaryDetach() {
//        super.onStartTemporaryDetach();
//        draweeHolder.onDetach();
//    }
//
//    @Override
//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        draweeHolder.onAttach();
//    }
//
//    @Override
//    public void onFinishTemporaryDetach() {
//        super.onFinishTemporaryDetach();
//        draweeHolder.onAttach();
//    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        if (who == draweeHolder.getTopLevelDrawable()) {
            return true;
        }

        return super.verifyDrawable(who);
    }

    public static class DialogLayout {

        private ImageRequest imageRequest;
        private int placeholderIndex;
        private CharSequence shortName;
        private Layout titleLayout;
        private Drawable titleIcon;
        private int titleIconTop;
        private String date;
        private int dateWidth;
        private Layout textLayout;
        private String counter;
        private int counterWidth;
        private Drawable state;

        public int getTitleIconTop() {
            return titleIconTop;
        }

        public void setTitleIconTop(int titleIconTop) {
            this.titleIconTop = titleIconTop;
        }

        public Drawable getState() {
            return state;
        }

        public void setState(Drawable state) {
            this.state = state;
        }

        public ImageRequest getImageRequest() {
            return imageRequest;
        }

        public void setImageRequest(ImageRequest imageRequest) {
            this.imageRequest = imageRequest;
        }

        public int getPlaceholderIndex() {
            return placeholderIndex;
        }

        public void setPlaceholderIndex(int placeholderIndex) {
            this.placeholderIndex = placeholderIndex;
        }

        public CharSequence getShortName() {
            return shortName;
        }

        public void setShortName(CharSequence shortName) {
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

        public Drawable getTitleIcon() {
            return titleIcon;
        }

        public void setTitleIcon(Drawable titleIcon) {
            this.titleIcon = titleIcon;
        }

        public void setCounter(String counter, int counterWidth) {
            this.counter = counter;
            this.counterWidth = counterWidth;
        }

        public String getCounter() {
            return counter;
        }

        public int getCounterWidth() {
            return counterWidth;
        }
    }
}
