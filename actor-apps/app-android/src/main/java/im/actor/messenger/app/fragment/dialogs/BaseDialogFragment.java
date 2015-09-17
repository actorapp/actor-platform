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
import android.widget.TextView;

import im.actor.core.entity.Dialog;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.DisplayListFragment;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.messenger.app.core.Core.messenger;

public abstract class BaseDialogFragment extends DisplayListFragment<Dialog, DialogHolder> {

    private View emptyDialogs;

    private String joinGroupUrl;

    public BaseDialogFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            joinGroupUrl = getArguments().getString("invite_url", null);
        }

        View res = inflate(inflater, container, R.layout.fragment_dialogs,
                messenger().getDialogsDisplayList());

        // setAnimationsEnabled(true);

        // Footer

        FrameLayout footer = new FrameLayout(getActivity());
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
        hint.setTextSize(15);
        hint.setTextColor(getResources().getColor(R.color.text_subheader));
        hint.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footer.addView(hint);

        addFooterView(footer);

        // Header

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(0)));
        header.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_main));
        addHeaderView(header);

        // Empty View
        emptyDialogs = res.findViewById(R.id.emptyDialogs);
        bind(messenger().getAppState().getIsDialogsEmpty(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, Value<Boolean> Value) {
                if (val) {
                    emptyDialogs.setVisibility(View.VISIBLE);
                } else {
                    emptyDialogs.setVisibility(View.GONE);
                }
            }
        });

        return res;
    }

    @Override
    protected BindedListAdapter<Dialog, DialogHolder> onCreateAdapter(BindedDisplayList<Dialog> displayList, Activity activity) {
        return new DialogsAdapter(displayList, new OnItemClickedListener<Dialog>() {
            @Override
            public void onClicked(Dialog item) {
                onItemClick(item);
            }

            @Override
            public boolean onLongClicked(Dialog item) {
                return onItemLongClick(item);
            }
        }, activity);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (joinGroupUrl != null && !joinGroupUrl.isEmpty()) {
            String[] urlSplit = null;
            if (joinGroupUrl.contains("join")) {
                urlSplit = joinGroupUrl.split("/join/");
            } else if (joinGroupUrl.contains("token")) {
                urlSplit = joinGroupUrl.split("token=");
            }
            if (urlSplit != null) {
                joinGroupUrl = urlSplit[urlSplit.length - 1];
                execute(messenger().joinGroupViaLink(joinGroupUrl), R.string.invite_link_title, new CommandCallback<Integer>() {
                    @Override
                    public void onResult(Integer res) {
                        getActivity().startActivity(Intents.openGroupDialog(res, true, getActivity()));
                        getActivity().finish();
                        joinGroupUrl = "";
                    }

                    @Override
                    public void onError(Exception e) {
                        joinGroupUrl = "";
                    }
                });
            }
        }
        messenger().onDialogsOpen();
    }

    protected void onItemClick(Dialog item) {

    }

    protected boolean onItemLongClick(final Dialog dialog) {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().onDialogsClosed();
    }
}