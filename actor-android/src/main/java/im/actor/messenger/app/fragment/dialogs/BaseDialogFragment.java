package im.actor.messenger.app.fragment.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.DisplayListFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.util.Screen;
import im.actor.model.android.BindedListAdapter;
import im.actor.model.entity.Dialog;
import im.actor.model.mvvm.BindedDisplayList;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 22.11.14.
 */
public abstract class BaseDialogFragment extends DisplayListFragment<Dialog, DialogHolder> {

    public BaseDialogFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View res = inflate(inflater, container,
                R.layout.fragment_dialogs, messenger().getDialogsGlobalList());

        // Footer

        FrameLayout footer = new FrameLayout(getActivity());
        footer.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        footer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(160)));
        ImageView shadow = new ImageView(getActivity());
        shadow.setImageResource(R.drawable.card_shadow_bottom);
        shadow.setScaleType(ImageView.ScaleType.FIT_XY);
        shadow.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
        footer.addView(shadow);

        TextView hint = new TextView(getActivity());
        hint.setText(R.string.dialogs_hint);
        hint.setTypeface(Fonts.regular());
        hint.setPadding(Screen.dp(16), Screen.dp(8), Screen.dp(16), 0);
        hint.setGravity(Gravity.CENTER);
        hint.setTextSize(16);
        hint.setTextColor(getResources().getColor(R.color.text_subheader));
        hint.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footer.addView(hint);

        addFooterView(footer);

        // Header

        TextView header = new TextView(getActivity());
        header.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        header.setText(R.string.dialogs_title);
        header.setTypeface(Fonts.bold());
        header.setPadding(Screen.dp(16), 0, 0, 0);
        header.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        header.setTextSize(16);
        header.setTextColor(getResources().getColor(R.color.text_subheader));

        LinearLayout headerCont = new LinearLayout(getActivity());
        headerCont.setBackgroundColor(getResources().getColor(R.color.bg_light));
        headerCont.setOrientation(LinearLayout.VERTICAL);
        headerCont.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));
        headerCont.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addHeaderView(headerCont);

        return res;
    }

    @Override
    protected void configureRecyclerView(RecyclerView recyclerView) {
        super.configureRecyclerView(recyclerView);
        recyclerView.setItemAnimator(null);
    }

    @Override
    protected BindedListAdapter<Dialog, DialogHolder> onCreateAdapter(BindedDisplayList<Dialog> displayList, Activity activity) {
        DialogsAdapter adapter = new DialogsAdapter(displayList, activity);
        adapter.setItemClicked(new OnItemClickedListener<Dialog>() {
            @Override
            public void onClicked(Dialog item) {
                onItemClick(item);
            }

            @Override
            public boolean onLongClicked(Dialog item) {
                return false;
            }
        });
        if (supportLongClick()) {
            adapter.setItemLongClicked(new OnItemClickedListener<Dialog>() {
                @Override
                public void onClicked(Dialog item) {
                    onItemLongClick(item);
                }

                @Override
                public boolean onLongClicked(Dialog item) {
                    return false;
                }
            });
        }
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        messenger().onDialogsOpen();
    }

    protected boolean supportLongClick() {
        return false;
    }

    protected void onItemClick(Dialog item) {

    }

    protected void onItemLongClick(final Dialog dialog) {

    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().onDialogsClosed();
    }
}
