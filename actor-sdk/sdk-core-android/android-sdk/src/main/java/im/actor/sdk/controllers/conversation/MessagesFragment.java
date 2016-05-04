package im.actor.sdk.controllers.conversation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.UnsupportedContent;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.ShortcutActivity;
import im.actor.sdk.controllers.conversation.messages.preprocessor.ChatListProcessor;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.fragment.DisplayListFragment;
import im.actor.sdk.controllers.settings.BaseActorSettingsFragment;
import im.actor.sdk.util.Screen;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.generic.mvvm.AndroidListUpdate;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MessagesFragment extends DisplayListFragment<Message, MessageHolder> {

    private static SharedPreferences wallpaperPrefs;

    private static final int REQUEST_GALLERY = 198;
    private String shortcutText;
    private long firstUnread = -1;
    private DisplayList.AndroidChangeListener<Message> listener;

    public static MessagesFragment create(Peer peer) {
        return new MessagesFragment(peer);
    }

    private Peer peer;

    protected MessagesAdapter messagesAdapter;
    private ConversationVM conversationVM;
    private ActionMode actionMode;
    private int onPauseSize = 0;
    private ImageView chatBackgroundView;

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
            conversationVM = messenger().getConversationVM(peer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        View res = inflate(inflater, container, R.layout.fragment_messages, onCreateDisplayList());

        //
        // Loading background
        //
        if (wallpaperPrefs == null) {
            wallpaperPrefs = getContext().getSharedPreferences("wallpaper", Context.MODE_PRIVATE);
        }
        Drawable background;
        if (messenger().getSelectedWallpaper() == null) {
            background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[0]);
        } else if (messenger().getSelectedWallpaper().equals("local:bg_1")) {
            if (ActorSDK.sharedActor().style.getDefaultBackgrouds().length > 1) {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[1]);
            } else {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[0]);
            }
        } else if (messenger().getSelectedWallpaper().equals("local:bg_2")) {
            if (ActorSDK.sharedActor().style.getDefaultBackgrouds().length > 2) {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[2]);
            } else {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[0]);
            }
        } else if (messenger().getSelectedWallpaper().equals("local:bg_3")) {
            if (ActorSDK.sharedActor().style.getDefaultBackgrouds().length > 3) {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[3]);
            } else {
                background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[0]);
            }
        } else if (messenger().getSelectedWallpaper().startsWith("local:")) {
            background = getResources().getDrawable(ActorSDK.sharedActor().style.getDefaultBackgrouds()[0]);
        } else {
            background = Drawable.createFromPath(BaseActorSettingsFragment.getWallpaperFile());
        }
        ((ImageView) res.findViewById(R.id.chatBackgroundView)).setImageDrawable(background);

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
            res.setListProcessor(new ChatListProcessor(peer, this.getContext()));
        }
        return res;
    }

    private boolean isLoaded = false;

    protected void bindDisplayListLoad() {
        bindDisplayListLoad(true);
    }

    protected void bindDisplayListLoad(boolean notify) {
        firstUnread = messenger().loadFirstUnread(peer);

        Log.d("DIAPLAY_LIST", "bindDisplayListLoad: " + notify);
        final BindedDisplayList<Message> list = getDisplayList();
        listener = new DisplayList.AndroidChangeListener<Message>() {


            @Override
            public void onCollectionChanged(AndroidListUpdate<Message> modification) {
                ondisplayListLoaded();
            }


        };
        list.addAndroidListener(listener);
        if (notify) {
            ondisplayListLoaded();
        }
    }

    private void ondisplayListLoaded() {
        final BindedDisplayList<Message> list = getDisplayList();
        Log.d("DIAPLAY_LIST", "ondisplayListLoaded  isLoaded: " + isLoaded + " list size: " + list.getSize());
        if (isLoaded) {
            return;
        }

        if (list.getSize() == 0) {
            return;
        }

        isLoaded = true;
        //long lastRead = modules.getMessagesModule().loadReadState(peer);
        Log.d("DIAPLAY_LIST", "ondisplayListLoaded  firstUnread: " + firstUnread);

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

        checkBotEmptyView();

    }

    private void scrollToUnread(long unreadId, final int index) {

        if (messagesAdapter != null) {
            messagesAdapter.setFirstUnread(unreadId);
        }

        if (getCollection() != null) {
            RecyclerView.LayoutManager layoutManager = getCollection().getLayoutManager();
            if (index > 0 && layoutManager != null && layoutManager instanceof ChatLinearLayoutManager) {
                ((ChatLinearLayoutManager) layoutManager).setStackFromEnd(false);
                ((ChatLinearLayoutManager) layoutManager).scrollToPositionWithOffset(index + 1, Screen.dp(64));
                // layoutManager.scrollToPosition(getDisplayList().getSize() - index - 1);
                // layoutManager.scrollToPosition(index + 1);
                // getCollection().scrollToPosition(index + 1);

            } else {
                // layoutManager.scrollToPosition(0);
                getCollection().scrollToPosition(0);
            }

        }
    }

    @Override
    protected BindedListAdapter<Message, MessageHolder> onCreateAdapter(BindedDisplayList<Message> displayList, Activity activity) {
        messagesAdapter = new MessagesAdapter(displayList, this, activity);
        if (firstUnread != -1 && messagesAdapter.getFirstUnread() == -1) {
            messagesAdapter.setFirstUnread(firstUnread);
        }
        return messagesAdapter;
    }

    @Override
    protected void configureRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        final ChatLinearLayoutManager layoutManager = new ChatLinearLayoutManager(getActivity(), ChatLinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);
        getDisplayList().setLinearLayoutCallback(new BindedDisplayList.LinearLayoutCallback() {
            @Override
            public void setStackFromEnd(boolean b) {
                if (layoutManager != null) layoutManager.setStackFromEnd(b);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        bindDisplayListLoad(onPauseSize != 0 && getDisplayList().getSize() != onPauseSize);
        messenger().onConversationOpen(peer);
        checkBotEmptyView();
    }

    @Override
    public void onCollectionChanged() {
        super.onCollectionChanged();
        checkBotEmptyView();
    }

    public void checkBotEmptyView() {
        if (getActivity() == null) {
            return;
        }
        ((ChatActivity) getActivity()).checkEmptyBot();

    }

    public void onAvatarClick(int uid) {
        ActorSDK.sharedActor().startProfileActivity(getActivity(), uid);
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
        } else {
            if (message.getContent() instanceof TextContent && message.getSenderId() == myUid() && message.getSortDate() >= messenger().loadFirstUnread(peer)) {
                ((ChatActivity) getActivity()).onEditTextMessage(message.getRid(), ((TextContent) message.getContent()).getText());
                return true;
            }
        }
        return false;
    }

    public boolean onLongClick(final Message message, final boolean hasMyReaction) {
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
                    menu.findItem(R.id.like).setVisible(selected.length == 1);
                    menu.findItem(R.id.shortcut).setVisible(peer.getPeerType() == PeerType.PRIVATE & selected.length == 1 && isAllText && users().get(peer.getPeerId()).isBot());
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.shortcut) {
                        shortcutText = messenger().getFormatter().formatMessagesExport(messagesAdapter.getSelected());

                        Drawable shortCutDrawable = getResources().getDrawable(R.drawable.ic_message_white_24dp);

                        createShortcutDialog(menuItem.getTitle().toString(), shortCutDrawable);
                        actionMode.finish();


                    } else if (menuItem.getItemId() == R.id.delete) {
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
                    } else if (menuItem.getItemId() == R.id.like) {
                        Message currentMessage = messagesAdapter.getSelected()[0];

                        if (hasMyReaction) {
                            ActorSDK.sharedActor().getMessenger().removeReaction(getPeer(), currentMessage.getRid(), "\u2764").start(new CommandCallback<Void>() {
                                @Override
                                public void onResult(Void res) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                        } else {
                            ActorSDK.sharedActor().getMessenger().addReaction(getPeer(), currentMessage.getRid(), "\u2764").start(new CommandCallback<Void>() {
                                @Override
                                public void onResult(Void res) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });

                        }
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
                        Intent i = new Intent(getActivity(), ActorMainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                            } else if (!(m.getContent() instanceof UnsupportedContent)) {
                                AbsContent fileMessage = m.getContent();
                                try {
                                    i.putExtra("forward_content", AbsContent.serialize(fileMessage));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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
                        getActivity().finish();
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

    public void createShortcutDialog(String title, final Drawable shortCutDrawable) {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setTitle(title);

        final LinearLayout ll = new LinearLayout(getActivity());
        ll.setPadding(Screen.dp(20), 0, Screen.dp(20), 0);

        final EditText shortcutInput = new EditText(getActivity());
        shortcutInput.setTextColor(Color.BLACK);
        shortcutInput.setText(shortcutText);
        shortcutInput.setCompoundDrawablesWithIntrinsicBounds(shortCutDrawable, null, null, null);
        ll.addView(shortcutInput, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        builder.setView(ll);

        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent shortcutIntent = new Intent(getContext(), ShortcutActivity.class);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                shortcutIntent.setAction("im.actor.action.botMessageShortcut");
                shortcutIntent.putExtra("peer", peer.getUnuqueId());
                shortcutIntent.putExtra("text", shortcutInput.getText().toString());
                Intent addIntent = new Intent();
                addIntent
                        .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutInput.getText().toString() + "->" + users().get(peer.getPeerId()).getNick().get());
//                                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                                        Intent.ShortcutIconResource.fromContext(getContext(),
//                                                R.drawable.ic_message_white_24dp));

                BitmapDrawable bd = (BitmapDrawable) shortCutDrawable;
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());

                addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                getContext().getApplicationContext().sendBroadcast(addIntent);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNeutralButton("Icon", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });

        android.support.v7.app.AlertDialog ad = builder.create();
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(FragmentActivity.INPUT_METHOD_SERVICE);
                shortcutInput.requestFocus();
                inputMethodManager.showSoftInput(shortcutInput, 0);
            }
        });
        ad.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                try {
                    createShortcutDialog("Create shortcut", Drawable.createFromStream(getActivity().getContentResolver().openInputStream(data.getData()), null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        getDisplayList().removeAndroidListener(listener);
        listener = null;
        if (messagesAdapter != null) {
            messagesAdapter.getBinder().unbindAll();
            messagesAdapter = null;
        }
    }
}
