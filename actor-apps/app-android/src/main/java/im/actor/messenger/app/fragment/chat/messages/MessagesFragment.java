package im.actor.messenger.app.fragment.chat.messages;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.fragment.DisplayListFragment;
import im.actor.messenger.app.fragment.chat.ChatActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.AndroidListUpdate;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MessagesFragment extends DisplayListFragment<Message, MessageHolder> {

    public static MessagesFragment create(Peer peer) {
        return new MessagesFragment(peer);
    }

    private Peer peer;

    private ChatLinearLayoutManager linearLayoutManager;
    protected MessagesAdapter messagesAdapter;
    // private ConversationVM conversationVM;
    private ActionMode actionMode;
    private int onPauseSize = 0;

    public MessagesFragment(Peer peer) {
        this.peer = peer;
        Bundle bundle = new Bundle();
        bundle.putByteArray("EXTRA_PEER", peer.toByteArray());
        setArguments(bundle);
    }

    public MessagesFragment() {

    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            peer = Peer.fromBytes(getArguments().getByteArray("EXTRA_PEER"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        View res = inflate(inflater, container, R.layout.fragment_messages, onCreateDisplayList());

        View footer = new View(getActivity());
        footer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(8)));
        // Add Footer as Header because of reverse layout
        addHeaderView(footer);

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(64)));
        // Add Header as Footer because of reverse layout
        addFooterView(header);

        bindDisplayListLoad();

        return res;
    }

    protected BindedDisplayList<Message> onCreateDisplayList() {
        BindedDisplayList<Message> res = messenger().getMessageDisplayList(peer);
        if (res.getListProcessor() == null) {
            res.setListProcessor(new ChatListProcessor(this));
        }

        return res;
    }

    private boolean isLoaded = false;

    protected void bindDisplayListLoad() {
        final BindedDisplayList<Message> list = getDisplayList();
        DisplayList.AndroidChangeListener<Message> listener = new DisplayList.AndroidChangeListener<Message>() {


            @Override
            public void onCollectionChanged(AndroidListUpdate<Message> modification) {
                ondisplayListLoaded();
            }


        };
        list.addAndroidListener(listener);
        ondisplayListLoaded();
    }

    private void ondisplayListLoaded() {
        final BindedDisplayList<Message> list = getDisplayList();
        if (isLoaded) {
            return;
        }

        if (list.getSize() == 0) {
            return;
        }

        isLoaded = true;
        //long lastRead = modules.getMessagesModule().loadReadState(peer);
        long firstUnread = messenger().loadFirstUnread(peer);

        if (firstUnread == 0) {
            // Already scrolled to bottom
            return;
        }

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

        if (index > 0) {
            if (linearLayoutManager != null) {
                linearLayoutManager.setStackFromEnd(false);
                linearLayoutManager.scrollToPositionWithOffset(index + 1, Screen.dp(64));
                // linearLayoutManager.scrollToPosition(getDisplayList().getSize() - index - 1);
                // linearLayoutManager.scrollToPosition(index + 1);
                // getCollection().scrollToPosition(index + 1);
            }

        } else if (getCollection() != null) {
            // linearLayoutManager.scrollToPosition(0);
            getCollection().scrollToPosition(0);
        }
    }

    @Override
    protected BindedListAdapter<Message, MessageHolder> onCreateAdapter(BindedDisplayList<Message> displayList, Activity activity) {
        messagesAdapter = new MessagesAdapter(displayList, this, activity);
        return messagesAdapter;
    }

    @Override
    protected void configureRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new ChatLinearLayoutManager(getActivity(), ChatLinearLayoutManager.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        getDisplayList().setLinearLayoutCallback(new BindedDisplayList.LinearLayoutCallback() {
            @Override
            public void setStackFromEnd(boolean b) {
                if (linearLayoutManager != null) linearLayoutManager.setStackFromEnd(b);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onPauseSize != 0 && getDisplayList().getSize() != onPauseSize) bindDisplayListLoad();
        messenger().onConversationOpen(peer);
    }

    public void onAvatarClick(int uid) {
        startActivity(Intents.openProfile(uid, getActivity()));
    }

    public void onAvatarLongClick(int uid) {
        ((ChatActivity) getActivity()).insertMention(uid);
    }

    public boolean onClick(Message message) {
        if (actionMode != null) {
            if (messagesAdapter.isSelected(message)) {
                messagesAdapter.setSelected(message, false);
                if (messagesAdapter.getSelectedCount() == 0) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    actionMode.invalidate();
                }
            } else {
                messagesAdapter.setSelected(message, true);
                actionMode.invalidate();
            }
            return true;
        }
        return false;
    }

    public boolean onLongClick(Message message) {
        if (actionMode == null) {
            messagesAdapter.clearSelection();
            messagesAdapter.setSelected(message, true);
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.messages_context, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    Message[] selected = messagesAdapter.getSelected();
                    if (selected.length > 0) {
                        actionMode.setTitle("" + selected.length);
                    }

                    boolean isAllText = true;

                    for (Message k : selected) {
                        if (!(k.getContent() instanceof TextContent)) {
                            isAllText = false;
                            break;
                        }
                    }

                    menu.findItem(R.id.copy).setVisible(isAllText);
                    menu.findItem(R.id.quote).setVisible(isAllText);
                    menu.findItem(R.id.forward).setVisible(selected.length == 1 || isAllText);
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.delete) {
                        Message[] selected = messagesAdapter.getSelected();
                        final long[] rids = new long[selected.length];
                        for (int i = 0; i < rids.length; i++) {
                            rids[i] = selected[i].getRid();
                        }

                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.alert_delete_messages_text)
                                        .replace("{0}", "" + rids.length))
                                .setPositiveButton(R.string.alert_delete_messages_yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                messenger().deleteMessages(peer, rids);
                                                actionMode.finish();
                                            }
                                        })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show()
                                .setCanceledOnTouchOutside(true);
                        return true;
                    } else if (menuItem.getItemId() == R.id.copy) {
                        String text = messenger().getFormatter().formatMessagesExport(messagesAdapter.getSelected());
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Messages", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.toast_messages_copied, Toast.LENGTH_SHORT).show();
                        actionMode.finish();
                        return true;
                    } else if (menuItem.getItemId() == R.id.quote) {
                        String quote = "";
                        String rawQuote = "";
                        int i = 0;
                        for (Message m : messagesAdapter.getSelected()) {
                            if (m.getContent() instanceof TextContent) {
                                UserVM user = users().get(m.getSenderId());
                                String nick = user.getNick().get();
                                String name = (nick != null && !nick.isEmpty()) ? "@".concat(nick) : user.getName().get();
                                String text = ((TextContent) m.getContent()).getText();
                                quote = quote.concat(name).concat(": ").concat(text);
                                rawQuote = rawQuote.concat(name).concat(": ").concat(text).concat("\n");
                                if (i++ != messagesAdapter.getSelectedCount() - 1) {
                                    quote += ";\n";
                                } else {
                                    quote += "\n";
                                }
                            }
                        }
                        ((ChatActivity) getActivity()).addQuote(quote, rawQuote);
                        actionMode.finish();
                        return true;

                    } else if (menuItem.getItemId() == R.id.forward) {
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        if (messagesAdapter.getSelected().length == 1) {
                            Message m = messagesAdapter.getSelected()[0];
                            if (m.getContent() instanceof TextContent) {
                                UserVM user = users().get(m.getSenderId());
                                String nick = user.getNick().get();
                                String name = (nick != null && !nick.isEmpty()) ? "@".concat(nick) : user.getName().get();
                                String text = ((TextContent) m.getContent()).getText();
                                String forward = name.concat(": ").concat(text).concat("\n");
                                i.putExtra("forward_text", forward);
                                i.putExtra("forward_text_raw", forward);
                            } else if (m.getContent() instanceof DocumentContent) {
                                boolean isDoc = !(m.getContent() instanceof PhotoContent || m.getContent() instanceof VideoContent);
                                DocumentContent fileMessage = (DocumentContent) m.getContent();
                                if (fileMessage.getSource() instanceof FileRemoteSource) {
                                    i.putExtra("forward_doc_descriptor", messenger().findDownloadedDescriptor(((FileRemoteSource) fileMessage.getSource()).getFileReference().getFileId()));
                                } else if (fileMessage.getSource() instanceof FileLocalSource) {
                                    String descriptor = ((FileLocalSource) fileMessage.getSource()).getFileDescriptor();
                                    i.putExtra("forward_doc_descriptor", descriptor);
                                }
                                i.putExtra("forward_doc_is_doc", isDoc);
                            }
                        } else {
                            String quote = "";
                            String rawQuote = "";
                            int j = 0;
                            for (Message m : messagesAdapter.getSelected()) {
                                if (m.getContent() instanceof TextContent) {
                                    UserVM user = users().get(m.getSenderId());
                                    String nick = user.getNick().get();
                                    String name = (nick != null && !nick.isEmpty()) ? "@".concat(nick) : user.getName().get();
                                    String text = ((TextContent) m.getContent()).getText();
                                    quote = quote.concat(name).concat(": ").concat(text);
                                    rawQuote = rawQuote.concat(name).concat(": ").concat(text).concat("\n");
                                    if (j++ != messagesAdapter.getSelectedCount() - 1) {
                                        quote += ";\n";
                                    } else {
                                        quote += "\n";
                                    }
                                }
                            }
                            i.putExtra("forward_text", quote);
                            i.putExtra("forward_text_raw", rawQuote);
                        }
                        actionMode.finish();
                        startActivity(i);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    MessagesFragment.this.actionMode = null;
                    messagesAdapter.clearSelection();
                }
            });
        } else {
            if (messagesAdapter.isSelected(message)) {
                messagesAdapter.setSelected(message, false);
                if (messagesAdapter.getSelectedCount() == 0) {
                    actionMode.finish();
                    actionMode = null;
                } else {
                    actionMode.invalidate();
                }
            } else {
                messagesAdapter.setSelected(message, true);
                actionMode.invalidate();
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        onPauseSize = new Integer(getDisplayList().getSize());
        messenger().onConversationClosed(peer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (conversationVM != null) {
//            conversationVM.release();
//            conversationVM = null;
//        }
        messagesAdapter = null;
        linearLayoutManager = null;
        linearLayoutManager = null;
    }
}
