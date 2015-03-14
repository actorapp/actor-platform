package im.actor.messenger.app.fragment.search;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;
import com.droidkit.engine.uilist.UiListListener;

import im.actor.messenger.R;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.SearchHighlight;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.storage.GlobalSearch;
import im.actor.messenger.util.Screen;

/**
 * Created by ex3ndr on 19.09.14.
 */
public class SearchAdapter extends BaseAdapter {
    private UiListListener listListener;
    private UiList<GlobalSearch> uiList;
    private Context context;

    private int padding = Screen.dp(10);
    private String query = "";

    public SearchAdapter(UiList<GlobalSearch> uiList, Context context) {
        this.uiList = uiList;
        this.context = context;
        this.listListener = new UiListListener() {
            @Override
            public void onListUpdated() {
                notifyDataSetChanged();
            }
        };
        uiList.addListener(listListener);
    }

    public void setQuery(String query) {
        this.query = query;
        notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return uiList.getSize();
    }

    @Override
    public GlobalSearch getItem(int position) {
        return uiList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        GlobalSearch search = getItem(position);
        // return DialogUids.getDialogUid(search.getContType(), search.getContId());
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GlobalSearch object = getItem(position);

        ViewHolder<GlobalSearch> holder;

        View view;
        if (convertView == null || convertView.getTag() == null) {
            holder = new Holder();
            view = holder.init(object, parent, context);
            view.setTag(holder);
        } else {
            holder = (ViewHolder<GlobalSearch>) convertView.getTag();
            view = convertView;
        }

        holder.bind(object, position, context);

        return view;
    }

    public void close() {

    }

    private class Holder extends ViewHolder<GlobalSearch> {

        private AvatarView avatar;
        private TextView title;

        @Override
        public View init(GlobalSearch data, ViewGroup viewGroup, Context context) {
            FrameLayout fl = new FrameLayout(context);

            avatar = new AvatarView(context);
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Screen.dp(52), Screen.dp(52));
                layoutParams.leftMargin = Screen.dp(12);
                layoutParams.topMargin = Screen.dp(6);
                layoutParams.bottomMargin = Screen.dp(6);
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                fl.addView(avatar, layoutParams);
            }

            title = new TextView(context);
            title.setTextColor(context.getResources().getColor(R.color.text_primary));
            title.setPadding(Screen.dp(74), 0, Screen.dp(12), 0);
            title.setTextSize(16);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setTypeface(Fonts.load(context, "Regular"));
            {
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_VERTICAL;
                layoutParams.topMargin = padding;
                layoutParams.bottomMargin = padding;
                fl.addView(title, layoutParams);
            }

            return fl;
        }

        @Override
        public void bind(GlobalSearch data, int position, Context context) {
            avatar.setEmptyDrawable(AvatarDrawable.create(data, 24, context));
            if (data.getAvatar() != null) {
                avatar.bindAvatar(24, data.getAvatar());
            } else {
                avatar.unbind();
            }
            title.setText(SearchHighlight.highlightQuery(data.getTitle(), query, 0xff0277bd));
//            if (data.getContType() == DialogType.TYPE_GROUP) {
//                title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dialogs_group, 0, 0, 0);
//            } else {
//                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
//            }
        }
    }
}
