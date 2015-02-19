package im.actor.messenger.app.fragment.contacts;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.HolderAdapter;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.util.Screen;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 04.11.14.
 */
public class ContactSearchAdapter extends HolderAdapter<UserVM> {

    private UserVM[] result = new UserVM[0];

    public ContactSearchAdapter(Context context) {
        super(context);
    }

    public void updateResults(UserVM[] results) {
        this.result = results;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public UserVM getItem(int position) {
        return result[position];
    }

    @Override
    public long getItemId(int position) {
        return result[position].getId();
    }

    @Override
    protected ViewHolder<UserVM> createHolder(UserVM obj) {
        return new Holder();
    }

    private class Holder extends ViewHolder<UserVM> {

        private AvatarView avatar;
        private TextView title;

        @Override
        public View init(UserVM data, ViewGroup viewGroup, Context context) {
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
                layoutParams.topMargin = Screen.dp(10);
                layoutParams.bottomMargin = Screen.dp(10);
                fl.addView(title, layoutParams);
            }

            return fl;
        }

        @Override
        public void bind(UserVM data, int position, Context context) {
            avatar.setEmptyDrawable(AvatarDrawable.create(data, 24, context));
            if (data.getAvatar().get() != null) {
                avatar.bindAvatar(24, data.getAvatar().get());
            } else {
                avatar.unbind();
            }
            title.setText(data.getName().get());
        }
    }
}
