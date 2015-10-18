package im.actor.sdk.controllers.fragment.media;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.DocumentContent;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.controllers.fragment.DisplayListFragment;
import im.actor.sdk.controllers.conversation.messages.ChatListProcessor;
import im.actor.sdk.controllers.conversation.messages.DocHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by badgr on 01.09.2015.
 */
public class DocumentsFragment extends MessagesFragment {

    private Peer peer;

    public DocumentsFragment(Peer peer) {
        this.peer = peer;
        Bundle bundle = new Bundle();
        bundle.putByteArray("EXTRA_PEER", peer.toByteArray());
        setArguments(bundle);
    }

    public DocumentsFragment() {

    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    protected BindedDisplayList<Message> onCreateDisplayList() {
        BindedDisplayList<Message> res = messenger().getDocsDisplayList(peer);
        if (res.getListProcessor() == null) {
            res.setListProcessor(new ChatListProcessor(this));
        }
        return res;
    }

    @Override
    protected void configureRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        final ChatLinearLayoutManager linearLayoutManager = new ChatLinearLayoutManager(getActivity(), ChatLinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void bindDisplayListLoad() {

    }

    @Override
    protected BindedListAdapter<Message, MessageHolder> onCreateAdapter(BindedDisplayList<Message> displayList, Activity activity) {
        messagesAdapter = new DocumentsAdapter(displayList, this, activity);
        return messagesAdapter;
    }
}
