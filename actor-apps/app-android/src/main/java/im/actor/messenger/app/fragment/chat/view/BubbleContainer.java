package im.actor.messenger.app.fragment.chat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.util.TextUtils;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.core.viewmodel.UserVM;

import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.users;

/**
 * Created by ex3ndr on 19.01.15.
 */
public class BubbleContainer extends ViewGroup {

    public interface OnAvatarClickListener {
        void onAvatarClick(int uid);
    }

    public interface OnAvatarLongClickListener {
        void onAvatarLongClick(int uid);
    }

    private static final int MODE_LEFT = 0;
    private static final int MODE_RIGHT = 1;
    private static final int MODE_FULL = 2;

    private final Paint SELECTOR_PAINT = new Paint();

    private boolean showDateDiv;
    private boolean showUnreadDiv;
    private boolean showAvatar;

    private TextView dateDiv;
    private TextView unreadDiv;
    private AvatarView avatarView;

    private int mode = MODE_FULL;

    private boolean isSelected;

    private OnAvatarClickListener onClickListener;
    private OnAvatarLongClickListener onLongClickListener;

    public BubbleContainer(Context context) {
        super(context);
        init();
    }

    public BubbleContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        setWillNotDraw(false);

        SELECTOR_PAINT.setColor(getResources().getColor(R.color.selector_selected));
        SELECTOR_PAINT.setStyle(Paint.Style.FILL);

        // DATE

        showDateDiv = false;

        dateDiv = new TextView(getContext());
        dateDiv.setTextSize(12);
        dateDiv.setTypeface(Fonts.regular());
        dateDiv.setIncludeFontPadding(false);
        dateDiv.setBackgroundResource(R.drawable.conv_bubble_date_bg);
        dateDiv.setGravity(Gravity.CENTER);
        dateDiv.setTextColor(getResources().getColor(R.color.conv_date_text));

        if (!showDateDiv) {
            dateDiv.setVisibility(GONE);
        } else {
            dateDiv.setVisibility(VISIBLE);
        }

        addView(dateDiv, new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // UNREAD

        showUnreadDiv = false;

        unreadDiv = new TextView(getContext());
        unreadDiv.setTextSize(13);
        unreadDiv.setTypeface(Fonts.regular());
        unreadDiv.setIncludeFontPadding(false);
        unreadDiv.setBackgroundColor(getResources().getColor(R.color.conv_date_bg));
        unreadDiv.setGravity(Gravity.CENTER);
        unreadDiv.setTextColor(getResources().getColor(R.color.conv_date_text));
        unreadDiv.setPadding(0, Screen.dp(6), 0, Screen.dp(6));
        unreadDiv.setText(R.string.chat_new_messages);

        if (!showUnreadDiv) {
            unreadDiv.setVisibility(GONE);
        } else {
            unreadDiv.setVisibility(VISIBLE);
        }

        addView(unreadDiv, new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // AVATAR
        avatarView = new AvatarView(getContext());
        avatarView.init(Screen.dp(42), 12);
        addView(avatarView, new MarginLayoutParams(Screen.dp(42), Screen.dp(42)));
    }

    public OnAvatarClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnAvatarClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnAvatarLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void makeFullSizeBubble() {
        mode = MODE_FULL;
        showAvatar = false;
        avatarView.setVisibility(GONE);
        findMessageView().setLayoutParams(new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        requestLayout();
    }

    public void makeOutboundBubble() {
        mode = MODE_RIGHT;
        showAvatar = false;
        avatarView.setVisibility(GONE);
        avatarView.unbind();
        findMessageView().setLayoutParams(new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        requestLayout();
    }

    public void makeInboundBubble(boolean showAvatar, final int uid) {
        makeInboundBubble(showAvatar, uid, 0);
    }

    public void makeInboundBubble(boolean showAvatar, final int uid, final int gid) {
        mode = MODE_LEFT;
        this.showAvatar = showAvatar;
        if (showAvatar) {
            UserVM u = users().get(uid);
            avatarView.setVisibility(VISIBLE);
            if(gid!=0){
                avatarView.bind(groups().get(gid));
            }else{
                avatarView.bind(u);
            }
            avatarView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onAvatarClick(uid);
                    }
                }
            });
            avatarView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onLongClickListener != null) {
                        onLongClickListener.onAvatarLongClick(uid);
                        return true;
                    }else{
                        return false;
                    }
                }
            });
        } else {
            avatarView.setVisibility(GONE);
            avatarView.unbind();
        }
        findMessageView().setLayoutParams(new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void showDate(long time) {
        showDateDiv = true;
        dateDiv.setText(TextUtils.formatDate(time));
        dateDiv.setVisibility(VISIBLE);
        requestLayout();
    }

    public void hideDate() {
        if (showDateDiv) {
            dateDiv.setVisibility(GONE);
            showDateDiv = false;
            requestLayout();
        }
    }

    public void showUnread() {
        if (!showUnreadDiv) {
            showUnreadDiv = true;
            unreadDiv.setVisibility(VISIBLE);
            requestLayout();
        }
    }

    public void hideUnread() {
        if (showUnreadDiv) {
            showUnreadDiv = false;
            unreadDiv.setVisibility(GONE);
            requestLayout();
        }
    }

    private View findMessageView() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != dateDiv && v != unreadDiv && v != avatarView) {
                return v;
            }
        }
        throw new RuntimeException("Unable to find bubble view!");
    }

    // Small hack for avoiding listview selection

    public void setBubbleSelected(boolean isSelected) {
        this.isSelected = isSelected;
        setSelected(isSelected);
        invalidate();
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected != isSelected) {
            return;
        }
        super.setSelected(selected);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int topOffset = 0;

        View messageView = findMessageView();
        int padding = Screen.dp(8);
        if (showAvatar) {
            padding += Screen.dp(48);
        }
        measureChildWithMargins(messageView, widthMeasureSpec, padding, heightMeasureSpec, 0);

        if (showDateDiv) {
            measureChild(dateDiv, widthMeasureSpec, heightMeasureSpec);
            topOffset += Screen.dp(16) + dateDiv.getMeasuredHeight();
        }

        if (showUnreadDiv) {
            measureChild(unreadDiv, widthMeasureSpec, heightMeasureSpec);
            topOffset += Screen.dp(16) + unreadDiv.getMeasuredHeight();
        }

        if (showAvatar) {
            measureChild(avatarView, widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), messageView.getMeasuredHeight() + topOffset);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int topOffset = 0;

        if (showUnreadDiv) {
            int w = unreadDiv.getMeasuredWidth();
            int h = unreadDiv.getMeasuredHeight();
            int dateLeft = (right - left - w) / 2;
            unreadDiv.layout(dateLeft, topOffset + Screen.dp(8), dateLeft + w, topOffset + Screen.dp(8) + h);
            topOffset += Screen.dp(16) + h;
        }

        if (showDateDiv) {
            int w = dateDiv.getMeasuredWidth();
            int h = dateDiv.getMeasuredHeight();
            int dateLeft = (right - left - w) / 2;
            dateDiv.layout(dateLeft, topOffset + Screen.dp(8), dateLeft + w, topOffset + Screen.dp(8) + h);
            topOffset += Screen.dp(16) + h;
        }

        if (showAvatar) {
            int w = avatarView.getMeasuredWidth();
            int h = avatarView.getMeasuredHeight();
            avatarView.layout(Screen.dp(6),
                    bottom - top - h - Screen.dp(4),
                    Screen.dp(6) + w,
                    bottom - top - Screen.dp(4));
        }

        View bubble = findMessageView();
        int w = bubble.getMeasuredWidth();
        int h = bubble.getMeasuredHeight();

        if (mode == MODE_LEFT) {
            int leftOffset = 0;
            if (showAvatar) {
                leftOffset = Screen.dp(48);
            }
            bubble.layout(leftOffset, topOffset, leftOffset + w, topOffset + h);
        } else if (mode == MODE_RIGHT) {
            bubble.layout(getMeasuredWidth() - w, topOffset, getMeasuredWidth(), topOffset + h);
        } else if (mode == MODE_FULL) {
            int bubbleLeft = (right - left - w) / 2;
            bubble.layout(bubbleLeft, topOffset, bubbleLeft + w, topOffset + h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSelected) {
            View bubble = findMessageView();
            canvas.drawRect(0, getHeight() - bubble.getHeight(),
                    getWidth(), getHeight(), SELECTOR_PAINT);
        }
    }
}
