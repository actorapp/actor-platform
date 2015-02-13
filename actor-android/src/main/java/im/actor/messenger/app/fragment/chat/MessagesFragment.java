package im.actor.messenger.app.fragment.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.uilist.UiList;
import com.droidkit.engine.uilist.UiListStateListener;
import com.droidkit.mvvm.ui.Listener;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.fragment.chat.adapter.ChatAdapter;
import im.actor.messenger.app.view.BackgroundView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.core.actors.base.UiAskCallback;
import im.actor.messenger.core.actors.chat.ChatActionsActor;
import im.actor.messenger.core.actors.chat.OwnReadStateActor;
import im.actor.messenger.core.actors.contacts.ContactsActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.messages.ConversationMessage;
import im.actor.messenger.storage.scheme.messages.ReadState;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.util.BoxUtil;
import im.actor.messenger.util.Logger;
import im.actor.messenger.util.Screen;
import im.actor.messenger.util.VisibleViewItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.readStates;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class MessagesFragment extends BaseFragment implements UiListStateListener, ExScrollListener {

    public static MessagesFragment create(int chatType, int chatId) {
        Bundle bundle = new Bundle();
        bundle.putInt("CHAT_TYPE", chatType);
        bundle.putInt("CHAT_ID", chatId);
        MessagesFragment res = new MessagesFragment();
        res.setArguments(bundle);
        return res;
    }

    private static final int STATE_BOTTOM = 0;
    private static final int STATE_TOP = 1;
    private static final int STATE_UNREAD = 2;

    private int chatType, chatId;
    private ConversationListView listView;
    private ChatAdapter adapter;
    private EngineUiList<MessageModel> listEngine;
    private UiList<MessageModel> list;
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

    private HashMap<Long, MessageModel> selected = new HashMap<Long, MessageModel>();

    private long firstUnread = 0;

    private boolean isFirst = true;

    public int getChatType() {
        return chatType;
    }

    public int getChatId() {
        return chatId;
    }

    public long getFirstUnread() {
        return firstUnread;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatType = getArguments().getInt("CHAT_TYPE");
        chatId = getArguments().getInt("CHAT_ID");

        scrollNewMessage = new TextView(getActivity());
        scrollNewMessage.setTextColor(Color.WHITE);
        scrollNewMessage.setText(R.string.chat_new_messages);
        scrollNewMessage.setBackgroundResource(R.drawable.conv_scroll_down_new);
        scrollNewMessage.setTypeface(Fonts.load(getActivity(), "Regular"));
        scrollNewMessage.setTextSize(14);
        scrollNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != STATE_BOTTOM) {
                    state = STATE_BOTTOM;
                    listEngine.scrollToStart();
                } else {
                    for (int i = 0; i < list.getSize(); i++) {
                        if (list.getItem(i).getRaw().getRid() == newMessageId) {
                            listView.setSelectionFromTop(list.getSize() - i - 1, Screen.dp(48));
                            break;
                        }
                    }
                    newMessageId = 0;
                    if (showNewMessage) {
                        showNewMessage = false;
                        goneView(scrollNewMessage);
                    }
                }
            }
        });
        scrollNewMessage.setVisibility(View.GONE);

        scrollUp = new ImageView(getActivity());
        scrollUp.setImageResource(R.drawable.conv_scroll_up);
        scrollUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = STATE_TOP;
                listEngine.scrollToEnd();
            }
        });

        scrollDown = new ImageView(getActivity());
        scrollDown.setImageResource(R.drawable.conv_scroll_down);
        scrollDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = STATE_BOTTOM;
                listEngine.scrollToStart();
            }
        });

        listView = new ConversationListView(getActivity());
        listView.setDividerHeight(0);
        listView.setDivider(null);
        // listView.setBackgroundResource(R.drawable.chat_bg_container);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setScrollingCacheEnabled(false);
        // listView.setCacheColorHint(getResources().getColor(R.color.conv_bg));
        // listView.setBackgroundColor(getResources().getColor(R.color.conv_bg));
        // listView.setStackFromBottom(true);

        listEngine = ListEngines.messagesUiList(DialogUids.getDialogUid(chatType, chatId));
        list = listEngine.getUiList();
        adapter = new ChatAdapter(chatType, chatId, this, getActivity());
        View view = new View(getActivity());
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(10)));
        listView.addFooterView(view, null, false);

        FrameLayout header = new FrameLayout(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setPadding(0, 0, 0, Screen.dp(8));
        final TextView addContact = new TextView(getActivity());
        addContact.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));
        addContact.setText(R.string.chat_add_to_contacts);
        addContact.setAllCaps(true);
        addContact.setTextColor(getResources().getColor(R.color.text_primary_light));
        addContact.setTextSize(14);
        addContact.setTypeface(Fonts.load(getActivity(), "Medium"));
        addContact.setGravity(Gravity.CENTER);
        addContact.setBackgroundResource(R.drawable.selector_add);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask(ContactsActor.contactsList().addContact(chatId), getString(R.string.chat_add_to_contacts_progress), new UiAskCallback<Boolean>() {
                    @Override
                    public void onPreStart() {

                    }

                    @Override
                    public void onCompleted(Boolean res) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });
            }
        });

        if (chatType == DialogType.TYPE_USER) {
            UserModel u = users().get(chatId);
            getBinder().bind(u.getContactModel(), new Listener<Boolean>() {
                @Override
                public void onUpdated(Boolean aBoolean) {
                    if (aBoolean) {
                        addContact.setVisibility(View.GONE);
                    } else {
                        addContact.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            addContact.setVisibility(View.GONE);
        }

        header.addView(addContact);
        listView.addHeaderView(header, null, false);

        listView.setAdapter(adapter);
        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                adapter.onMovedToScrapHeap(view);
            }
        });

        bg = new BackgroundView(getActivity());

        FrameLayout rootLayout = new FrameLayout(getActivity());

        rootLayout.addView(bg, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        rootLayout.addView(listView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
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
        adapter.notifyDataSetChanged();

        if (isFirst) {
            isFirst = false;
            initUnreadLocation();
        }

        scrollUp.setVisibility(View.GONE);
        scrollDown.setVisibility(View.GONE);

        listEngine.getUiList().addExListener(this);
        listView.setExScrollListener(this);

        // listView.setAdapter(adapter);

        // bg.bind();
    }

    private void initUnreadLocation() {

        if (list.getSize() == 0) {
            return;
        }

        ReadState readState = readStates().get(DialogUids.getDialogUid(chatType, chatId));
        if (readState == null || readState.getLastReadSortingKey() == 0) {
            listView.setSelectionFromTop(list.getSize() - 1, -10000);
            return;
        }

        long lastRead = readState.getLastReadSortingKey();
        int index = -1;

        for (int i = list.getSize() - 1; i >= 0; i--) {
            MessageModel messageModel = list.getItem(i);
            if (messageModel.getRaw().getSenderId() == myUid()) {
                continue;
            }
            if (messageModel.getRaw().getSortKey() > lastRead) {
                firstUnread = messageModel.getRid();
                index = i;
                break;
            }
        }

        if (index >= 0) {
            listView.setSelectionFromTop(list.getSize() - index, Screen.dp(48));
        } else {
            listView.setSelectionFromTop(list.getSize(), -10000);
        }
    }

    public boolean onClick(MessageModel messageModel) {
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

    public boolean onLongClick(MessageModel messageModel) {
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
                        MessageModel model = selected.get(k);
                        if (!(model.getContent() instanceof TextMessage)) {
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

                                        ChatActionsActor.actions().deleteMessages(chatType, chatId,
                                                BoxUtil.unbox(selected.keySet().toArray(new Long[selected.size()])));

                                        mode.finish();
                                    }
                                })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show()
                                .setCanceledOnTouchOutside(true);
                    } else if (item.getItemId() == R.id.copy) {

                        String text = "";

                        MessageModel[] models = selected.values().toArray(new MessageModel[0]);
                        Arrays.sort(models, new Comparator<MessageModel>() {

                            int compare(long lhs, long rhs) {
                                return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
                            }

                            @Override
                            public int compare(MessageModel lhs, MessageModel rhs) {
                                return compare(lhs.getRaw().getListSortKey(), rhs.getRaw().getListSortKey());
                            }
                        });

                        if (models.length == 1) {
                            for (MessageModel model : models) {
                                if (!(model.getContent() instanceof TextMessage)) {
                                    continue;
                                }
                                text += ((TextMessage) model.getContent()).getText();
                            }
                        } else {
                            for (MessageModel model : models) {
                                if (!(model.getContent() instanceof TextMessage)) {
                                    continue;
                                }
                                if (text.length() > 0) {
                                    text += "\n";
                                }
                                text += users().get(model.getRaw().getSenderId()).getName() + ": ";
                                text += ((TextMessage) model.getContent()).getText();
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
                    listView.invalidateViews();
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

    public void onItemViewed(MessageModel messageModel) {
        if (messageModel.getRaw().getSenderId() != myUid()) {
            OwnReadStateActor.readState().messageRead(chatType, chatId, messageModel.getRid(), messageModel.getRaw().getSortKey(),
                    messageModel.getRaw().getTime(),
                    messageModel.getRaw().getContent().isEncrypted());
        }
    }

    @Override
    public void onStoppedScroll() {
        goneView(scrollUp);
        goneView(scrollDown);
    }

    @Override
    public void onScrolledUp() {
        showView(scrollUp);
        goneView(scrollDown);
    }

    @Override
    public void onScrolledDown() {
        goneView(scrollUp);
        if (!showNewMessage) {
            showView(scrollDown);
        }
    }

    @Override
    public void onScrolledToEnd() {
        isScrolledToEnd = true;
        if (showNewMessage) {
            showNewMessage = false;
            goneView(scrollNewMessage);
        }
    }

    @Override
    public void onScrolledFromEnd() {
        isScrolledToEnd = false;
    }

    @Override
    public void onListPreUpdated() {
        preDump = dumpState();
        preSize = list.getSize();
        bottomId = 0;
        if (preSize > 0) {
            ConversationMessage bottomMessage = list.getItem(0).getRaw();
            bottomId = bottomMessage.getRid();
        }
    }

    @Override
    public void onListPostUpdated() {
        adapter.notifyDataSetChanged();

        // Empty list
        if (list.getSize() == 0) {
            return;
        }

        // Initial load
        if (preDump.length == 0) {
            adapter.notifyDataSetChanged();
            if (state == STATE_BOTTOM) {
                listView.setSelectionFromTop(list.getSize() - 1, -10000);
            } else if (state == STATE_TOP) {
                listView.setSelectionFromTop(0, 0);
            } else if (state == STATE_UNREAD) {
                initUnreadLocation();
            }
            return;
        }

        // Scrolling to bottom on new messages

        if (list.getSize() > 0 && preSize > 0 && listView.getLastVisiblePosition() >= preSize - 1
                && bottomId != list.getItem(0).getRaw().getRid()) {
            if (BuildConfig.ENABLE_CHROME) {
                listView.smoothScrollToPosition(list.getSize());
            } else {
                listView.setSelectionFromTop(list.getSize(), -10000);
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        listView.setSelectionFromTop(list.getSize(), -10000);
                    }
                });
            }
            return;
        }

        // Keeping position

        // Fallback values
        int newIndex = preDump[0].getIndex() + (list.getSize() - preSize) - 1;
        int newTop = preDump[0].getTop();

        Logger.d("MessagesList", "Apply change");

        // TODO: Optimize
        outer:
        for (int i = 0; i < list.getSize(); i++) {
            for (int j = 0; j < preDump.length; j++) {
                if (preDump[j].getId() == list.getItem(list.getSize() - i - 1).getRaw().getRid()) {
                    newIndex = i;
                    newTop = preDump[j].getTop();
                    break outer;
                }
            }
        }

        listView.setSelectionFromTop(newIndex, newTop);
    }

    public VisibleViewItem[] dumpState() {
        int childCount = listView.getChildCount();
        int headerCount = 0;

        ArrayList<VisibleViewItem> res = new ArrayList<VisibleViewItem>();
        for (int i = 0; i < childCount; i++) {
            View v = listView.getChildAt(i);
            int index = listView.getFirstVisiblePosition() + i;
            if (index >= list.getSize()) {
                continue;
            }
            long id = list.getItem(list.getSize() - index - 1).getRaw().getRid();
            if (id != 0) {
                int top = ((v == null) ? 0 : v.getTop()) - listView.getPaddingTop();
                res.add(new VisibleViewItem(index + headerCount, top, id));
            }
        }

        return res.toArray(new VisibleViewItem[0]);
    }

    @Override
    public void onPause() {
        super.onPause();
        listEngine.getUiList().removeExListener(this);
        listView.setExScrollListener(null);
        if (actionMode != null) {
            actionMode.finish();
        }
        listView.invalidateViews();
        // listView.setAdapter(null);
        // adapter.dispose();
        // bg.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.dispose();
        }
        if (listView != null) {
            listView.setRecyclerListener(null);
        }
        adapter = null;
        listView = null;
        listEngine = null;
        list = null;
        scrollUp = null;
        scrollDown = null;
        preDump = null;
        actionMode = null;
    }
}
