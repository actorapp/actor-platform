package im.actor.messenger.app.fragment.chat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.base.BaseActivity;
import im.actor.messenger.app.emoji.SmileProcessor;
import im.actor.messenger.app.keyboard.KeyboardStatusListener;
import im.actor.messenger.app.keyboard.emoji.EmojiKeyboard;
import im.actor.messenger.app.util.RandomUtil;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.util.io.IOUtils;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.app.view.SelectionListenerEdittext;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.model.Messenger;
import im.actor.model.entity.GroupMember;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;
import in.uncod.android.bypass.Bypass;
import in.uncod.android.bypass.MentionSpan;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.users;
import static im.actor.messenger.app.emoji.SmileProcessor.emoji;
import static im.actor.messenger.app.view.ViewUtils.expandMentions;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;


public class ChatActivity extends BaseActivity {

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;

    private static final Character MENTION_BOUNDS_CHR = '\u200b';
    private static final String MENTION_BOUNDS_STR = MENTION_BOUNDS_CHR.toString();


    private Peer peer;

    private Messenger messenger;

    private SelectionListenerEdittext messageBody;
    private TintImageView sendButton;
    private ImageButton attachButton;
    private View kicked;

    // Action bar
    private View barView;
    private AvatarView barAvatar;
    private TextView barTitle;
    private View barSubtitleContainer;
    private TextView barSubtitle;
    private View barTypingContainer;
    private ImageView barTypingIcon;
    private TextView barTyping;

    private String fileName;

    private KeyboardHelper keyboardUtils;

    private boolean isTypingDisabled = false;

    private boolean isCompose = false;
    private EmojiKeyboard emojiKeyboard;
    private boolean isMentionsVisible = false;
    private MentionsAdapter mentionsAdapter;
    private ListView mentionsList;
    private FrameLayout quoteContainer;
    private TextView quoteText;
    private ImageView quoteClose;
    private String mentionSearchString = "";
    private int mentionStart;
    private boolean isOneCharErase = false;
    Bypass bypass = new Bypass();

    private boolean useMentionOneItemAutocomplete = false;
    private boolean useForceMentionHide = false;
    private boolean forceMentionHide = useForceMentionHide;
    private String lastMentionSearch = "";
    private String sendUri;
    private ArrayList<String> sendUriMultiple;
    private int shareUser;
    private String currentQuote = "";
    private String forwardDocDescriptor;
    private boolean forwardDocIsDoc = true;
    private String forwardText;
    private String forwardTextRaw;
    private String sendText;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        if (saveInstance != null) {
            fileName = saveInstance.getString("pending_file_name", null);
        }

        keyboardUtils = new KeyboardHelper(this);

        peer = Peer.fromUniqueId(getIntent().getExtras().getLong(Intents.EXTRA_CHAT_PEER));

        isCompose = saveInstance == null && getIntent().getExtras().getBoolean(Intents.EXTRA_CHAT_COMPOSE, false);

        messenger = messenger();

        // Init action bar

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        // Action bar header

        barView = LayoutInflater.from(this).inflate(R.layout.bar_conversation, null);
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
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(barView, layout);
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


        // Init view

        setContentView(R.layout.activity_dialog);

        getWindow().setBackgroundDrawable(null);

        if (saveInstance == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.messagesFragment, MessagesFragment.create(peer))
                    .commit();
        }

        messageBody = (SelectionListenerEdittext) findViewById(R.id.et_message);
        //messageBody.setMovementMethod(LinkMovementMethod.getInstance());
        messageBody.addTextChangedListener(new TextWatcher() {

            boolean mentionErase;
            int mentionEraseStart;
            int eraseStart;
            int eraseCount;
            boolean isErase;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (after > count && !isTypingDisabled) {
                    messenger.onTyping(peer);
                }
                isErase = after < count;
                isOneCharErase = isErase && (count == 1 || (count - after == 1));
                if (isOneCharErase) eraseStart = start + count - 1;

                mentionErase = isOneCharErase && s.charAt(eraseStart) == MENTION_BOUNDS_CHR;
                if (mentionErase) mentionEraseStart = eraseStart;

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
                    if (s.charAt(messageBody.getSelectionStart() - 1) == MENTION_BOUNDS_CHR)
                        messageBody.setSelection(messageBody.getSelectionStart() + 1);
                }

                if (mentionErase) {
                    int firstBound = s.subSequence(0, mentionEraseStart).toString().lastIndexOf(MENTION_BOUNDS_STR);
                    //Delete mention
                    if (mentionEraseStart > 0 && firstBound > 0 && s.charAt(firstBound -1) == '@' ) {
                        for (URLSpan span : s.getSpans(firstBound, mentionEraseStart, URLSpan.class)) {
                            s.removeSpan(span);
                        }
                        s.replace(firstBound, mentionEraseStart, lastMentionSearch);
                        if (useForceMentionHide) {
                            hideMentions();
                            forceMentionHide = true;
                        }
                    //Delete mention bounds
                    }else if (mentionEraseStart > 0 && s.charAt(mentionEraseStart - 1) == MENTION_BOUNDS_CHR) {
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
        });


        messageBody.setOnSelectionListener(new SelectionListenerEdittext.OnSelectedListener() {
            @Override
            public void onSelected(int selStart, int selEnd) {
                //Fix full select
                Editable text = messageBody.getText();
                if (selEnd != selStart && text.charAt(selStart) == '@') {
                    if (text.charAt(selEnd - 1) == MENTION_BOUNDS_CHR) {
                        messageBody.setSelection(selStart + 2, selEnd - 1);
                    } else if (text.length() >= 3 && text.charAt(selEnd - 2) == MENTION_BOUNDS_CHR) {
                        messageBody.setSelection(selStart + 2, selEnd - 2);
                    }
                }

            }
        });

        messageBody.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                if (messenger().isSendByEnterEnabled()) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });
        messageBody.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                if (i == EditorInfo.IME_ACTION_DONE) {
                    sendMessage();
                    return true;
                }
                if (messenger().isSendByEnterEnabled()) {
                    if (keyEvent != null && i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });

        kicked = findViewById(R.id.kickedFromChat);

        sendButton = (TintImageView) findViewById(R.id.ib_send);
        sendButton.setResource(R.drawable.conv_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        attachButton = (ImageButton) findViewById(R.id.ib_attach);
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                            fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".jpg";
                            startActivityForResult(
                                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                            .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName))),
                                    REQUEST_PHOTO);
                        } else if (item.getItemId() == R.id.takeVideo) {

                            File externalFile = getExternalFilesDir(null);
                            if (externalFile == null) {
                                Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                                return true;
                            }
                            String externalPath = externalFile.getAbsolutePath();
                            new File(externalPath + "/actor/").mkdirs();

                            fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".mp4";

                            Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName)));
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
        });


        final ImageView emojiButton = (ImageView) findViewById(R.id.ib_emoji);
        emojiKeyboard = new EmojiKeyboard(this);
//        emojiKeyboard.setOnStickerClickListener(new OnStickerClickListener() {
//            @Override
//            public void onStickerClick(Sticker sticker) {
//                messenger().sendPhoto(peer, getStickerProcessor().getStickerPath(sticker));
//            }
//        });


        emojiKeyboard.setKeyboardStatusListener(new KeyboardStatusListener() {


            @Override
            public void onDismiss() {
                emojiButton.setImageResource(R.drawable.ic_emoji);
            }

            @Override
            public void onShow() {
                emojiButton.setImageResource(R.drawable.ic_keyboard);
            }

        });

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emojiKeyboard.toggle(messageBody);
            }
        });

        // Mentions
        mentionsList = (ListView) findViewById(R.id.mentionsList);

        //Quote
        quoteContainer = (FrameLayout) findViewById(R.id.quoteContainer);
        quoteClose = (ImageView) findViewById(R.id.ib_close_quote);
        quoteText = (TextView) findViewById(R.id.quote_text);
        quoteClose.setOnClickListener(new View.OnClickListener() {
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
    public void onResume() {
        super.onResume();

        if (peer.getPeerType() == PeerType.PRIVATE) {
            final UserVM user = users().get(peer.getPeerId());
            if (user == null) {
                finish();
                return;
            }

            bind(barAvatar, user.getId(), user.getAvatar(), user.getName());
            bind(barTitle, user.getName());
            bind(barSubtitle, barSubtitleContainer, user);
            bindPrivateTyping(barTyping, barTypingContainer, barSubtitle, messenger().getTyping(user.getId()));

            kicked.setVisibility(View.GONE);
        } else if (peer.getPeerType() == PeerType.GROUP) {
            GroupVM group = groups().get(peer.getPeerId());
            if (group == null) {
                finish();
                return;
            }

            bind(barAvatar, group.getId(), group.getAvatar(), group.getName());
            bind(barTitle, group.getName());
            // Subtitle is always visible for Groups
            // barSubtitleContainer.setVisibility(View.VISIBLE);
            bind(barSubtitle, barSubtitleContainer, group);
            bindGroupTyping(barTyping, barTypingContainer, barSubtitle, messenger().getGroupTyping(group.getId()));

            bind(messenger().getGroups().get(peer.getPeerId()).isMember(), new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                    kicked.setVisibility(val ? View.GONE : View.VISIBLE);
                }
            });
        }

        if (isCompose) {
            messageBody.requestFocus();
            keyboardUtils.setImeVisibility(messageBody, true);
        }
        isCompose = false;

        isTypingDisabled = true;
        String text = messenger().loadDraft(peer);
        if (text != null) {
            messageBody.setText((emoji().processEmojiCompatMutable(bypass.markdownToSpannable(text, true), SmileProcessor.CONFIGURATION_BUBBLES)));
        } else {
            messageBody.setText("");
        }
        messageBody.setSelection(messageBody.getText().length());
        isTypingDisabled = false;

        if (sendUri != null && !sendUri.isEmpty()) {
            sendUri(Uri.parse(sendUri), true);
            sendUri = "";
        }

        if (sendUriMultiple != null && sendUriMultiple.size() > 0) {
            for (String sendUri : sendUriMultiple) {
                sendUri(Uri.parse(sendUri), false);
            }
            sendUriMultiple.clear();
        }

        if(sendText!= null && !sendText.isEmpty()){
            messageBody.setText(sendText);
            sendText = "";
        }

        if (shareUser != 0) {
            String userName = users().get(shareUser).getName().get();
            String mentionTitle = "@".concat(userName);
            ArrayList<Integer> mention = new ArrayList<Integer>();
            mention.add(shareUser);
            messenger().sendMessage(peer, mentionTitle, "[".concat(mentionTitle).concat("](people://".concat(Integer.toString(shareUser)).concat(")")), mention);
            messenger().trackTextSend(peer);
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
                messenger().trackDocumentSend(peer);
            } else {
                sendUri(Uri.fromFile(new File(forwardDocDescriptor)), false);
            }
            forwardDocDescriptor = "";
        }

    }

    private void sendMessage() {

        boolean useMD = false;

        Editable mdText = messageBody.getText();
        String rawText = mdText.toString();
        ArrayList<Integer> mentions = convertUrlspansToMarkdownLinks(mdText);
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

        messageBody.setText("");
        mentionSearchString = "";

        if (rawText.length() == 0) {
            return;
        }

        // Hack for full screen mode
        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageBody, false);
        }

        messenger().sendMessage(peer, rawText, useMD ? mdTextString : "", mentions);
        messenger().trackTextSend(peer);
    }

    private ArrayList<Integer> convertUrlspansToMarkdownLinks(Editable text) {
        int start;
        int end;
        boolean urlTitleEndsSpace;
        String url;
        String urlTitle;
        String mdUrl;
        URLSpan[] spans = messageBody.getText().getSpans(0, text.length(), URLSpan.class);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                if (data.getData() != null) {
                    sendUri(data.getData(), true);
                }
            } else if (requestCode == REQUEST_PHOTO) {
                messenger().sendPhoto(peer, fileName);
                messenger().trackPhotoSend(peer);
            } else if (requestCode == REQUEST_VIDEO) {
                messenger().sendVideo(peer, fileName);
                messenger().trackVideoSend(peer);
            } else if (requestCode == REQUEST_DOC) {
                if (data.getData() != null) {
                    sendUri(data.getData(), true);
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

    private void sendUri(final Uri uri, final boolean showDialog) {
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                if (showDialog) {
                    progressDialog = new ProgressDialog(ChatActivity.this);
                    progressDialog.setMessage(getString(R.string.pick_downloading));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.TITLE};
                String picturePath;
                String mimeType;
                String fileName;


                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                    mimeType = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
                    fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[2]));
                    if (mimeType == null) {
                        mimeType = "?/?";
                    }
                    cursor.close();
                } else {
                    picturePath = uri.getPath();
                    fileName = new File(uri.getPath()).getName();
                    int index = fileName.lastIndexOf(".");
                    if (index > 0) {
                        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(index + 1));
                    } else {
                        mimeType = "?/?";
                    }
                }

                if (picturePath == null || !uri.getScheme().equals("file")) {
                    File externalFile = AppContext.getContext().getExternalFilesDir(null);
                    if (externalFile == null) {
                        return null;
                    }
                    String externalPath = externalFile.getAbsolutePath();

                    File dest = new File(externalPath + "/Actor/");
                    dest.mkdirs();

                    File outputFile = new File(dest, "upload_" + RandomUtil.randomId() + ".jpg");
                    picturePath = outputFile.getAbsolutePath();

                    try {
                        IOUtils.copy(getContentResolver().openInputStream(uri), new File(picturePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                if (fileName == null) {
                    fileName = picturePath;
                }

                if (mimeType.startsWith("video/")) {
                    messenger().sendVideo(peer, picturePath, fileName);
                    messenger().trackVideoSend(peer);
                } else if (mimeType.startsWith("image/")) {
                    messenger().sendPhoto(peer, picturePath, new File(fileName).getName());
                    messenger().trackPhotoSend(peer);
                } else {
                    messenger().sendDocument(peer, picturePath, new File(fileName).getName());
                    messenger().trackDocumentSend(peer);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (showDialog) progressDialog.dismiss();
            }
        }.execute();
    }

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

                    if (mentionStart != -1 && mentionStart + mentionSearchString.length() + 1 <= messageBody.getText().length()) {

                        SpannableStringBuilder spannedMention = buildMention(userId, name);

                        Editable text = messageBody.getText();
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
                        messageBody.setSelection(cursorPosition, cursorPosition);
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
        if (mentionsAdapter != null)
            if (newRowsCount == 1 && !isOneCharErase && !forceMentionHide && useMentionOneItemAutocomplete) {
                mentionsList.performItemClick(mentionsAdapter.getView(0, null, null), 0, mentionsAdapter.getItemId(0));
            } else {
                expandMentions(mentionsList, oldRowsCount, newRowsCount);
            }
    }

    public void onAvatarLongClick(int uid) {
        UserVM user = users().get(uid);
        String name = user.getName().get();


        Editable text = messageBody.getText();
        if (text.length() > 0 && text.charAt(text.length() - 1) != ' ') text.append(" ");

        SpannableStringBuilder spannedMention = buildMention(uid, name);

        text.append(spannedMention.append(", "));
        messageBody.requestFocus();
        keyboardUtils.setImeVisibility(messageBody, true);

    }

    @NotNull
    private SpannableStringBuilder buildMention(int uid, String name) {
        String mention = "people://".concat(Integer.toString(uid));
        MentionSpan span = new MentionSpan(mention, true);
        SpannableStringBuilder spannedMention = new SpannableStringBuilder("@".concat(MENTION_BOUNDS_STR).concat(name).concat(MENTION_BOUNDS_STR));
        spannedMention.setSpan(span, 0, spannedMention.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannedMention;
    }

    public void addQuote(String quote, String rawQuote) {
        if(quote!=null && !quote.isEmpty()){
            quoteText.setText(bypass.markdownToSpannable(quote, true));
        }else{
            quoteText.setText(bypass.markdownToSpannable(rawQuote, true));
        }
        currentQuote = rawQuote;
        showView(quoteContainer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        if (peer.getPeerType() == PeerType.PRIVATE) {
            menu.findItem(R.id.contact).setVisible(true);
        } else {
            menu.findItem(R.id.contact).setVisible(false);
        }

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

        menu.findItem(R.id.files).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fileName != null) {
            outState.putString("pending_file_name", fileName);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileName = savedInstanceState.getString("pending_file_name", null);
    }

    @Override
    public void onPause() {
        super.onPause();
        emojiKeyboard.destroy();
        Editable text = (Editable) messageBody.getText().subSequence(0, messageBody.getText().length());

        convertUrlspansToMarkdownLinks(text);
        messenger.saveDraft(peer, text.toString());
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
}
