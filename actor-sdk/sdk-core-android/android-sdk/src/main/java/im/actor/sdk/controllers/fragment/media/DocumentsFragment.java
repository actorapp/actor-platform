package im.actor.sdk.controllers.fragment.media;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.messages.preprocessor.ChatListProcessor;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.MessagesFragment;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

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
            res.setListProcessor(new ChatListProcessor(peer, getContext()));
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
