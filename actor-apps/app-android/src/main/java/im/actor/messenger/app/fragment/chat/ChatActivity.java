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
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
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
import im.actor.messenger.app.view.SelectionListenerEditText;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.messenger.app.view.emoji.SmileProcessor;
import im.actor.messenger.app.view.markdown.AndroidMarkdown;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

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

    // Separator for mention span for propper detection of deletion
    private static final Character MENTION_BOUNDS_CHR = '\u200b';
    private static final String MENTION_BOUNDS_STR = MENTION_BOUNDS_CHR.toString();

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
    private boolean useForceMentionHide = false;
    private boolean forceMentionHide = useForceMentionHide;
    private String lastMentionSearch = "";

    //////////////////////////////////
    // Quote
    //////////////////////////////////

    private FrameLayout quoteContainer;
    private TextView quoteText;
    private String currentQuote = "";

    //////////////////////////////////
    // Forwarding
    //////////////////////////////////

//    private String sendUri;
//    private ArrayList<String> sendUriMultiple;
//    private int shareUser;
//    private String forwardDocDescriptor;
//    private boolean forwardDocIsDoc = true;
//    private String forwardText;
//    private String forwardTextRaw;
//    private String sendText;

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

        // Handling selection changed
        messageEditText.setOnSelectionListener(new SelectionListenerEditText.OnSelectedListener() {
            @Override
            public void onSelected(int selStart, int selEnd) {
                //TODO: Fix full select
                Editable text = messageEditText.getText();
                if (selEnd != selStart && text.charAt(selStart) == '@') {
                    if (text.charAt(selEnd - 1) == MENTION_BOUNDS_CHR) {
                        messageEditText.setSelection(selStart + 2, selEnd - 1);
                    } else if (text.length() >= 3 && text.charAt(selEnd - 2) == MENTION_BOUNDS_CHR) {
                        messageEditText.setSelection(selStart + 2, selEnd - 2);
                    }
                }
            }
        });

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

//        // Sharing
//        sendUri = getIntent().getStringExtra("send_uri");
//        sendUriMultiple = getIntent().getStringArrayListExtra("send_uri_multiple");
//        shareUser = getIntent().getIntExtra("share_user", 0);
//
//        //Forwarding
//        forwardText = getIntent().getStringExtra("forward_text");
//        forwardTextRaw = getIntent().getStringExtra("forward_text_raw");
//        sendText = getIntent().getStringExtra("send_text");
//        forwardDocDescriptor = getIntent().getStringExtra("forward_doc_descriptor");
//        forwardDocIsDoc = getIntent().getBooleanExtra("forward_doc_is_doc", true);
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
//        // Performing actions
//
//        if (sendUri != null && !sendUri.isEmpty()) {
//            sendUri(Uri.parse(sendUri), true);
//            sendUri = "";
//        }
//
//        if (sendUriMultiple != null && sendUriMultiple.size() > 0) {
//            for (String sendUri : sendUriMultiple) {
//                sendUri(Uri.parse(sendUri), false);
//            }
//            sendUriMultiple.clear();
//        }
//
//        if (sendText != null && !sendText.isEmpty()) {
//            messageEditText.setText(sendText);
//            sendText = "";
//        }
//
//        if (shareUser != 0) {
//            String userName = users().get(shareUser).getName().get();
//            String mentionTitle = "@".concat(userName);
//            ArrayList<Integer> mention = new ArrayList<Integer>();
//            mention.add(shareUser);
//            messenger().sendMessage(peer, mentionTitle, "[".concat(mentionTitle).concat("](people://".concat(Integer.toString(shareUser)).concat(")")), mention);
//            messenger().trackTextSend(peer);
//            shareUser = 0;
//        }
//
//        if (forwardTextRaw != null && !forwardTextRaw.isEmpty()) {
//            addQuote(forwardText, forwardTextRaw);
//            forwardText = "";
//            forwardTextRaw = "";
//        }
//
//        if (forwardDocDescriptor != null && !forwardDocDescriptor.isEmpty()) {
//            if (forwardDocIsDoc) {
//                messenger().sendDocument(peer, forwardDocDescriptor);
//                messenger().trackDocumentSend(peer);
//            } else {
//                sendUri(Uri.fromFile(new File(forwardDocDescriptor)), false);
//            }
//            forwardDocDescriptor = "";
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
                public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                    removedFromGroup.setVisibility(val ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // TODO: Rewrite to correct conversion from spannable to markdown

        // Converting messageEditText content to markdown
        Editable text = (Editable) messageEditText.getText().subSequence(0, messageEditText.getText().length());

        // Converting spans to markdown
        convertUrlSpansToMarkdownLinks(text);

        // Saving draft
        messenger().saveDraft(peer, text.toString());
    }

    // Message send

    @Override
    protected void onSendButtonPressed() {
        boolean useMD = false;

        Editable mdText = messageEditText.getText();
        String rawText = mdText.toString();
        ArrayList<Integer> mentions = convertUrlSpansToMarkdownLinks(mdText);
        if (mentions.size() > 0) useMD = true;

        String mdTextString = mdText.toString().replace(MENTION_BOUNDS_STR, "").trim();
        rawText = rawText.replace(MENTION_BOUNDS_STR, "").trim();

        if (currentQuote != null && !currentQuote.isEmpty()) {
            mdTextString = currentQuote.concat(mdTextString);
            rawText = quoteText.getText().toString().concat(rawText);
            goneView(quoteContainer);
            useMD = true;
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

        messenger().sendMessage(peer, rawText, useMD ? mdTextString : "", mentions);
        messenger().trackTextSend(peer);
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
                messenger().trackPhotoSend(peer);
            } else if (requestCode == REQUEST_VIDEO) {
                messenger().sendVideo(peer, pending_fileName);
                messenger().trackVideoSend(peer);
            } else if (requestCode == REQUEST_DOC) {
                if (data.getData() != null) {
                    execute(messenger().sendUri(peer, data.getData()), R.string.pick_downloading);
                } else if (data.hasExtra("picked")) {
                    ArrayList<String> files = data.getStringArrayListExtra("picked");
                    if (files != null) {
                        for (String s : files) {
                            messenger().sendDocument(peer, s);
                            messenger().trackDocumentSend(peer);
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
        mentionsAdapter = new MentionsAdapter(new ArrayList<GroupMember>(groupInfo.getMembers().get()), this, new MentionsAdapter.MentionsUpdatedCallback() {

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
                if (item != null && item instanceof GroupMember) {
                    UserVM user = users().get(((GroupMember) item).getUid());
                    String name = user.getName().get();
                    int userId = user.getId();

                    if (mentionStart != -1 && mentionStart + mentionSearchString.length() + 1 <= messageEditText.getText().length()) {

                        SpannableStringBuilder spannedMention = buildMention(userId, name);

                        Editable text = messageEditText.getText();
                        boolean spaceAppended = false;
                        if (text.length() > mentionStart + mentionSearchString.length() + 1) {
                            if (text.charAt(mentionSearchString.length() + 1) != ' ') {
                                spannedMention.append(' ');
                                spaceAppended = true;
                            }
                        } else {
                            spannedMention.append(' ');
                            spaceAppended = true;
                        }

                        text.replace(mentionStart, mentionStart + mentionSearchString.length() + 1, spannedMention);

                        int cursorPosition = mentionStart + spannedMention.length() + (spaceAppended ? 0 : 1);
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

        Editable text = messageEditText.getText();
        if (text.length() > 0 && text.charAt(text.length() - 1) != ' ') text.append(" ");

        SpannableStringBuilder spannedMention = buildMention(uid, name);

        text.append(spannedMention.append(", "));
        messageEditText.requestFocus();
        keyboardUtils.setImeVisibility(messageEditText, true);
    }

    private SpannableStringBuilder buildMention(int uid, String name) {
        String mention = "people://" + uid;
        MentionSpan span = new MentionSpan(mention, true);
        SpannableStringBuilder spannedMention = new SpannableStringBuilder("@" + MENTION_BOUNDS_STR + name + MENTION_BOUNDS_STR);
        spannedMention.setSpan(span, 0, spannedMention.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannedMention;
    }

    private ArrayList<Integer> convertUrlSpansToMarkdownLinks(Editable text) {
        int start;
        int end;
        boolean urlTitleEndsSpace;
        String url;
        String urlTitle;
        String mdUrl;
        URLSpan[] spans = messageEditText.getText().getSpans(0, text.length(), URLSpan.class);
        ArrayList<Integer> mentions = new ArrayList<Integer>();
        for (URLSpan span : spans) {
            start = text.getSpanStart(span);
            end = text.getSpanEnd(span);
            if (start != -1 && end <= text.length()) {
                url = span.getURL();
                urlTitle = text.toString().substring(start, end);
                urlTitleEndsSpace = urlTitle.endsWith(" ");
                urlTitle = urlTitle.trim();
                //if(Uri.parse(url).getScheme().equals("people") && !urlTitle.startsWith("@") )urlTitle = new String("@").concat(urlTitle);
                mdUrl = "[".concat(urlTitle).concat("](").concat(url).concat(")");
                if (urlTitleEndsSpace) mdUrl = mdUrl.concat(" ");
                boolean addMention = true;
                if (urlTitle.equals("@".concat(MENTION_BOUNDS_STR).concat(MENTION_BOUNDS_STR)) || urlTitle.equals("@")) {
                    mdUrl = "@";
                    addMention = false;
                }
                if (!urlTitle.contains("@")) {
                    mdUrl = urlTitle;
                    addMention = false;
                }
                if (addMention && span instanceof MentionSpan) {
                    mentions.add(Integer.parseInt(url.split("://")[1]));
                }
                text.replace(start, end, mdUrl);
            }
        }
        return mentions;
    }

    // Quotes

    public void addQuote(String quote, String rawQuote) {
//        if (quote != null && !quote.isEmpty()) {
//            quoteText.setText(bypass.markdownToSpannable(quote, true));
//        } else {
//            quoteText.setText(bypass.markdownToSpannable(rawQuote, true));
//        }
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

        // Text change state variables
        private boolean mentionErase;
        private int mentionEraseStart;
        private int eraseStart;
        private int eraseCount;
        private boolean isErase;
        private boolean isOneCharErase = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Notify about typing only when text is increased
            if (after > count && !isTypingDisabled) {
                messenger().onTyping(peer);
            }

            // Saving useful information about performed action on text
            // That can be used in afterTextChanged method

            // Saving erasing information
            // For one char erase keep start position
            isErase = after < count;
            isOneCharErase = isErase && (count == 1 || (count - after == 1));
            if (isOneCharErase) {
                eraseStart = start + count - 1;
            }

            // Detecting if starting to erase mention
            mentionErase = isOneCharErase && s.charAt(eraseStart) == MENTION_BOUNDS_CHR;
            if (mentionErase) {
                mentionEraseStart = eraseStart;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String str = s.toString();
            String firstPeace = str.substring(0, start + count);


            if (peer.getPeerType() == PeerType.GROUP) {
                //Open mentions
                if (count == 1 && s.charAt(start) == '@') {
                    showMentions(false);
                    forceMentionHide = false;
                    mentionSearchString = "";
                    lastMentionSearch = "";
                } else if (!forceMentionHide && firstPeace.contains("@")) {
                    showMentions(true);
                } else {
                    hideMentions();
                }

                //Set mentions query
                mentionStart = firstPeace.lastIndexOf("@");
                if (firstPeace.contains("@") && mentionStart + 1 < firstPeace.length()) {
                    mentionSearchString = firstPeace.substring(mentionStart + 1, firstPeace.length());
                    if (!mentionSearchString.startsWith(MENTION_BOUNDS_STR) && !mentionSearchString.isEmpty())
                        lastMentionSearch = mentionSearchString;
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

            //Escape mention bounds
            int escapeMentionEdit = s.toString().indexOf(" ".concat(MENTION_BOUNDS_STR));
            if (!isOneCharErase && escapeMentionEdit != -1) {
                //TODO do not append space if not needed
                s.replace(escapeMentionEdit, escapeMentionEdit + 2, MENTION_BOUNDS_STR.concat(" "));
                if (s.charAt(messageEditText.getSelectionStart() - 1) == MENTION_BOUNDS_CHR)
                    messageEditText.setSelection(messageEditText.getSelectionStart() + 1);
            }

            if (mentionErase) {
                int firstBound = s.subSequence(0, mentionEraseStart).toString().lastIndexOf(MENTION_BOUNDS_STR);
                //Delete mention
                if (mentionEraseStart > 0 && firstBound > 0 && s.charAt(firstBound - 1) == '@') {
                    for (URLSpan span : s.getSpans(firstBound, mentionEraseStart, URLSpan.class)) {
                        s.removeSpan(span);
                    }
                    s.replace(firstBound, mentionEraseStart, lastMentionSearch);
                    if (useForceMentionHide) {
                        hideMentions();
                        forceMentionHide = true;
                    }
                    //Delete mention bounds
                } else if (mentionEraseStart > 0 && s.charAt(mentionEraseStart - 1) == MENTION_BOUNDS_CHR) {
                    s.replace(mentionEraseStart - 2, mentionEraseStart, "");
                } else if (mentionEraseStart > 0) {
                    s.replace(mentionEraseStart - 1, mentionEraseStart, "");
                }
            }

            //Delete mention bounds after erase last character in name
            int emptyBoundsIndex = s.toString().indexOf(MENTION_BOUNDS_STR.concat(MENTION_BOUNDS_STR));
            if (isErase && emptyBoundsIndex != -1) {
                s.replace(emptyBoundsIndex, emptyBoundsIndex + 2, "");
            }

            //Delete useless bound
            if (s.toString().trim().length() == 1 && s.toString().trim().charAt(0) == MENTION_BOUNDS_CHR) {
                s.clear();
            }
        }
    }
}
