package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import im.actor.core.entity.Peer;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_CHAT_PEER = "chat_peer";

//    private boolean isAutocompleteVisible = false;
//    private HolderAdapter autocompleteAdapter;
//    private RecyclerListView autocompleteList;
//    private String autocompleteString = "";
//    private int autocompleteTriggerStart;

    public static Intent build(Peer peer, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        // For faster keyboard open/close
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //
        // Loading Layout
        //
        setContentView(R.layout.activity_dialog);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //
        // Loading Fragments if needed
        //
        if (saveInstance == null) {
            Peer peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));
            ChatFragment chatFragment = ChatFragment.create(peer);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.chatFragment, chatFragment)
                    .commitNow();
            String quote = getIntent().getStringExtra("forward_text_raw");
            if (quote != null) {
                chatFragment.onMessageQuote(quote);
            }
        }

//        // Mentions
//        autocompleteList = (RecyclerListView) findViewById(R.id.mentionsList);
//        autocompleteList.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

    }


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
    public ActionMode startSupportActionMode(@NonNull final ActionMode.Callback callback) {
        // Fix for bug https://code.google.com/p/android/issues/detail?id=159527
        final ActionMode mode = super.startSupportActionMode(callback);
        if (mode != null) {
            mode.invalidate();
        }
        return mode;
    }


//    private void showAutoComplete(boolean initEmpty, boolean isMentions) {
//        if (isAutocompleteVisible) {
//            return;
//        }
//        isAutocompleteVisible = true;
//
//        if (!isMentions) {
//            autocompleteAdapter = new CommandsAdapter(peer.getPeerId(), this, (oldRowsCount, newRowsCount) -> onMentionsChanged(oldRowsCount, newRowsCount));
//        } else {
//            GroupVM groupInfo = groups().get(peer.getPeerId());
//            autocompleteAdapter = new MentionsAdapter(groupInfo.getId(), this, (oldRowsCount, newRowsCount) -> onMentionsChanged(oldRowsCount, newRowsCount), initEmpty);
//        }
//
////        if(autocompleteAdapter.getCount() == 0){
////            isAutocompleteVisible = false;
////            return;
////        }
//
//        autocompleteList.setAdapter(autocompleteAdapter);
//        autocompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                Object item = parent.getItemAtPosition(position);
////                if (item != null && item instanceof MentionFilterResult) {
////
////                    String origMention = ((MentionFilterResult) item).getMentionString();
////
////                    if (autocompleteTriggerStart != -1 && autocompleteTriggerStart + autocompleteString.length() + 1 <= messageEditText.getText().length()) {
////
////                        String mentionString = origMention + (autocompleteTriggerStart == 0 ? ": " : " ");
////
////                        Editable text = messageEditText.getText();
////
////                        int cursorPosition = autocompleteTriggerStart + mentionString.length();
////
////                        text.replace(autocompleteTriggerStart, autocompleteTriggerStart + autocompleteString.length() + 1, mentionString);
////
////                        messageEditText.setSelection(cursorPosition, cursorPosition);
////                    }
////                    hideMentions();
////                } else if (item != null && item instanceof BotCommand) {
////                    messenger().sendMessage(peer, "/".concat(((BotCommand) item).getSlashCommand()));
////                    messageEditText.setText("");
////                    hideMentions();
////                }
//            }
//        });
//        //hideShare();
//
//        expandMentions(autocompleteList, 0, autocompleteList.getCount());
//    }
//
//    private void hideMentions() {
//        if (!isAutocompleteVisible) {
//            return;
//        }
//        isAutocompleteVisible = false;
//
//        expandMentions(autocompleteList, autocompleteAdapter.getCount(), 0);
//        autocompleteAdapter = null;
//        autocompleteList.setAdapter(null);
//    }
//
//    private void onMentionsChanged(int oldRowsCount, int newRowsCount) {
//        if (autocompleteAdapter != null) {
//            expandMentions(autocompleteList, oldRowsCount, newRowsCount);
//        }
//    }
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (autocompleteAdapter != null) {
//            autocompleteAdapter.dispose();
//        }
//    }
}
