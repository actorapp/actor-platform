package im.actor.sdk.controllers.conversation;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import im.actor.core.entity.GroupType;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Sticker;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.ValueListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarCallback;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteCallback;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesDefaultFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesFragmentCallback;
import im.actor.sdk.controllers.conversation.placeholder.EmptyChatPlaceholder;
import im.actor.sdk.controllers.conversation.quote.QuoteCallback;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.controllers.conversation.attach.AttachFragment;
import im.actor.sdk.controllers.conversation.toolbar.ChatToolbarFragment;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ChatFragment extends BaseFragment implements InputBarCallback, MessagesFragmentCallback, QuoteCallback,
        AutocompleteCallback {

    public static ChatFragment create(Peer peer) {
        ChatFragment res = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("peer", peer.getUnuqueId());
        res.setArguments(bundle);
        return res;
    }

    private View quoteContainer;
    private View inputContainer;
    private View inputOverlayContainer;
    private View emptyContainer;
    private TextView inputOverlayText;
    private Peer peer;
    private long editRid;
    private String currentQuote;

    public ChatFragment() {
        setUnbindOnPause(true);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        peer = Peer.fromUniqueId(getArguments().getLong("peer"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View res = inflater.inflate(R.layout.fragment_chat, container, false);

        quoteContainer = res.findViewById(R.id.quoteFragment);

        res.findViewById(R.id.sendContainer).setBackgroundColor(style.getMainBackgroundColor());
        inputContainer = res.findViewById(R.id.sendFragment);
        inputOverlayContainer = res.findViewById(R.id.inputOverlay);
        inputOverlayContainer.setBackgroundColor(style.getMainBackgroundColor());
        inputOverlayText = (TextView) res.findViewById(R.id.overlayText);
        inputOverlayText.setOnClickListener(view -> onOverlayPressed());
        inputOverlayContainer.setVisibility(View.GONE);
        emptyContainer = res.findViewById(R.id.emptyPlaceholder);
        emptyContainer.setVisibility(View.GONE);

        if (savedInstanceState == null) {
            Fragment toolbarFragment = ActorSDK.sharedActor().getDelegate().fragmentForToolbar(peer);
            if (toolbarFragment == null) {
                toolbarFragment = new ChatToolbarFragment(peer);
            }
            InputBarFragment inputBarFragment = ActorSDK.sharedActor().getDelegate().fragmentForChatInput();
            if (inputBarFragment == null) {
                inputBarFragment = new InputBarFragment();
            }

            AutocompleteFragment autocompleteFragment = ActorSDK.sharedActor().getDelegate().fragmentForAutocomplete(peer);
            if (autocompleteFragment == null) {
                autocompleteFragment = AutocompleteFragment.create(peer);
                autocompleteFragment.setUnderlyingView(res.findViewById(R.id.messagesFragment));
            }

            QuoteFragment quoteFragment = ActorSDK.sharedActor().getDelegate().fragmentForQuote();
            if (quoteFragment == null) {
                quoteFragment = new QuoteFragment();
            }
            MessagesDefaultFragment messagesDefaultFragment = MessagesDefaultFragment.create(peer);
            messagesDefaultFragment.setNewMessageListener(inputBarFragment);
            getChildFragmentManager().beginTransaction()
                    .add(toolbarFragment, "toolbar")
                    .add(R.id.messagesFragment, messagesDefaultFragment)
                    .add(R.id.sendFragment, inputBarFragment)
                    .add(R.id.quoteFragment, quoteFragment)
                    .add(R.id.emptyPlaceholder, new EmptyChatPlaceholder())
                    .add(R.id.autocompleteContainer, autocompleteFragment)
                    .commitNow();

            AbsAttachFragment fragment = ActorSDK.sharedActor().getDelegate().fragmentForAttachMenu(peer);
            if (fragment == null) {
                fragment = new AttachFragment(peer);
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.overlay, fragment)
                    .commit();
        }

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();
        findInputBar().setText(messenger().loadDraft(peer), true);

        if (peer.getPeerType() == PeerType.PRIVATE) {
            UserVM userVM = users().get(peer.getPeerId());

            if (userVM.isBot()) {
                ConversationVM conversationVM = messenger().getConversationVM(peer);
                bind(conversationVM.getIsEmpty(), conversationVM.getIsLoaded(), (isEmpty, valueModel, isLoaded, valueModel2) -> {
                    if (isEmpty) {
                        inputOverlayText.setText(R.string.chat_empty_bot);
                        inputOverlayText.setTextColor(style.getListActionColor());
                        inputOverlayText.setClickable(true);
                        showView(inputOverlayContainer, false);
                        goneView(inputContainer, false);
                    } else {
                        goneView(inputOverlayContainer, false);
                        showView(inputContainer, false);
                    }

                    if (isEmpty && isLoaded) {
                        showView(emptyContainer, false);
                    } else {
                        hideView(emptyContainer, false);
                    }
                });
                bind(userVM.getAbout(), (val, valueModel) -> {
                    if (val == null) {
                        findEmptyPlaceholder().setText(getString(R.string.chat_empty_bot_about));
                    } else {
                        findEmptyPlaceholder().setText(val);
                    }
                });
            } else {
                bind(userVM.getIsBlocked(), (val, valueModel) -> {
                    if (val) {
                        inputOverlayText.setText(R.string.blocked_unblock);
                        inputOverlayText.setTextColor(style.getListActionColor());
                        inputOverlayText.setClickable(true);
                        showView(inputOverlayContainer, false);
                        goneView(inputContainer, false);
                    } else {
                        goneView(inputOverlayContainer, false);
                        showView(inputContainer, false);
                    }
                });
            }
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(peer.getPeerId());
            bind(groupVM.isMember(), groupVM.getIsCanWriteMessage(), (isMember, valueModel, canWriteMessage, valueModel2) -> {
                if (canWriteMessage) {
                    goneView(inputOverlayContainer, false);
                    showView(inputContainer, false);
                } else if (isMember) {
                    if (messenger().isNotificationsEnabled(peer)) {
                        inputOverlayText.setText(getString(R.string.chat_mute));
                    } else {
                        inputOverlayText.setText(getString(R.string.chat_unmute));
                    }
                    inputOverlayText.setTextColor(style.getListActionColor());
                    inputOverlayText.setClickable(true);
                    inputOverlayText.setEnabled(true);
                    showView(inputOverlayContainer, false);
                    goneView(inputContainer, false);
                } else if (groupVM.getIsCanJoin().get()) {
                    inputOverlayText.setText(getString(R.string.join));
                    inputOverlayText.setTextColor(style.getListActionColor());
                    inputOverlayText.setClickable(true);
                    inputOverlayText.setEnabled(true);
                    showView(inputOverlayContainer, false);
                    goneView(inputContainer, false);
                } else if (groupVM.getIsDeleted().get()) {
                    inputOverlayText.setText(groupVM.getGroupType() == GroupType.CHANNEL ? R.string.channel_deleted : R.string.group_deleted);
                    inputOverlayText.setTextColor(style.getListActionColor());
                    inputOverlayText.setClickable(false);
                    inputOverlayText.setEnabled(false);
                    showView(inputOverlayContainer, false);
                    goneView(inputContainer, false);
                } else {
                    inputOverlayText.setText(R.string.chat_not_member);
                    inputOverlayText.setTextColor(style.getListActionColor());
                    inputOverlayText.setClickable(false);
                    inputOverlayText.setEnabled(false);
                    showView(inputOverlayContainer, false);
                    goneView(inputContainer, false);
                }
            });
        }
    }

    public void onOverlayPressed() {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            UserVM userVM = users().get(peer.getPeerId());
            if (userVM.isBot()) {
                messenger().sendMessage(peer, "/start");
            } else {
                if (userVM.getIsBlocked().get()) {
                    execute(messenger().unblockUser(userVM.getId()));
                }
            }
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM groupVM = groups().get(peer.getPeerId());
            if (groupVM.isMember().get()) {
                if (messenger().isNotificationsEnabled(peer)) {
                    messenger().changeNotificationsEnabled(peer, false);
                    inputOverlayText.setText(getString(R.string.chat_unmute));
                } else {
                    messenger().changeNotificationsEnabled(peer, true);
                    inputOverlayText.setText(getString(R.string.chat_mute));
                }
            } else if (groupVM.getIsCanJoin().get()) {
                messenger().joinGroup(groupVM.getId());
            } else {
                // TODO: Rejoin
            }
        }
    }

    public boolean onBackPressed() {

        // Share Menu
        AbsAttachFragment attachFragment = findShareFragment();
        if (attachFragment != null) {
            if (attachFragment.onBackPressed()) {
                return true;
            }
        }

        if (findInputBar().onBackPressed()) {
            return true;
        }

        // Message Edit
        if (editRid != 0) {
            editRid = 0;
            findInputBar().setText("");
            hideQuote();
            return true;
        }

        // Message Quoting
        if (currentQuote != null) {
            currentQuote = null;
            hideQuote();
            return true;
        }

        return false;
    }

    @Override
    public void onTextFocusChanged(boolean isFocused) {

    }

    @Override
    public void onTyping() {
        messenger().onTyping(peer);
    }

    @Override
    public void onTextChanged(String text) {

    }

    @Override
    public void onAutoCompleteWordChanged(String text) {
        findAutocomplete().onCurrentWordChanged(text);
    }

    @Override
    public void onTextSent(String sequence) {
        if (sequence.trim().length() > 0) {
            if (editRid == 0) {
                if (currentQuote != null) {
                    messenger().sendMessage(peer, currentQuote + sequence);
                    currentQuote = null;
                    hideQuote();
                } else {
                    messenger().sendMessage(peer, sequence);
                }
            } else {
                messenger().updateMessage(peer, sequence, editRid).failure(e -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        RpcException re = (RpcException) e;
                        String error;
                        if (re.getTag().equals("NOT_IN_TIME_WINDOW")) {
                            error = getString(R.string.edit_message_error_slowpoke);
                        } else if (re.getTag().equals("NOT_LAST_MESSAGE")) {
                            error = getString(R.string.edit_message_error_not_last);
                        } else {
                            error = re.getMessage();
                        }
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                    }
                });
                editRid = 0;
                hideQuote();
            }
            findInputBar().setText("");
        }
    }

    @Override
    public void onAudioSent(int duration, String descriptor) {
        messenger().sendVoice(peer, duration, descriptor);
    }

    @Override
    public void onStickerSent(Sticker sticker) {
        messenger().sendSticker(peer, sticker);
    }

    @Override
    public void onAttachPressed() {
        findInputBar().clearFocus();

        AbsAttachFragment attachFragment = findShareFragment();
        if (attachFragment != null) {
            quoteContainer.postDelayed(() -> attachFragment.show(), 200);
        }
    }

    @Override
    public void onAvatarClick(int uid) {
        ActorSDKLauncher.startProfileActivity(getActivity(), uid);
    }

    @Override
    public void onAvatarLongClick(int uid) {
        insertMention(uid);
    }

    @Override
    public void onMentionPicked(String name) {
        findInputBar().replaceCurrentWord(name);
    }

    @Override
    public void onCommandPicked(String command) {
        messenger().sendMessage(peer, "/" + command);
        findInputBar().setText("");
        findAutocomplete().onCurrentWordChanged("");
    }

    public void insertMention(int uid) {
        UserVM user = users().get(uid);
        String name = user.getName().get();
        String nick = user.getNick().get();

        String text = findInputBar().getText();
        if (text.length() > 0 && !text.endsWith(" ")) {
            text = text + " ";
        }

        String mentionString;
        if (nick != null) {
            mentionString = "@" + nick;
        } else {
            mentionString = name;
        }
        if (text.length() == 0) {
            mentionString += ": ";
        } else {
            mentionString += " ";
        }

        findInputBar().setText(text + mentionString);
        findInputBar().requestFocus();
    }

    @Override
    public void onMessageEdit(long rid, String text) {
        editRid = rid;
        findInputBar().setText(text);
        showQuote(text, false);
    }

    @Override
    public void onMessageQuote(String text) {
        showQuote(text, true);
        currentQuote = text;
    }

    @Override
    public void onQuoteCancelled() {
        if (editRid != 0) {
            editRid = 0;
            findInputBar().setText("");
        }
        currentQuote = null;
        hideQuote();
    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().saveDraft(peer, findInputBar().getText());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        quoteContainer = null;
    }


    //
    // Tools
    //
    private InputBarFragment findInputBar() {
        return ((InputBarFragment) getChildFragmentManager().findFragmentById(R.id.sendFragment));
    }

    private QuoteFragment findQuoteFragment() {
        return ((QuoteFragment) getChildFragmentManager().findFragmentById(R.id.quoteFragment));
    }

    private AbsAttachFragment findShareFragment() {
        return ((AbsAttachFragment) getFragmentManager().findFragmentById(R.id.overlay));
    }

    private EmptyChatPlaceholder findEmptyPlaceholder() {
        return ((EmptyChatPlaceholder) getChildFragmentManager().findFragmentById(R.id.emptyPlaceholder));
    }

    private AutocompleteFragment findAutocomplete() {
        return ((AutocompleteFragment) getChildFragmentManager().findFragmentById(R.id.autocompleteContainer));
    }

    private void hideQuote() {
        hideView(quoteContainer);
        findInputBar().setDisableOnEmptyText(true);
        findInputBar().setAudioEnabled(true);
        findInputBar().setAttachEnabled(true);
    }

    private void showQuote(String text, boolean isQuote) {
        showView(quoteContainer);
        findQuoteFragment().setText(text, isQuote);
        findInputBar().setDisableOnEmptyText(false);
        findInputBar().setAudioEnabled(false);
        findInputBar().setAttachEnabled(false);
    }
}
