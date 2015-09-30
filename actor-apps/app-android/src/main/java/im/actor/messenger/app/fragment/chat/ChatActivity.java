package im.actor.messenger.app.fragment.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.Modules;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.chat.mentions.MentionsAdapter;
import im.actor.messenger.app.fragment.chat.messages.MessagesFragment;
import im.actor.messenger.app.util.RandomUtil;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.MentionSpan;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.markdown.AndroidMarkdown;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;


import static im.actor.messenger.app.core.Core.groups;
import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;
import static im.actor.messenger.app.view.ViewUtils.expandMentions;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.app.view.emoji.SmileProcessor.emoji;

public class ChatActivity extends ActorEditTextActivity {

    public static Intent build(Peer peer, boolean compose, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        intent.putExtra(EXTRA_CHAT_COMPOSE, compose);
        return intent;
    }

    //////////////////////////////////
    // Activity keys
    //////////////////////////////////

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;

    public static final String EXTRA_CHAT_PEER = "chat_peer";
    public static final String EXTRA_CHAT_COMPOSE = "compose";

    public static final String STATE_FILE_NAME = "pending_file_name";

    //////////////////////////////////
    // Configuration
    //////////////////////////////////

    //////////////////////////////////
    // Model
    //////////////////////////////////

    // Peer of current chat
    private Peer peer;

    //////////////////////////////////
    // Toolbar views
    //////////////////////////////////

    // Toolbar title root view
    private View barView;
    // Toolbar Avatar view
    private AvatarView barAvatar;
    // Toolbar title view
    private TextView barTitle;

    // Toolbar subtitle view container
    private View barSubtitleContainer;
    // Toolbar subtitle text view
    private TextView barSubtitle;

    // Toolbar typing container
    private View barTypingContainer;
    // Toolbar typing icon
    private ImageView barTypingIcon;
    // Toolbar typing text
    private TextView barTyping;

    //////////////////////////////////
    // Mentions
    //////////////////////////////////

    private boolean isMentionsVisible = false;
    private MentionsAdapter mentionsAdapter;
    private ListView mentionsList;
    private String mentionSearchString = "";
    private int mentionStart;

    //////////////////////////////////
    // Quote
    //////////////////////////////////

    private FrameLayout quoteContainer;
    private TextView quoteText;
    private String currentQuote = "";

    //////////////////////////////////
    // Forwarding
    //////////////////////////////////

    private String sendUri;
    private ArrayList<String> sendUriMultiple;
    private int shareUser;
    private String forwardDocDescriptor;
    private boolean forwardDocIsDoc = true;
    private String forwardText;
    private String forwardTextRaw;
    private String sendText;

    //////////////////////////////////
    // Utility variables
    //////////////////////////////////

    // Camera photo destination name
    private String pending_fileName;

    // Lock typing during messageEditText change
    private boolean isTypingDisabled = false;

    // Is Activity opened from Compose
    private boolean isCompose = false;

    @Override
    public void onCreate(Bundle saveInstance) {
        // Reading peer of chat
        peer = Peer.fromUniqueId(getIntent().getExtras().getLong(EXTRA_CHAT_PEER));

        if (saveInstance == null) {
            // Set compose state for auto-showing menu
            isCompose = getIntent().getExtras().getBoolean(EXTRA_CHAT_COMPOSE, false);
        } else {
            // Activity restore
            pending_fileName = saveInstance.getString(STATE_FILE_NAME, null);
        }

        super.onCreate(saveInstance);

        messageEditText.addTextChangedListener(new TextWatcherImp());


        // Mentions
        mentionsList = (ListView) findViewById(R.id.mentionsList);

        //Quote
        quoteContainer = (FrameLayout) findViewById(R.id.quoteContainer);
        quoteText = (TextView) findViewById(R.id.quote_text);
        findViewById(R.id.ib_close_quote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneView(quoteContainer);
                quoteText.setText("");
                currentQuote = "";
            }
        });

        // Sharing
        sendUri = getIntent().getStringExtra("send_uri");
        sendUriMultiple = getIntent().getStringArrayListExtra("send_uri_multiple");
        shareUser = getIntent().getIntExtra("share_user", 0);

        //Forwarding
        forwardText = getIntent().getStringExtra("forward_text");
        forwardTextRaw = getIntent().getStringExtra("forward_text_raw");
        sendText = getIntent().getStringExtra("send_text");
        forwardDocDescriptor = getIntent().getStringExtra("forward_doc_descriptor");
        forwardDocIsDoc = getIntent().getBooleanExtra("forward_doc_is_doc", true);
    }

    @Override
    protected Fragment onCreateFragment() {
        return MessagesFragment.create(peer);
    }

    @Override
    protected void onCreateToolbar() {
        // Loading Toolbar header views
        // Binding to real data is performed in onResume method
        barView = LayoutInflater.from(this).inflate(R.layout.bar_conversation, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        setToolbar(barView, layout);

        barTitle = (TextView) barView.findViewById(R.id.title);
        barSubtitleContainer = barView.findViewById(R.id.subtitleContainer);
        barTypingIcon = (ImageView) barView.findViewById(R.id.typingImage);
        barTypingIcon.setImageDrawable(new TypingDrawable());
        barTyping = (TextView) barView.findViewById(R.id.typing);
        barSubtitle = (TextView) barView.findViewById(R.id.subtitle);
        barTypingContainer = barView.findViewById(R.id.typingContainer);
        barTypingContainer.setVisibility(View.INVISIBLE);
        barAvatar = (AvatarView) barView.findViewById(R.id.avatarPreview);
        barAvatar.init(Screen.dp(32), 18);
        barView.findViewById(R.id.titleContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peer.getPeerType() == PeerType.PRIVATE) {
                    startActivity(Intents.openProfile(peer.getPeerId(), ChatActivity.this));
                } else if (peer.getPeerType() == PeerType.GROUP) {
                    startActivity(Intents.openGroup(peer.getPeerId(), ChatActivity.this));
                } else {
                    // Nothing to do
                }
            }
        });
    }

    // Activity lifecycle

    @Override
    public void onResume() {
        super.onResume();

        // Force keyboard open if activity started with Compose flag
        if (isCompose) {
            messageEditText.requestFocus();
            keyboardUtils.setImeVisibility(messageEditText, true);
        }
        isCompose = false;

        // Loading drafts
        isTypingDisabled = true;
        String text = messenger().loadDraft(peer);
        if (text != null) {
            // Using only links parsing to avoid non-mentions formatting
            Spannable spantext = AndroidMarkdown.processOnlyLinks(text);
            spantext = emoji().processEmojiCompatMutable(spantext, SmileProcessor.CONFIGURATION_BUBBLES);
            messageEditText.setText(spantext);
        } else {
            messageEditText.setText("");
        }
        messageEditText.setSelection(messageEditText.getText().length());
        isTypingDisabled = false;

        // TODO: Remove from ChatActivity
        // Performing actions

        if (sendUri != null && !sendUri.isEmpty()) {
            execute(messenger().sendUri(peer, Uri.parse(sendUri)));
            sendUri = "";
        }

        if (sendUriMultiple != null && sendUriMultiple.size() > 0) {
            for (String sendUri : sendUriMultiple) {
                execute(messenger().sendUri(peer, Uri.parse(sendUri)));
            }
            sendUriMultiple.clear();
        }

        if (sendText != null && !sendText.isEmpty()) {
            messageEditText.setText(sendText);
            sendText = "";
        }

        if (shareUser != 0) {
            String userName = users().get(shareUser).getName().get();
            String mentionTitle = "@".concat(userName);
            ArrayList<Integer> mention = new ArrayList<Integer>();
            mention.add(shareUser);
            messenger().sendMessage(peer, mentionTitle, "[".concat(mentionTitle).concat("](people://".concat(Integer.toString(shareUser)).concat(")")), mention);
            shareUser = 0;
        }

        if (forwardTextRaw != null && !forwardTextRaw.isEmpty()) {
            addQuote(forwardText, forwardTextRaw);
            forwardText = "";
            forwardTextRaw = "";
        }

        if (forwardDocDescriptor != null && !forwardDocDescriptor.isEmpty()) {
            if (forwardDocIsDoc) {
                messenger().sendDocument(peer, forwardDocDescriptor);
            } else {
                execute(messenger().sendUri(peer, Uri.fromFile(new File(forwardDocDescriptor))));
            }
            forwardDocDescriptor = "";
        }
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

            // Binding User Avatar to Toolbar
            bind(barAvatar, user.getId(), user.getAvatar(), user.getName());

            // Binding User name to Toolbar
            bind(barTitle, user.getName());

            // Binding User presence to Toolbar
            bind(barSubtitle, barSubtitleContainer, user);

            // Binding User typing to Toolbar
            bindPrivateTyping(barTyping, barTypingContainer, barSubtitle, messenger().getTyping(user.getId()));

            // Hide removedFromGroup panel as we are not in group
            removedFromGroup.setVisibility(View.GONE);

        } else if (peer.getPeerType() == PeerType.GROUP) {

            // Loading group
            GroupVM group = groups().get(peer.getPeerId());
            if (group == null) {
                finish();
                return;
            }

            // Binding Group avatar to Toolbar
            bind(barAvatar, group.getId(), group.getAvatar(), group.getName());

            // Binding Group title to Toolbar
            bind(barTitle, group.getName());

            // Subtitle is always visible for Groups
            barSubtitleContainer.setVisibility(View.VISIBLE);

            // Binding group members
            bind(barSubtitle, barSubtitleContainer, group);

            // Binding group typing
            bindGroupTyping(barTyping, barTypingContainer, barSubtitle, messenger().getGroupTyping(group.getId()));

            // Binding membership flag to removedFromGroup panel
            bind(messenger().getGroups().get(peer.getPeerId()).isMember(), new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> Value) {
                    removedFromGroup.setVisibility(val ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Saving draft
        messenger().saveDraft(peer, messageEditText.getText().toString());
    }

    // Message send

    @Override
    protected void onSendButtonPressed() {

        String rawText = messageEditText.getText().toString();

        if (currentQuote != null && !currentQuote.isEmpty()) {
            rawText = currentQuote.concat(rawText);
            goneView(quoteContainer);
            currentQuote = "";
        }

        messageEditText.setText("");
        mentionSearchString = "";

        if (rawText.length() == 0) {
            return;
        }

        // Hack for full screen mode
        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageEditText, false);
        }

        messenger().sendMessage(peer, rawText);
    }

    @Override
    protected void onAttachButtonClicked() {
        Context wrapper = new ContextThemeWrapper(ChatActivity.this, R.style.AttachPopupTheme);
        PopupMenu popup = new PopupMenu(wrapper, findViewById(R.id.attachAnchor));

        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.getMenuInflater().inflate(R.menu.attach_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.gallery) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/* video/*");
                    startActivityForResult(intent, REQUEST_GALLERY);
                    return true;
                } else if (item.getItemId() == R.id.takePhoto) {
                    File externalFile = getExternalFilesDir(null);
                    if (externalFile == null) {
                        Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    String externalPath = externalFile.getAbsolutePath();
                    new File(externalPath + "/actor/").mkdirs();

                    pending_fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".jpg";
                    startActivityForResult(
                            new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pending_fileName))),
                            REQUEST_PHOTO);
                } else if (item.getItemId() == R.id.takeVideo) {

                    File externalFile = getExternalFilesDir(null);
                    if (externalFile == null) {
                        Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    String externalPath = externalFile.getAbsolutePath();
                    new File(externalPath + "/actor/").mkdirs();

                    pending_fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".mp4";

                    Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pending_fileName)));
                    startActivityForResult(i, REQUEST_VIDEO);
                    return true;
                } else if (item.getItemId() == R.id.file) {
                    startActivityForResult(Intents.pickFile(ChatActivity.this), REQUEST_DOC);
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                if (data.getData() != null) {
                    execute(messenger().sendUri(peer, data.getData()), R.string.pick_downloading);
                }
            } else if (requestCode == REQUEST_PHOTO) {
                messenger().sendPhoto(peer, pending_fileName);
            } else if (requestCode == REQUEST_VIDEO) {
                messenger().sendVideo(peer, pending_fileName);
            } else if (requestCode == REQUEST_DOC) {
                if (data.getData() != null) {
                    execute(messenger().sendUri(peer, data.getData()), R.string.pick_downloading);
                } else if (data.hasExtra("picked")) {
                    ArrayList<String> files = data.getStringArrayListExtra("picked");
                    if (files != null) {
                        for (String s : files) {
                            messenger().sendDocument(peer, s);
                        }
                    }
                }
            }
        }
    }

    // Mentions

    private void showMentions(boolean initEmpty) {
        if (isMentionsVisible) {
            return;
        }
        isMentionsVisible = true;


        GroupVM groupInfo = groups().get(peer.getPeerId());
        mentionsAdapter = new MentionsAdapter(groupInfo.getId(), this, new MentionsAdapter.MentionsUpdatedCallback() {

            @Override
            public void onMentionsUpdated(int oldRowsCount, int newRowsCount) {
                onMentionsChanged(oldRowsCount, newRowsCount);
            }
        }, initEmpty);

        mentionsList.setAdapter(mentionsAdapter);
        mentionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null && item instanceof MentionFilterResult) {

                    String origMention = ((MentionFilterResult) item).getMentionString();

                    if (mentionStart != -1 && mentionStart + mentionSearchString.length() + 1 <= messageEditText.getText().length()) {

                        String mentionString = origMention + ": ";

                        Editable text = messageEditText.getText();

                        int cursorPosition = mentionStart + mentionString.length();

                        text.replace(mentionStart, mentionStart + mentionSearchString.length() + 1, mentionString);

                        messageEditText.setSelection(cursorPosition, cursorPosition);
                    }
                    hideMentions();
                }
            }
        });

        expandMentions(mentionsList, 0, mentionsList.getCount());
    }

    private void hideMentions() {
        if (!isMentionsVisible) {
            return;
        }
        isMentionsVisible = false;

        expandMentions(mentionsList, mentionsAdapter.getCount(), 0);
        mentionsAdapter = null;
        mentionsList.setAdapter(null);
    }

    private void onMentionsChanged(int oldRowsCount, int newRowsCount) {
        if (mentionsAdapter != null) {
            expandMentions(mentionsList, oldRowsCount, newRowsCount);
        }
    }

    public void insertMention(int uid) {
        UserVM user = users().get(uid);
        String name = user.getName().get();
        String nick = user.getNick().get();
        Editable text = messageEditText.getText();
        if (text.length() > 0 && text.charAt(text.length() - 1) != ' ') text.append(" ");

        String mentionString = ((nick != null && !nick.isEmpty()) ? "@" + nick : name) + ": ";

        text.append(mentionString);
        messageEditText.requestFocus();
        keyboardUtils.setImeVisibility(messageEditText, true);
    }


    // Quotes

    public void addQuote(String quote, String rawQuote) {
        if (quote != null && !quote.isEmpty()) {
            quoteText.setText(quote);
        } else {
            quoteText.setText(rawQuote);
        }
        currentQuote = rawQuote;
        showView(quoteContainer);
    }

    // Back button handling

    @Override
    public void onBackPressed() {
        if (isMentionsVisible) {
            hideMentions();
        } else if (emojiKeyboard.isShowing()) {
            emojiKeyboard.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    // Options Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflating menu
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        // Show menu for opening chat contact
        if (peer.getPeerType() == PeerType.PRIVATE) {
            menu.findItem(R.id.contact).setVisible(true);
        } else {
            menu.findItem(R.id.contact).setVisible(false);
        }

        // Show menus for leave group and group info view
        if (peer.getPeerType() == PeerType.GROUP) {
            if (groups().get(peer.getPeerId()).isMember().get()) {
                menu.findItem(R.id.leaveGroup).setVisible(true);
                menu.findItem(R.id.groupInfo).setVisible(true);
            } else {
                menu.findItem(R.id.leaveGroup).setVisible(false);
                menu.findItem(R.id.groupInfo).setVisible(false);
            }
        } else {
            menu.findItem(R.id.groupInfo).setVisible(false);
            menu.findItem(R.id.leaveGroup).setVisible(false);
        }

        // Hide unsupported files menu
        menu.findItem(R.id.files).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.clear:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_delete_all_messages_text)
                        .setPositiveButton(R.string.alert_delete_all_messages_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                execute(messenger().clearChat(peer));
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.leaveGroup:
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.alert_leave_group_message)
                                .replace("%1$s", groups().get(peer.getPeerId()).getName().get()))
                        .setPositiveButton(R.string.alert_leave_group_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog2, int which) {
                                execute(messenger().leaveGroup(peer.getPeerId()));
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.contact:
                startActivity(Intents.openProfile(peer.getPeerId(), ChatActivity.this));
                break;
            case R.id.groupInfo:
                startActivity(Intents.openGroup(peer.getPeerId(), ChatActivity.this));
                break;
            case R.id.files:
                // startActivity(Intents.openDocs(chatType, chatId, ChatActivity.this));
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (pending_fileName != null) {
            outState.putString(STATE_FILE_NAME, pending_fileName);
        }
    }

    private class TextWatcherImp implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Notify about typing only when text is increased
            if (after > count && !isTypingDisabled) {
                messenger().onTyping(peer);
            }

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String str = s.toString();
            String firstPeace = str.substring(0, start + count);

            int startSelection = messageEditText.getSelectionStart();

            String currentWord = "";
            int length = 0;

            for (String word : str.split(" ")) {
                length = length + word.length() + 1;
                if (length > startSelection) {
                    currentWord = word;
                    break;
                }
            }

            currentWord = currentWord.isEmpty() ? str : currentWord;

            if (peer.getPeerType() == PeerType.GROUP) {
                //Open mentions
                if (count == 1 && s.charAt(start) == '@' && !str.endsWith(" ")) {
                    showMentions(false);
                    mentionSearchString = "";

                } else if (currentWord.startsWith("@") && !str.endsWith(" ")) {
                    showMentions(true);
                } else {
                    hideMentions();
                }

                //Set mentions query
                mentionStart = firstPeace.lastIndexOf("@");
                if (currentWord.startsWith("@") && currentWord.length() > 1) {
                    mentionSearchString = currentWord.substring(1, currentWord.length());
                } else {
                    mentionSearchString = "";
                }

                if (mentionSearchString.equals(" ")) {
                    hideMentions();
                } else if (mentionsAdapter != null) {
                    //mentionsDisplay.initSearch(mentionSearchString, false);
                    mentionsAdapter.setQuery(mentionSearchString.toLowerCase());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                sendButton.setTint(getResources().getColor(R.color.conv_send_enabled));
                sendButton.setEnabled(true);
            } else {
                sendButton.setTint(getResources().getColor(R.color.conv_send_disabled));
                sendButton.setEnabled(false);
            }


        }
    }
}
