package im.actor.allmessages;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.R;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.fragment.DisplayListFragment;
import im.actor.sdk.view.adapters.OnItemClickedListener;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AllMessagesFragment extends DisplayListFragment<MessageEx, AllMessageHolder>{
    @Override
    protected BindedListAdapter<MessageEx, AllMessageHolder> onCreateAdapter(BindedDisplayList<MessageEx> displayList, Activity activity) {
        return new AllMessagesAdapter(displayList, new OnItemClickedListener<MessageEx>() {
            @Override
            public void onClicked(MessageEx item) {

            }

            @Override
            public boolean onLongClicked(MessageEx item) {
                return false;
            }
        }, activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflate(inflater, container, R.layout.fragment_all_messages,
                messenger().getCustomDisplayList(new Peer(PeerType.PRIVATE, 1), "favorite", MessageEx.CREATOR));
        res.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());

        return res;
    }

    @Override
    public void onCollectionChanged() {
        super.onCollectionChanged();
        if(scrolledTOTop){
            if(recyclerView!=null){
                recyclerView.scrollToPosition(0);
            }
        }
    }

    private boolean scrolledTOTop = true;
    RecyclerView recyclerView;
    @Override
    protected void configureRecyclerView(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrolledTOTop = linearLayoutManager.findFirstCompletelyVisibleItemPosition() ==0;
            }
        });
    }
}