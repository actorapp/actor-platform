package im.actor.sdk.controllers.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.core.entity.Dialog;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.dialogs.view.DialogHolder;
import im.actor.sdk.controllers.dialogs.view.DialogsAdapter;
import im.actor.sdk.controllers.DisplayListFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.runtime.android.view.BindedListAdapter;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public abstract class BaseDialogFragment extends DisplayListFragment<Dialog, DialogHolder> {

    private View emptyDialogs;

    public BaseDialogFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        BindedDisplayList<Dialog> displayList = messenger().getDialogsDisplayList();
        if (displayList.getListProcessor() == null) {
            displayList.setListProcessor((items, previous) -> {
                for (Dialog d : items) {
                    if (d.getSenderId() != 0) {
                        users().get(d.getSenderId());
                    }
                }
                return null;
            });
        }

        View res = inflate(inflater, container, R.layout.fragment_dialogs, displayList);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        // Footer

        FrameLayout footer = new FrameLayout(getActivity());
        footer.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(160)));
        footer.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        addFooterView(footer);

        // Header

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(ActorSDK.sharedActor().style.getDialogsPaddingTopDp())));
        header.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        addHeaderView(header);

        // Empty View
        emptyDialogs = res.findViewById(R.id.emptyDialogs);
        bind(messenger().getAppState().getIsDialogsEmpty(), (val, Value) -> {
            if (val) {
                emptyDialogs.setVisibility(View.VISIBLE);
            } else {
                emptyDialogs.setVisibility(View.GONE);
            }
        });
        ((TextView) res.findViewById(R.id.add_contact_hint_text)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        ((TextView) emptyDialogs.findViewById(R.id.empty_dialogs_text)).setTextColor(ActorSDK.sharedActor().style.getMainColor());
        emptyDialogs.findViewById(R.id.empty_dialogs_bg).setBackgroundColor(ActorSDK.sharedActor().style.getMainColor());

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