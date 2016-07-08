package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.conversation.mentions.CommandsAdapter;
import im.actor.sdk.controllers.conversation.mentions.MentionsAdapter;
import im.actor.core.utils.GalleryScannerActor;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.RecyclerListView;

import static im.actor.sdk.util.ViewUtils.expandMentions;
import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

@SuppressWarnings("NullableProblems")
public class ChatActivity extends BaseActivity {

    public static final String EXTRA_CHAT_PEER = "chat_peer";

    // Peer of current chat
    private Peer peer;

    private boolean isAutocompleteVisible = false;
    private HolderAdapter autocompleteAdapter;
    private RecyclerListView autocompleteList;
    private String autocompleteString = "";
    private int autocompleteTriggerStart;

    public static Intent build(Peer peer, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        //
        // Loading Settings
        //
        peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));

        //
        // Loading Layout
        //
        setContentView(R.layout.activity_dialog);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //
        // Loading Fragments if needed
        //
        if (saveInstance == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.chatFragment, ChatFragment.create(peer))
                    .commit();
        }

        // Mentions
        autocompleteList = (RecyclerListView) findViewById(R.id.mentionsList);
        autocompleteList.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        handleIntent();

//        emptyBotSend = findViewById(R.id.botEmptyTextBlock);
//        emptyBotHint = (TextView) findViewById(R.id.botEmptyHint);

        checkEmptyBot();
    }

    private void handleIntent() {
        //Forwarding
//        forwardText = intent.getStringExtra("forward_text");
//        forwardTextRaw = intent.getStringExtra("forward_text_raw");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Notify old chat closed
        messenger().onConversationClosed(peer);

        peer = Peer.fromUniqueId(intent.getExtras().getLong(EXTRA_CHAT_PEER));
        // setFragment(null);

        onPerformBind();
        handleIntent();
    }

    // Activity lifecycle

    @Override
    public void onResume() {
        super.onResume();

//        if (forwardTextRaw != null && !forwardTextRaw.isEmpty()) {
//            addQuote(forwardText, forwardTextRaw);
//            forwardText = "";
//            forwardTextRaw = "";
//        }

//        if (isBot) {
//            emptyBotSend.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
//            TextView emptyBotSendText = (TextView) emptyBotSend.findViewById(R.id.empty_bot_text);
//            emptyBotSendText.setTextColor(ActorSDK.sharedActor().style.getMainColor());
//
//            emptyBotSendText.setOnClickListener(v -> messenger().sendMessage(peer, "/start"));
//
//            checkEmptyBot();
//        }

//        if (isShareVisible) {
//            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Show());
//        }
    }

    public void checkIsBot() {
        // isBot = (peer.getPeerType() == PeerType.PRIVATE && users().get(peer.getPeerId()).isBot());
    }

    public void checkEmptyBot() {
//        if (isBot) {
//            messenger().isStarted(peer.getPeerId())
//                    .then(empty -> {
//                        if (empty) {
//                            showView(emptyBotSend);
//                            showView(emptyBotHint);
//                        } else {
//                            hideView(emptyBotSend);
//                            hideView(emptyBotHint);
//                        }
//                    });
//        }
    }


    @Override
    protected void onPerformBind() {
        super.onPerformBind();

        // Performing all required Data Binding here

        if (peer.getPeerType() == PeerType.PRIVATE) {

            // Loading user
            final UserVM user = users().get(peer.getPeerId());
            if (user == null) {
                finish();
                return;
            }

//            // Bind user blocked
//            inputBlockedText.setText(R.string.profile_settings_unblock);
//            bind(users().get(peer.getPeerId()).getIsBlocked(), (val, valueModel) -> {
//                inputBlockContainer.setVisibility(val ? View.VISIBLE : View.GONE);
//            });
//            inputBlockedText.setOnClickListener(v -> {
//                execute(messenger().unblockUser(peer.getPeerId()));
//            });

            // Bind empty bot about
//            if (isBot) {
//                bind(users().get(peer.getPeerId()).getAbout(), (about, valueModel) -> {
//                    emptyBotHint.setText((about != null && !about.isEmpty()) ? about : getString(R.string.chat_empty_bot_about));
//                });
//            }

        } else if (peer.getPeerType() == PeerType.GROUP) {

            // Loading group
            GroupVM group = groups().get(peer.getPeerId());
            if (group == null) {
                finish();
                return;
            }

//            // Binding membership flag to inputBlockContainer panel
//            bind(group.isMember(), (val, Value) -> {
//                inputBlockContainer.setVisibility(val ? View.GONE : View.VISIBLE);
//            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        messenger().getGalleryScannerActor().send(new GalleryScannerActor.Hide());
    }

//    @Override
//    protected void onAttachButtonClicked() {
//
//        if (shareMenuMaxHeight == 0) {
//            shareMenuMaxHeight = Screen.dp(245);
//        }
//
//        // Trying to open custom share menu
//        if (ActorSDK.sharedActor().getDelegate().onAttachMenuClicked(this)) {
//            return;
//        }
//
//        // Opening default share menu
//        if (shareMenuCaontainer.getVisibility() == View.VISIBLE) {
//            hideShare();
//        } else {
//            shareContainer.setVisibility(View.VISIBLE);
//            showShare();
//            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                keyboardUtils.setImeVisibility(messageEditText, false);
//                messageEditText.clearFocus();
//            }
//        }
//    }

    // Mentions
    // Bot commands

    private void showAutoComplete(boolean initEmpty, boolean isMentions) {
        if (isAutocompleteVisible) {
            return;
        }
        isAutocompleteVisible = true;

        if (!isMentions) {
            autocompleteAdapter = new CommandsAdapter(peer.getPeerId(), this, (oldRowsCount, newRowsCount) -> onMentionsChanged(oldRowsCount, newRowsCount));
        } else {
            GroupVM groupInfo = groups().get(peer.getPeerId());
            autocompleteAdapter = new MentionsAdapter(groupInfo.getId(), this, (oldRowsCount, newRowsCount) -> onMentionsChanged(oldRowsCount, newRowsCount), initEmpty);
        }

//        if(autocompleteAdapter.getCount() == 0){
//            isAutocompleteVisible = false;
//            return;
//        }

        autocompleteList.setAdapter(autocompleteAdapter);
        autocompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object item = parent.getItemAtPosition(position);
//                if (item != null && item instanceof MentionFilterResult) {
//
//                    String origMention = ((MentionFilterResult) item).getMentionString();
//
//                    if (autocompleteTriggerStart != -1 && autocompleteTriggerStart + autocompleteString.length() + 1 <= messageEditText.getText().length()) {
//
//                        String mentionString = origMention + (autocompleteTriggerStart == 0 ? ": " : " ");
//
//                        Editable text = messageEditText.getText();
//
//                        int cursorPosition = autocompleteTriggerStart + mentionString.length();
//
//                        text.replace(autocompleteTriggerStart, autocompleteTriggerStart + autocompleteString.length() + 1, mentionString);
//
//                        messageEditText.setSelection(cursorPosition, cursorPosition);
//                    }
//                    hideMentions();
//                } else if (item != null && item instanceof BotCommand) {
//                    messenger().sendMessage(peer, "/".concat(((BotCommand) item).getSlashCommand()));
//                    messageEditText.setText("");
//                    hideMentions();
//                }
            }
        });
        //hideShare();

        expandMentions(autocompleteList, 0, autocompleteList.getCount());
    }

    private void hideMentions() {
        if (!isAutocompleteVisible) {
            return;
        }
        isAutocompleteVisible = false;

        expandMentions(autocompleteList, autocompleteAdapter.getCount(), 0);
        autocompleteAdapter = null;
        autocompleteList.setAdapter(null);
    }

    private void onMentionsChanged(int oldRowsCount, int newRowsCount) {
        if (autocompleteAdapter != null) {
            expandMentions(autocompleteList, oldRowsCount, newRowsCount);
        }
    }

//    public void insertMention(int uid) {
//        UserVM user = users().get(uid);
//        String name = user.getName().get();
//        String nick = user.getNick().get();
//        Editable text = messageEditText.getText();
//        if (text.length() > 0 && text.charAt(text.length() - 1) != ' ') text.append(" ");
//
//        String mentionString = ((nick != null && !nick.isEmpty()) ? "@" + nick : name) + (messageEditText.getText().length() > 0 ? " " : ": ");
//
//        text.append(mentionString);
//        messageEditText.requestFocus();
//        keyboardUtils.setImeVisibility(messageEditText, true);
//    }

    // Back button handling

//    @Override
//    public void onBackPressed() {
//        if (isAutocompleteVisible) {
//            hideMentions();
////        } else if (isShareVisible) {
////            hideShare();
////        } else if (emojiKeyboard.isShowing()) {
////            emojiKeyboard.dismiss();
//        } else {
//            super.onBackPressed();
//        }
//    }


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chatFragment);
        if (fragment instanceof ChatFragment) {
            if (!((ChatFragment) fragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public ActionMode startSupportActionMode(final ActionMode.Callback callback) {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startSupportActionMode(callback);
        if (mode != null) {
            mode.invalidate();
        }
        return mode;
    }

//    private class TextWatcherImp implements TextWatcher {
//
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            String str = s.toString();
//            String firstPeace = str.substring(0, start + count);
//
//            int startSelection = messageEditText.getSelectionStart();
//
//            String currentWord = "";
//            int length = 0;
//
//            for (String word : str.split(" ")) {
//                length = length + word.length() + 1;
//                if (length > startSelection) {
//                    currentWord = word;
//                    break;
//                }
//            }
//
//            currentWord = currentWord.isEmpty() ? str : currentWord;
//
//            char autocompleteTriggerChar = '@';
//            String autocompleteTriggerString = "@";
//
//
//            if (peer.getPeerType() == PeerType.GROUP || isBot) {
//
//                if (isBot) {
//                    autocompleteTriggerChar = '/';
//                    autocompleteTriggerString = "/";
//                }
//                //Open mentions
//                if (count == 1 && s.charAt(start) == autocompleteTriggerChar && !str.endsWith(" ")) {
//                    showAutoComplete(false, !isBot);
//                    autocompleteString = "";
//
//                } else if (currentWord.startsWith(autocompleteTriggerString) && !str.endsWith(" ")) {
//                    showAutoComplete(true, !isBot);
//                } else {
//                    hideMentions();
//                }
//
//                //Set mentions query
//                autocompleteTriggerStart = firstPeace.lastIndexOf(autocompleteTriggerString);
//                if (currentWord.startsWith(autocompleteTriggerString) && currentWord.length() > 1) {
//                    autocompleteString = currentWord.substring(1, currentWord.length());
//                } else {
//                    autocompleteString = "";
//                }
//
//                if (autocompleteString.equals(" ")) {
//                    hideMentions();
//                } else if (autocompleteAdapter != null) {
//                    //mentionsDisplay.initSearch(autocompleteString, false);
//                    if (autocompleteAdapter instanceof MentionsAdapter) {
//                        ((MentionsAdapter) autocompleteAdapter).setQuery(autocompleteString.toLowerCase());
//                    } else if (autocompleteAdapter instanceof CommandsAdapter) {
//                        ((CommandsAdapter) autocompleteAdapter).setQuery(autocompleteString.toLowerCase());
//                    }
//                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autocompleteAdapter != null) {
            autocompleteAdapter.dispose();
        }
    }

}
