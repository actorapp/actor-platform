package im.actor.sdk.controllers.conversation.messages;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.Log;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.content.AudioHolder;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.ChatListProcessor;
import im.actor.sdk.controllers.DisplayListFragment;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.util.Screen;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.BindedDisplayList;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public abstract class MessagesFragment extends DisplayListFragment<Message, AbsMessageViewHolder> {

    private final boolean isPrimaryMode;

    protected Peer peer;
    protected ConversationVM conversationVM;

    protected ChatLinearLayoutManager layoutManager;
    protected MessagesAdapter messagesAdapter;
    protected CircularProgressBar progressView;
    private long firstUnread = -1;
    private boolean isUnreadLoaded = false;
    private boolean reloaded;
    private NewMessageListener newMessageListener;


    //
    // Constructor and getters
    //
    public MessagesFragment(boolean isPrimaryMode) {
        this.isPrimaryMode = isPrimaryMode;
        setUnbindOnPause(true);
    }

    public Peer getPeer() {
        return peer;
    }

    public ConversationVM getConversationVM() {
        return conversationVM;
    }

    //
    // View
    //
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //
        // Loading arguments
        //
        try {
            peer = Peer.fromBytes(getArguments().getByteArray("EXTRA_PEER"));
            conversationVM = messenger().getConversationVM(peer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //
        // Display List
        //
        BindedDisplayList<Message> displayList = onCreateDisplayList();
        if (isPrimaryMode) {
            displayList.setLinearLayoutCallback(b -> {
                if (layoutManager != null) {
                    layoutManager.setStackFromEnd(b);
                }
            });
        }


        //
        // Main View
        //
        View res = inflate(inflater, container, R.layout.fragment_messages, displayList);
        progressView = (CircularProgressBar) res.findViewById(R.id.loadingProgress);
        progressView.setIndeterminate(true);
        progressView.setVisibility(View.INVISIBLE);

        //
        // Loading background
        //
        Drawable background;
        int[] backgrounds = ActorSDK.sharedActor().style.getDefaultBackgrouds();
        String selectedWallpaper = messenger().getSelectedWallpaper();
        if (selectedWallpaper != null) {
            background = getResources().getDrawable(backgrounds[0]);
            if (selectedWallpaper.startsWith("local:")) {
                for (int i = 1; i < backgrounds.length; i++) {
                    if (getResources().getResourceEntryName(backgrounds[i]).equals(selectedWallpaper.replaceAll("local:", ""))) {
                        background = getResources().getDrawable(backgrounds[i]);
                    }
                }
            } else {
                background = Drawable.createFromPath(BaseActorSettingsFragment.getWallpaperFile());
            }
        } else {
            background = getResources().getDrawable(backgrounds[0]);
        }
        ((ImageView) res.findViewById(R.id.chatBackgroundView)).setImageDrawable(background);


        //
        // List Padding
        //
        View footer = new View(getActivity());
        footer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(8)));
        addHeaderView(footer); // Add Footer as Header because of reverse layout

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64)));
        addFooterView(header); // Add Header as Footer because of reverse layout


        //
        // Init unread message index if available
        //
        recalculateUnreadMessageIfNeeded();

        return res;
    }

    //
    // Configure RecyclerView
    //
    @Override
    protected BindedListAdapter<Message, AbsMessageViewHolder> onCreateAdapter(BindedDisplayList<Message> displayList, Activity activity) {
        messagesAdapter = new MessagesAdapter(displayList, this, activity);
        if (firstUnread != -1 && messagesAdapter.getFirstUnread() == -1) {
            messagesAdapter.setFirstUnread(firstUnread);
        }
        return messagesAdapter;
    }

    @Override
    protected void configureRecyclerView(RecyclerView recyclerView) {
        layoutManager = new ChatLinearLayoutManager(getActivity(), ChatLinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    protected BindedDisplayList<Message> onCreateDisplayList() {
        BindedDisplayList<Message> displayList = messenger().getMessageDisplayList(peer);
        if (displayList.getListProcessor() == null) {
            displayList.setListProcessor(new ChatListProcessor(peer, this.getContext()));
        }
        notifyNewMessage(displayList);
        return displayList;
    }


    //
    // Unread Messages
    //
    private void recalculateUnreadMessageIfNeeded() {

        Log.d("READ_DEBUG", "trying to scroll to unread");

        // Scroll to unread only in primary mode
        if (!isPrimaryMode) {
            return;
        }

        BindedDisplayList<Message> list = getDisplayList();
        if (firstUnread == -1) {
            firstUnread = conversationVM.getLastReadMessageDate();
        }

        // Do not scroll to unread twice
        if (isUnreadLoaded) {
            return;
        }

        // Ignore if list is not loaded
        if (list.getSize() == 0) {
            return;
        }

        // refresh list if top message is too old
        if (getLastMessage(getDisplayList()).getSortDate() < firstUnread && !reloaded) {
            reloaded = true;
            getDisplayList().initCenter(firstUnread, true);
            return;
        }

        // If List is not empty: mark as loaded
        isUnreadLoaded = true;

        // If don't have unread message date: nothing to do
        if (firstUnread <= 0) {
            return;
        }

        // Searching for first unread message
        int index = -1;
        long unread = -1;
        for (int i = list.getSize() - 1; i >= 0; i--) {
            Message message = list.getItem(i);
            if (message.getSenderId() == messenger().myUid()) {
                continue;
            }
            if (message.getSortDate() > firstUnread) {
                index = i;
                unread = message.getRid();
                break;
            }
        }

        // If have some unread messages: scroll to it
        if (index >= 0) {
            scrollToUnread(unread, index);
        } else {
            scrollToUnread(0, 0);
        }
    }

    private void scrollToUnread(long unreadId, final int index) {
        if (messagesAdapter != null) {
            messagesAdapter.setFirstUnread(unreadId);
        }
        if (getCollection() != null) {
            if (index > 0 && layoutManager != null) {
                layoutManager.setStackFromEnd(false);
                layoutManager.scrollToPositionWithOffset(index + 1, Screen.dp(64));
            } else {
                getCollection().scrollToPosition(0);
            }
        }
    }

    //
    // Callback
    //
    public void onAvatarClick(int uid) {

    }

    public void onAvatarLongClick(int uid) {

    }

    public boolean onClick(Message message) {
        return false;
    }

    public boolean onLongClick(final Message message, final boolean hasMyReaction) {
        return false;
    }


    //
    // Lifecycle
    //
    @Override
    public void onResume() {
        super.onResume();
        // Mark messages as read only when scroll to
        if (isPrimaryMode) {
            messenger().onConversationOpen(peer);
        }

        // Bind Progress
        bind(conversationVM.getIsLoaded(), conversationVM.getIsEmpty(), (isLoaded, valueModel, isEmpty, valueModel2) -> {
            if (isEmpty && !isLoaded) {
                showView(progressView);
            } else {
                hideView(progressView);
            }
        });
    }

    @Override
    public void onCollectionChanged() {
        super.onCollectionChanged();
        recalculateUnreadMessageIfNeeded();
        notifyNewMessage(getDisplayList());
    }

    protected void notifyNewMessage(BindedDisplayList<Message> displayList) {
        if (newMessageListener != null && displayList.getSize() > 0) {
            newMessageListener.onNewMessage(getLastMessage(displayList));
        }
    }

    public Message getLastMessage(BindedDisplayList<Message> displayList) {
        return displayList.getItem(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Mark messages as read only when scroll to
        if (isPrimaryMode) {
            messenger().onConversationClosed(peer);
        }
        AudioHolder.stopPlaying();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesAdapter != null) {
            messagesAdapter.getBinder().unbindAll();
            messagesAdapter = null;
        }
    }

    public void setNewMessageListener(NewMessageListener newMessageListener) {
        this.newMessageListener = newMessageListener;
    }

    public interface NewMessageListener {
        void onNewMessage(Message m);
    }
}
