package im.actor.messenger.app.fragment.contacts;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.images.cache.BitmapReference;
import com.droidkit.images.loading.ImageReceiver;
import com.droidkit.images.loading.ReceiverCallback;

import java.util.HashSet;

import im.actor.messenger.R;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.EngineHolderAdapter;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.app.view.SearchHighlight;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.core.images.AvatarTask;
import im.actor.messenger.util.Screen;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Contact;

import static im.actor.messenger.core.Core.core;

public class ContactsAdapter extends EngineHolderAdapter<Contact> implements ContactsStickListView.StickAdapter {

    private boolean selectable;

    private HashSet<Integer> selectedUsers = new HashSet<Integer>();

    private String query = "";

    private OnItemClickedListener<Contact> onItemClickedListener;
    private OnItemClickedListener<Contact> onItemLongClickedListener;

    public ContactsAdapter(EngineUiList<Contact> engine, Context context, boolean selectable,
                           OnItemClickedListener<Contact> onItemClickedListener,
                           OnItemClickedListener<Contact> onItemLongClickedListener) {
        super(engine, context);
        this.selectable = selectable;
        this.onItemClickedListener = onItemClickedListener;
        this.onItemLongClickedListener = onItemLongClickedListener;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setQuery(String query) {
        this.query = query;
        notifyDataSetInvalidated();
    }

    public void select(int uid) {
        selectedUsers.add(uid);
    }

    public void unselect(int uid) {
        selectedUsers.remove(uid);
    }

    @Override
    public long getItemId(Contact obj) {
        return obj.getUid();
    }

    @Override
    protected ViewHolder<Contact> createHolder(Contact obj) {
        return new Holder();
        // return new FastHolder();
    }

    @Override
    public String getItemHeader(int index) {
        return getFastName(getItem(index).getName());
    }

    private class FastHolder extends ViewHolder<Contact> {

        private ContactView view;

        @Override
        public View init(Contact data, ViewGroup viewGroup, Context context) {
            view = new ContactView(context);
            return view;
        }

        @Override
        public void bind(Contact data, int position, Context context) {

            String shortName = getFastName(data.getName());
            boolean showFastName = true;
            if (position > 0) {
                Contact contact = getUiList().getItem(position - 1);
                String prevShortName = getFastName(contact.getName());
                if (shortName.equals(prevShortName)) {
                    showFastName = false;
                }
            }

            view.bind(data.getUid(), data.getAvatar(), data.getName(), showFastName);
        }

        @Override
        public void unbind() {
            view.unbind();
        }
    }

    private class Holder extends ViewHolder<Contact> {

        private int padding = Screen.dp(16);

        private AvatarView avatar;
        private TextView title;

        private TextView fastTitle;

        private CheckBox isSelected;

        private FrameLayout cont;

        @Override
        public View init(Contact data, ViewGroup parent, Context context) {
            FrameLayout fl = new FrameLayout(context);

            fl.setBackgroundColor(context.getResources().getColor(R.color.bg_light));

            cont = new FrameLayout(context);

            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64));
                layoutParams.leftMargin = Screen.dp(40);
                fl.addView(cont, layoutParams);
            }

            cont.setBackgroundResource(R.drawable.selector);

            avatar = new AvatarView(context);
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
                layoutParams.leftMargin = Screen.dp(6);
                layoutParams.topMargin = Screen.dp(6);
                layoutParams.bottomMargin = Screen.dp(6);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                cont.addView(avatar, layoutParams);
            }

            fastTitle = new TextView(context);
            fastTitle.setTextColor(getContext().getResources().getColor(R.color.primary));
            fastTitle.setTextSize(18);
            fastTitle.setGravity(Gravity.CENTER);
            fastTitle.setTypeface(Fonts.regular());
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(40), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = Screen.dp(6);
                layoutParams.gravity = Gravity.CENTER_VERTICAL;
                fl.addView(fastTitle, layoutParams);
            }

            title = new TextView(context);
            title.setTextColor(getContext().getResources().getColor(R.color.text_primary));
            title.setPadding(Screen.dp(72), 0, (selectable ? Screen.dp(64) : 0) + Screen.dp(8), 0);
            title.setTextSize(16);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setTypeface(Fonts.regular());
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL;
                layoutParams.topMargin = padding;
                layoutParams.bottomMargin = padding;
                cont.addView(title, layoutParams);
            }

            if (selectable) {
                isSelected = new CheckBox(context);
                isSelected.setClickable(false);
                isSelected.setFocusable(false);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                layoutParams.rightMargin = padding;
                cont.addView(isSelected, layoutParams);
            }

            View div = new View(getContext());
            div.setBackgroundColor(getContext().getResources().getColor(R.color.divider));
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        getContext().getResources().getDimensionPixelSize(R.dimen.div_size));
                layoutParams.gravity = Gravity.BOTTOM;
                layoutParams.leftMargin = Screen.dp(72);
                cont.addView(div, layoutParams);
            }

            return fl;
        }

        @Override
        public void bind(final Contact data, int position, Context context) {
            String shortName = getFastName(data.getName());
            if (position == 0) {
                fastTitle.setVisibility(View.VISIBLE);
                fastTitle.setText(shortName);
            } else {
                Contact contact = getUiList().getItem(position - 1);
                String prevShortName = getFastName(contact.getName());
                if (shortName.equals(prevShortName)) {
                    fastTitle.setVisibility(View.GONE);
                } else {
                    fastTitle.setVisibility(View.VISIBLE);
                    fastTitle.setText(shortName);
                }
            }

            avatar.setEmptyDrawable(new AvatarDrawable(data.getName(),
                    data.getUid(), 24, context));

            if (data.getAvatar() != null) {
                avatar.bindAvatar(Screen.dp(54), data.getAvatar());
            } else {
                avatar.unbind();
            }

            if (query.length() > 0) {
                title.setText(SearchHighlight.highlightQuery(data.getName(), query, 0xff0277bd));
            } else {
                title.setText(data.getName());
            }

            if (selectable) {
                isSelected.setChecked(selectedUsers.contains(data.getUid()));
            }

            cont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickedListener.onClicked(data);
                }
            });

            if (onItemLongClickedListener != null) {
                cont.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onItemLongClickedListener.onClicked(data);
                        return true;
                    }
                });
            }
        }

        @Override
        public void unbind() {
            super.unbind();
            avatar.unbind();
        }
    }

    private class ContactView extends View {

        private final TextPaint NAME_TEXT_PAINT = new TextPaint();
        private final TextPaint FAST_TEXT_PAINT = new TextPaint();
        private final TextPaint AVATAR_TEXT_PAINT = new TextPaint();

        private final Paint DIV_PAINT = new Paint();
        private final Paint CIRCLE_BORDER_PAINT = new Paint();
        private final Paint CIRCLE_PAINT = new Paint();
        private final Paint AVATAR_PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

        private BitmapShader AVATAR_SHADER;
        private Matrix AVATAR_SHADER_MATRIX = new Matrix();

        private int[] PLACEHOLDERS;

        private int HEIGHT;

        private int AVATAR_CENTER_X;
        private int AVATAR_CENTER_Y;
        private int AVATAR_R;
        private float AVATAR_NAME_HEIGHT;
        private float AVATAR_NAME_TOP;
        private float AVATAR_NAME_WIDTH;
        private float AVATAR_NAME_LEFT;

        private int DIV_HEIGHT;
        private int DIV_LEFT;

        private float FAST_HEIGHT;
        private float FAST_WIDTH;
        private float FAST_LEFT;
        private float FAST_TOP;

        private float NAME_LEFT;
        private float NAME_HEIGHT;
        private float NAME_TOP;


        private boolean isNeedMeasure = false;
        private boolean isMeasured = false;

        private boolean isBinded = false;
        private String bindedName;
        private String bindedFastName;
        private boolean bindedShowFastName;
        private Avatar bindedAvatar;
        private BitmapReference bindedAvatarReference;
        private int bindedColor;

        private ImageReceiver receiver;

        public ContactView(Context context) {
            super(context);
            init();
        }

        public ContactView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public ContactView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            DIV_PAINT.setColor(getResources().getColor(R.color.divider));

            CIRCLE_PAINT.setStyle(Paint.Style.FILL);
            CIRCLE_PAINT.setAntiAlias(true);
            CIRCLE_PAINT.setColor(Color.LTGRAY);

            CIRCLE_BORDER_PAINT.setStyle(Paint.Style.STROKE);
            CIRCLE_BORDER_PAINT.setAntiAlias(true);
            CIRCLE_BORDER_PAINT.setColor(0x19000000);
            CIRCLE_BORDER_PAINT.setStrokeWidth(1);

            FAST_TEXT_PAINT.setColor(getResources().getColor(R.color.primary));
            FAST_TEXT_PAINT.setTypeface(Fonts.medium());
            FAST_TEXT_PAINT.setTextSize(Screen.sp(18));
            FAST_TEXT_PAINT.setAntiAlias(true);

            NAME_TEXT_PAINT.setColor(getResources().getColor(R.color.text_primary));
            NAME_TEXT_PAINT.setTypeface(Fonts.regular());
            NAME_TEXT_PAINT.setTextSize(Screen.sp(16));
            NAME_TEXT_PAINT.setAntiAlias(true);

            AVATAR_TEXT_PAINT.setColor(Color.WHITE);
            AVATAR_TEXT_PAINT.setTypeface(Fonts.regular());
            AVATAR_TEXT_PAINT.setTextSize(Screen.sp(24));
            AVATAR_TEXT_PAINT.setAntiAlias(true);

            HEIGHT = Screen.dp(64);
            AVATAR_R = Screen.dp(26);
            AVATAR_CENTER_X = Screen.dp(46) + AVATAR_R;
            AVATAR_CENTER_Y = HEIGHT / 2;

            DIV_HEIGHT = getResources().getDimensionPixelSize(R.dimen.div_size);
            DIV_LEFT = Screen.dp(112);

            FAST_HEIGHT = FAST_TEXT_PAINT.descent() - FAST_TEXT_PAINT.ascent();
            FAST_TOP = (HEIGHT - FAST_HEIGHT) / 2.0f - FAST_TEXT_PAINT.ascent();

            NAME_HEIGHT = NAME_TEXT_PAINT.descent() - NAME_TEXT_PAINT.ascent();
            NAME_TOP = (HEIGHT - NAME_HEIGHT) / 2.0f - NAME_TEXT_PAINT.ascent();
            NAME_LEFT = Screen.dp(112);

            AVATAR_NAME_HEIGHT = AVATAR_TEXT_PAINT.descent() - AVATAR_TEXT_PAINT.ascent();
            AVATAR_NAME_TOP = (HEIGHT - AVATAR_NAME_HEIGHT) / 2.0f - AVATAR_TEXT_PAINT.ascent();

            PLACEHOLDERS = new int[]{
                    getResources().getColor(R.color.placeholder_0),
                    getResources().getColor(R.color.placeholder_1),
                    getResources().getColor(R.color.placeholder_2),
                    getResources().getColor(R.color.placeholder_3),
                    getResources().getColor(R.color.placeholder_4),
                    getResources().getColor(R.color.placeholder_5),
                    getResources().getColor(R.color.placeholder_6),
            };

            receiver = core().getImageLoader().createReceiver(new ReceiverCallback() {
                @Override
                public void onImageLoaded(BitmapReference bitmap) {
                    bindedAvatarReference = bitmap.fork(this);
                    AVATAR_SHADER = new BitmapShader(bindedAvatarReference.getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    AVATAR_SHADER_MATRIX.reset();
                    AVATAR_SHADER_MATRIX.postScale(AVATAR_R * 2 / (float) bitmap.getBitmap().getWidth(),
                            AVATAR_R * 2 / (float) bitmap.getBitmap().getHeight());
                    AVATAR_SHADER.setLocalMatrix(AVATAR_SHADER_MATRIX);
                    AVATAR_PAINT.setShader(AVATAR_SHADER);
                    postInvalidate();
                }

                @Override
                public void onImageCleared() {

                }

                @Override
                public void onImageError() {

                }
            });
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), HEIGHT);
            isMeasured = true;
            if (isNeedMeasure) {
                measureName();
                isNeedMeasure = false;
            }
        }

        public void bind(int id, Avatar avatar, String name, boolean showFastName) {
            this.isBinded = true;
            this.bindedAvatar = avatar;
            this.bindedName = name;
            this.bindedShowFastName = showFastName;
            this.bindedFastName = getFastName(name);
            this.bindedColor = PLACEHOLDERS[Math.abs(id) % PLACEHOLDERS.length];

            if (bindedAvatarReference != null) {
                bindedAvatarReference.release();
                bindedAvatarReference = null;
            }
            AVATAR_SHADER = null;

            receiver.clear();

            if (avatar != null) {
                receiver.request(new AvatarTask(AVATAR_R * 2, avatar));
            }

            CIRCLE_PAINT.setColor(bindedColor);

            FAST_WIDTH = FAST_TEXT_PAINT.measureText(bindedFastName);
            FAST_LEFT = Screen.dp(6) + (Screen.dp(40) - FAST_WIDTH) / 2.0f;

            AVATAR_NAME_WIDTH = AVATAR_TEXT_PAINT.measureText(bindedFastName);
            AVATAR_NAME_LEFT = Screen.dp(46) + (AVATAR_R * 2 - AVATAR_NAME_WIDTH) / 2.0f;

            invalidateMeasure();
        }

        private void invalidateMeasure() {
            if (!isMeasured) {
                measureName();
            } else {
                isNeedMeasure = true;
                requestLayout();
            }
        }

        private void measureName() {

        }

        public void unbind() {
            this.isBinded = false;
            this.bindedAvatar = null;
            this.bindedFastName = null;
            this.bindedName = null;
            this.bindedShowFastName = false;
        }

        @Override
        protected void onDraw(Canvas canvas) {

            if (isBinded) {
                if (bindedShowFastName) {
                    canvas.drawText(bindedFastName, FAST_LEFT, FAST_TOP, FAST_TEXT_PAINT);
                }
                canvas.drawText(bindedName, NAME_LEFT, NAME_TOP, NAME_TEXT_PAINT);

                if (AVATAR_SHADER != null) {
                    canvas.save();
                    canvas.translate(AVATAR_CENTER_X - AVATAR_R, AVATAR_CENTER_Y - AVATAR_R);
                    canvas.drawCircle(AVATAR_R, AVATAR_R, AVATAR_R, AVATAR_PAINT);
                    canvas.restore();
                    canvas.drawCircle(AVATAR_CENTER_X, AVATAR_CENTER_Y, AVATAR_R, CIRCLE_BORDER_PAINT);
                } else {
                    canvas.drawCircle(AVATAR_CENTER_X, AVATAR_CENTER_Y, AVATAR_R, CIRCLE_PAINT);
                    canvas.drawText(bindedFastName, AVATAR_NAME_LEFT, AVATAR_NAME_TOP, AVATAR_TEXT_PAINT);
                }
            }

            canvas.drawRect(DIV_LEFT, HEIGHT - DIV_HEIGHT, getWidth(), HEIGHT, DIV_PAINT);
        }
    }

    private static String getFastName(String name) {
        if (name.length() > 1) {
            if (Character.isLetter(name.charAt(0))) {
                return name.substring(0, 1).toUpperCase();
            } else {
                return "#";
            }
        } else {
            return "#";
        }
    }
}
