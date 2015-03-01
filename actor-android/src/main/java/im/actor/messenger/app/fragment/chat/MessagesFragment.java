package im.actor.messenger.app.fragment.chat;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.engine.list.view.EngineUiList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.fragment.chat.recycler.RecyclerMessagesAdapter;
import im.actor.messenger.app.view.BackgroundView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.util.Screen;
import im.actor.messenger.util.VisibleViewItem;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.TextContent;

import static im.actor.messenger.core.Core.messenger;
import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.users;

/**
 * Created by ex3ndr on 01.09.14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MessagesFragment extends BaseFragment {

    public static MessagesFragment create(Peer peer) {
        Bundle bundle = new Bundle();
        bundle.putLong("CHAT_PEER", peer.getUnuqueId());
        MessagesFragment res = new MessagesFragment();
        res.setArguments(bundle);
        return res;
    }

    private static final int STATE_BOTTOM = 0;
    private static final int STATE_TOP = 1;
    private static final int STATE_UNREAD = 2;

    private Peer peer;

    private RecyclerView messagesView;
    private RecyclerMessagesAdapter messagesAdapter;
    // private ChatAdapter adapter;
    private EngineUiList<Message> listEngine;
    private ImageView scrollUp;
    private ImageView scrollDown;
    private TextView scrollNewMessage;

    private BackgroundView bg;

    // List bind temp data
    private VisibleViewItem[] preDump;
    private int preSize;
    private long bottomId;

    private int state = STATE_UNREAD;

    private boolean isScrolledToEnd = true;
    private boolean showNewMessage = false;
    private long newMessageId = 0;

    private ActionMode actionMode;

    private HashMap<Long, Message> selected = new HashMap<Long, Message>();

    private long firstUnread = 0;

    private boolean isFirst = true;

    public long getFirstUnread() {
        return firstUnread;
    }

    public EngineUiList<Message> getListEngine() {
        return listEngine;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Building model

        peer = Peer.fromUniqueId(getArguments().getLong("CHAT_PEER"));
        listEngine = ListEngines.getMessagesList(peer);

        // Building view

        scrollNewMessage = new TextView(getActivity());
        scrollNewMessage.setTextColor(Color.WHITE);
        scrollNewMessage.setText(R.string.chat_new_messages);
        scrollNewMessage.setBackgroundResource(R.drawable.conv_scroll_down_new);
        scrollNewMessage.setTypeface(Fonts.load(getActivity(), "Regular"));
        scrollNewMessage.setTextSize(14);
//        scrollNewMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (state != STATE_BOTTOM) {
//                    state = STATE_BOTTOM;
//                    listEngine.scrollToStart();
//                } else {
//                    for (int i = 0; i < list.getSize(); i++) {
//                        if (list.getItem(i).getRid() == newMessageId) {
//                            listView.setSelectionFromTop(list.getSize() - i - 1, Screen.dp(48));
//                            break;
//                        }
//                    }
//                    newMessageId = 0;
//                    if (showNewMessage) {
//                        showNewMessage = false;
//                        goneView(scrollNewMessage);
//                    }
//                }
//            }
//        });
        scrollNewMessage.setVisibility(View.GONE);

        scrollUp = new ImageView(getActivity());
        scrollUp.setImageResource(R.drawable.conv_scroll_up);
//        scrollUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                state = STATE_TOP;
//                listEngine.scrollToEnd();
//            }
//        });

        scrollDown = new ImageView(getActivity());
        scrollDown.setImageResource(R.drawable.conv_scroll_down);
//        scrollDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                state = STATE_BOTTOM;
//                listEngine.scrollToStart();
//            }
//        });

        messagesView = new RecyclerView(getActivity());
        messagesView.setVerticalScrollBarEnabled(true);
        messagesView.setHorizontalScrollBarEnabled(false);
        messagesView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(true);
        messagesView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new RecyclerMessagesAdapter(this);
        messagesView.setAdapter(messagesAdapter);

        // adapter = new ChatAdapter(peer, this, getActivity());
//        View view = new View(getActivity());
//        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(10)));
//        listView.addFooterView(view, null, false);
//
//        FrameLayout header = new FrameLayout(getActivity());
//        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        header.setPadding(0, 0, 0, Screen.dp(8));
//        final TextView addContact = new TextView(getActivity());
//        addContact.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));
//        addContact.setText(R.string.chat_add_to_contacts);
//        addContact.setAllCaps(true);
//        addContact.setTextColor(getResources().getColor(R.color.text_primary_light));
//        addContact.setTextSize(14);
//        addContact.setTypeface(Fonts.medium());
//        addContact.setGravity(Gravity.CENTER);
//        addContact.setBackgroundResource(R.drawable.selector_add);
//        addContact.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                execute(messenger().addContact(peer.getPeerId()), R.string.chat_add_to_contacts_progress, new CommandCallback<Boolean>() {
//                    @Override
//                    public void onResult(Boolean res) {
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//
//                    }
//                });
//            }
//        });
//
//        if (peer.getPeerType() == PeerType.PRIVATE) {
//            UserVM user = users().get(peer.getPeerId());
//            bind(user.isContact(), new ValueChangedListener<Boolean>() {
//                @Override
//                public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
//                    if (val) {
//                        addContact.setVisibility(View.GONE);
//                    } else {
//                        addContact.setVisibility(View.VISIBLE);
//                    }
//                }
//            });
//        } else {
//            addContact.setVisibility(View.GONE);
//        }
//
//        header.addView(addContact);
//        listView.addHeaderView(header, null, false);

        bg = new BackgroundView(getActivity());

        FrameLayout rootLayout = new FrameLayout(getActivity());

        rootLayout.addView(bg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        rootLayout.addView(messagesView,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT | Gravity.TOP);
            params.rightMargin = Screen.dp(8);
            params.topMargin = Screen.dp(8);
            rootLayout.addView(scrollUp, params);
        }
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT | Gravity.BOTTOM);
            params.rightMargin = Screen.dp(8);
            params.bottomMargin = Screen.dp(8);
            rootLayout.addView(scrollDown, params);
        }

        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT | Gravity.BOTTOM);
            params.rightMargin = Screen.dp(8);
            params.bottomMargin = Screen.dp(8);
            rootLayout.addView(scrollNewMessage, params);
        }
        return rootLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isFirst) {
            isFirst = false;
            initUnreadLocation();
        }

        scrollUp.setVisibility(View.GONE);
        scrollDown.setVisibility(View.GONE);
    }

    private void initUnreadLocation() {

//        if (list.getSize() == 0) {
//            return;
//        }
//
//        long lastRead = messenger().loadLastReadSortDate(peer);
//        if (lastRead == 0) {
//            listView.setSelectionFromTop(list.getSize() - 1, -10000);
//            return;
//        }
//
//        int index = -1;
//
//        for (int i = list.getSize() - 1; i >= 0; i--) {
//            Message messageModel = list.getItem(i);
//            if (messageModel.getSenderId() == myUid()) {
//                continue;
//            }
//            if (messageModel.getSortDate() > lastRead) {
//                firstUnread = messageModel.getRid();
//                index = i;
//                break;
//            }
//        }
//
//        if (index >= 0) {
//            listView.setSelectionFromTop(list.getSize() - index, Screen.dp(48));
//        } else {
//            listView.setSelectionFromTop(list.getSize(), -10000);
//        }
    }

    public boolean onClick(Message messageModel) {
        if (actionMode != null) {
            if (!selected.containsKey(messageModel.getRid())) {
                selected.put(messageModel.getRid(), messageModel);
            } else {
                selected.remove(messageModel.getRid());
            }
            actionMode.invalidate();
            if (selected.size() == 0) {
                if (actionMode != null) {
                    actionMode.finish();
                    actionMode = null;
                }
            }
            return true;
        }
        return false;
    }

    public boolean onLongClick(Message messageModel) {
        if (actionMode == null) {
            selected.clear();
            selected.put(messageModel.getRid(), messageModel);
            actionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.messages_context, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    mode.setTitle("" + selected.size());

                    boolean isAllText = true;
                    for (long k : selected.keySet()) {
                        Message model = selected.get(k);
                        if (!(model.getContent() instanceof TextContent)) {
                            isAllText = false;
                            break;
                        }
                    }

                    menu.findItem(R.id.copy).setVisible(isAllText);

                    return true;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.alert_delete_messages_text).replace("{0}", selected.size() + ""))
                                .setPositiveButton(R.string.alert_delete_messages_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

//                                        ChatActionsActor.actions().deleteMessages(chatType, chatId,
//                                                BoxUtil.unbox(selected.keySet().toArray(new Long[selected.size()])));

                                        mode.finish();
                                    }
                                })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show()
                                .setCanceledOnTouchOutside(true);
                    } else if (item.getItemId() == R.id.copy) {

                        String text = "";

                        Message[] models = selected.values().toArray(new Message[0]);
                        Arrays.sort(models, new Comparator<Message>() {

                            int compare(long lhs, long rhs) {
                                return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
                            }

                            @Override
                            public int compare(Message lhs, Message rhs) {
                                return compare(lhs.getListSortKey(), rhs.getListSortKey());
                            }
                        });

                        if (models.length == 1) {
                            for (Message model : models) {
                                if (!(model.getContent() instanceof TextContent)) {
                                    continue;
                                }
                                text += ((TextContent) model.getContent()).getText();
                            }
                        } else {
                            for (Message model : models) {
                                if (!(model.getContent() instanceof TextContent)) {
                                    continue;
                                }
                                if (text.length() > 0) {
                                    text += "\n";
                                }
                                text += users().get(model.getSenderId()).getName() + ": ";
                                text += ((TextContent) model.getContent()).getText();
                            }
                        }

                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Messages", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.toast_messages_copied, Toast.LENGTH_SHORT).show();

                        mode.finish();
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selected.clear();
                    // listView.invalidateViews();
                    actionMode = null;
                }
            });
            return true;
        }
        if (!selected.containsKey(messageModel.getRid())) {
            selected.put(messageModel.getRid(), messageModel);
        } else {
            selected.remove(messageModel.getRid());
        }
        if (actionMode != null) {
            if (selected.size() == 0) {
                actionMode.finish();
                actionMode = null;
            } else {
                actionMode.invalidate();
            }
        }

        return true;
    }

    public boolean isSelected(long rid) {
        return selected.containsKey(rid);
    }

    public void onItemViewed(Message messageModel) {
        if (messageModel.getSenderId() != myUid()) {
            messenger().onInMessageShown(peer, messageModel.getRid(), messageModel.getSortDate(), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messagesAdapter != null) {
            messagesAdapter.dispose();
        }
        messagesAdapter = null;
        messagesView = null;
        listEngine = null;
        scrollUp = null;
        scrollDown = null;
        preDump = null;
        actionMode = null;
    }
}
