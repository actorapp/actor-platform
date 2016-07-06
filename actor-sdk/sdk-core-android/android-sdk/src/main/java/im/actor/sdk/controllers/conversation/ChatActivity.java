package im.actor.sdk.controllers.conversation;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import im.actor.core.entity.BotCommand;
import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.conversation.botcommands.CommandsAdapter;
import im.actor.sdk.controllers.conversation.mentions.MentionsAdapter;
import im.actor.sdk.controllers.conversation.messages.MessagesDefaultFragment;
import im.actor.sdk.controllers.conversation.messages.content.AudioHolder;
import im.actor.sdk.controllers.conversation.toolbar.ChatToolbarFragment;
import im.actor.sdk.controllers.conversation.view.FastShareAdapter;
import im.actor.sdk.controllers.settings.BaseActorChatActivity;
import im.actor.sdk.core.audio.VoiceCaptureActor;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.util.Randoms;
import im.actor.sdk.util.Screen;
import im.actor.core.utils.GalleryScannerActor;
import im.actor.sdk.view.ShareMenuButtonFactory;
import im.actor.sdk.view.TintDrawable;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.RecyclerListView;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.controllers.conversation.view.TypingDrawable;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.markdown.AndroidMarkdown;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;

import static im.actor.sdk.util.ViewUtils.expandMentions;
import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.hideView;
import static im.actor.sdk.util.ViewUtils.showView;
import static im.actor.sdk.util.ViewUtils.zoomInView;
import static im.actor.sdk.util.ViewUtils.zoomOutView;
import static im.actor.sdk.view.emoji.SmileProcessor.emoji;
import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

@SuppressWarnings("NullableProblems")
public class ChatActivity extends ActorEditTextActivity {

    public static final String EXTRA_CHAT_PEER = "chat_peer";

    //////////////////////////////////
    // Activity keys
    //////////////////////////////////
    public static final String STATE_FILE_NAME = "pending_file_name";
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;
    private static final int REQUEST_CONTACT = 5;
    private static final int PERMISSIONS_REQUEST_CAMERA = 6;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 7;
    private static final int PERMISSION_REQ_MEDIA = 11;

    // Peer of current chat
    private Peer peer;

    //////////////////////////////////
    // Configuration
    //////////////////////////////////

    private boolean isAutocompleteVisible = false;

    //////////////////////////////////
    // Voice messages
    //////////////////////////////////
    private View audioContainer;
    private View recordPoint;
    private View messageContainer;
    private View audioSlide;
    private int slideStart;
    private TextView audioTimer;
    private boolean isAudioVisible;
    private boolean isShareVisible;
    private int SLIDE_LIMIT;
    ActorRef voiceRecordActor;
    private String audioFile;
    private ImageView audioButton;

    //////////////////////////////////
    // Mentions
    //////////////////////////////////
    private HolderAdapter autocompleteAdapter;
    private CommandsAdapter commandsAdapter;
    private RecyclerListView autocompleteList;
    private String autocompleteString = "";
    private int autocompleteTriggerStart;

    //////////////////////////////////
    // Quote
    //////////////////////////////////
    private TextView quoteText;
    private FrameLayout quoteContainer;
    private String currentQuote = "";

    //////////////////////////////////
    // Forwarding
    //////////////////////////////////
    private String forwardText;
    private String forwardTextRaw;
    private String sendText;
    private AbsContent forwardContent;
    // Camera photo destination name
    private String pending_fileName;

    //////////////////////////////////
    //Share menu
    //////////////////////////////////
    private View shareContainer;
    private View shareMenuCaontainer;
    private int shareMenuMaxHeight = 0;
    private FastShareAdapter fastShareAdapter;


    //////////////////////////////////
    // Utility variables
    //////////////////////////////////
    // Lock typing during messageEditText change
    private boolean isTypingDisabled = false;
    // Is Activity opened from Compose
    private boolean isCompose = false;
    private Intent intent;
    private boolean textEditing = false;
    private long currentEditRid;
    private Animation.AnimationListener animationListener;
    private boolean isBot = false;
    private View emptyBotSend;
    private TextView emptyBotHint;
    private ImageView menuIconToChange;
    private TextView menuTitleToChange;
    private View.OnClickListener shareSendOcl;
    private View.OnClickListener defaultSendOcl;

    public static Intent build(Peer peer, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        // Reading peer of chat
        intent = getIntent();
        peer = Peer.fromUniqueId(intent.getExtras().getLong(EXTRA_CHAT_PEER));
        checkIsBot();
        if (saveInstance != null) {
            // Activity restore
            pending_fileName = saveInstance.getString(STATE_FILE_NAME, null);
        }

        super.onCreate(saveInstance);

        if (saveInstance == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(ChatToolbarFragment.create(peer), "toolbar")
                    .commit();
        }

        messageEditText.addTextChangedListener(new TextWatcherImp());
        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    hideShare();
                }
            }
        });

        //Voice record
        SLIDE_LIMIT = (int) (Screen.getDensity() * 180);
        audioContainer = findViewById(R.id.audioContainer);
        audioTimer = (TextView) findViewById(R.id.audioTimer);
        audioSlide = findViewById(R.id.audioSlide);
        recordPoint = findViewById(R.id.record_point);

        audioButton = (ImageView) findViewById(R.id.record_btn);
        audioButton.setVisibility(View.VISIBLE);
        audioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isAudioVisible) {
                        showAudio();
                        slideStart = (int) event.getX();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isAudioVisible) {
                        hideAudio(false);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (isAudioVisible) {
                        int slide = slideStart - (int) event.getX();
                        if (slide < 0) {
                            slide = 0;
                        }
                        if (slide > SLIDE_LIMIT) {
                            hideAudio(true);
                        } else {
                            slideAudio(slide);
                        }
                    }
                }
                return true;
            }
        });

        // Mentions
        autocompleteList = (RecyclerListView) findViewById(R.id.mentionsList);
        autocompleteList.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        //Quote
        quoteContainer = (FrameLayout) findViewById(R.id.quoteContainer);
        quoteContainer.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        quoteText = (TextView) findViewById(R.id.quote_text);
        findViewById(R.id.ib_close_quote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneView(quoteContainer);
                quoteText.setText("");
                currentQuote = "";
                if (textEditing) {
                    messageEditText.setText("");
                }
                textEditing = false;
                checkSendButton();

            }
        });

        //share menu
        View shareMenu = findViewById(R.id.share_menu_container);
        shareMenuCaontainer = shareMenu;
        shareMenuCaontainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        shareContainer = findViewById(R.id.closeMenuLayout);
        shareContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideShare();
                return false;
            }
        });


        View.OnClickListener shareMenuOCL = new View.OnClickListener() {
            @Override
            public void onClick(View item) {
                if (item.getId() == R.id.share_gallery) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/* video/*");
                    startActivityForResult(intent, REQUEST_GALLERY);
                } else if (item.getId() == R.id.share_camera) {
                    File externalFile = Environment.getExternalStorageDirectory();
                    if (externalFile == null) {
                        Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                    } else {
                        String externalPath = externalFile.getAbsolutePath();
                        String exportPathBase = externalPath + "/" + ActorSDK.sharedActor().getAppName() + "/" + ActorSDK.sharedActor().getAppName() + " images" + "/";
                        new File(exportPathBase).mkdirs();

                        pending_fileName = exportPathBase + "capture_" + Randoms.randomId() + ".jpg";
                    }
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "camera - no permission :c");
                        ActivityCompat.requestPermissions(ChatActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSIONS_REQUEST_CAMERA);

                    } else {
                        startCamera();
                    }
                } else if (item.getId() == R.id.share_video) {

                    File externalFile = Environment.getExternalStorageDirectory();
                    if (externalFile == null) {
                        Toast.makeText(ChatActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                    } else {
                        String externalPath = externalFile.getAbsolutePath();
                        String exportPathBase = externalPath + "/" + ActorSDK.sharedActor().getAppName() + "/" + ActorSDK.sharedActor().getAppName() + " video" + "/";
                        new File(exportPathBase).mkdirs();

                        pending_fileName = exportPathBase + "capture_" + Randoms.randomId() + ".mp4";

                        Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pending_fileName)));
                        startActivityForResult(i, REQUEST_VIDEO);
                    }
                } else if (item.getId() == R.id.share_file) {
                    startActivityForResult(Intents.pickFile(ChatActivity.this), REQUEST_DOC);
                } else if (item.getId() == R.id.share_location) {
                    startActivityForResult(Intents.pickLocation(ChatActivity.this), REQUEST_LOCATION);
                } else if (item.getId() == R.id.share_contact) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } else if (item.getId() == R.id.share_hide) {
                    //just hide
                }

                //hide it
                hideShare();
            }
        };
        defaultSendOcl = shareMenuOCL;

        ArrayList<ShareMenuField> menuFields = new ArrayList<>();

        menuFields.add(new ShareMenuField(getString(R.string.share_menu_camera), R.id.share_camera, R.drawable.share_camera_selector, shareMenuOCL));
        menuFields.add(new ShareMenuField(getString(R.string.share_menu_file), R.id.share_file, R.drawable.share_file_selector, shareMenuOCL));
        menuFields.add(new ShareMenuField(getString(R.string.share_menu_gallery), R.id.share_gallery, R.drawable.share_gallery_selector, shareMenuOCL));
        try {
            Class.forName("com.google.android.gms.maps.GoogleMap");
            menuFields.add(new ShareMenuField(getString(R.string.share_menu_location), R.id.share_location, R.drawable.share_location_selector, shareMenuOCL));
        } catch (ClassNotFoundException e) {
            //ignore
        }
        menuFields.add(new ShareMenuField(getString(R.string.share_menu_video), R.id.share_video, R.drawable.share_video_selector, shareMenuOCL));
        menuFields.add(new ShareMenuField(getString(R.string.share_menu_contact), R.id.share_contact, R.drawable.share_contact_selector, shareMenuOCL));

        ActorSDK.sharedActor().getDelegate().addCustomShareMenuFields(menuFields);

        if (menuFields.size() % 2 != 0) {
            menuFields.add(new ShareMenuField(R.drawable.attach_hide2,
                    ActorSDK.sharedActor().style.getBackyardBackgroundColor(),
                    "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }));
        }
        FrameLayout row = (FrameLayout) findViewById(R.id.share_row_one);
        shareMenu.setBackgroundDrawable(new InsetDrawable(new ColorDrawable(ActorSDK.sharedActor().style.getMainBackgroundColor()), 0, Screen.dp(2), 0, 0));
        shareMenu.setPadding(0, 0, 0, 0);
        boolean first = true;
        int menuItemSize = Screen.dp(80);
        int screenWidth = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? Screen.getWidth() : Screen.getHeight();
        int distance = screenWidth / (menuFields.size() / 2 + menuFields.size() % 2);
        int initialMargin = distance / 2 - menuItemSize / 2;
        int marginFromStart = initialMargin;
        int secondRowTopMargin = Screen.dp(80);
        int shareIconSize = Screen.dp(60);

        for (int i = 0; i < menuFields.size(); i++) {
            ShareMenuField f = menuFields.get(i);

            LinearLayout shareItem = new LinearLayout(this);
            shareItem.setOrientation(LinearLayout.VERTICAL);
            shareItem.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView title = new TextView(this);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            title.setText(f.getTitle());

            ImageView icon = new ImageView(this);
            icon.setClickable(true);
            if (f.getSelector() != 0) {
                icon.setBackgroundResource(f.getSelector());
            } else {
                icon.setBackgroundDrawable(ShareMenuButtonFactory.get(f.getColor(), this));
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                icon.setImageResource(f.getIcon());
            }

            shareItem.addView(icon, shareIconSize, shareIconSize);
            shareItem.addView(title);

            View.OnClickListener l = v -> {
                hideShare();
                f.getOnClickListener().onClick(icon);
            };
            icon.setId(f.getId());
            icon.setOnClickListener(l);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(menuItemSize, menuItemSize);
            params.setMargins(marginFromStart, first ? 0 : secondRowTopMargin, initialMargin, 0);

            if (i == menuFields.size() - 1) {
                menuIconToChange = icon;
                menuTitleToChange = title;
                defaultSendOcl = l;

                params.setMargins(marginFromStart, first ? 0 : secondRowTopMargin, 0, 0);

            }
            row.addView(shareItem, params);
            if (!first) {
                marginFromStart += distance;
            }
            first = !first;
        }

        menuIconToChange.setTag(R.id.icon, ((ImageView) menuIconToChange).getDrawable());
        menuIconToChange.setTag(R.id.background, menuIconToChange.getBackground());
        menuTitleToChange.setTag(menuTitleToChange.getText().toString());

        handleIntent();

        shareSendOcl = v -> {
            Set<String> strings = fastShareAdapter.getSelectedVM().get();
            for (String s : strings.toArray(new String[strings.size()])) {
                execute(messenger().sendUri(peer, Uri.fromFile(new File(s))));
            }
            fastShareAdapter.clearSelected();
            hideShare();
        };

        RecyclerView fastShare = (RecyclerView) findViewById(R.id.fast_share);
        if (ActorSDK.sharedActor().isFastShareEnabled()) {
            fastShareAdapter = new FastShareAdapter(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            fastShare.setAdapter(fastShareAdapter);
            fastShare.setLayoutManager(layoutManager);
            StateListDrawable background = ShareMenuButtonFactory.get(ActorSDK.sharedActor().style.getMainColor(), ChatActivity.this);

            fastShareAdapter.getSelectedVM().subscribe((val, valueModel) -> {
                if (val.size() > 0) {
                    menuIconToChange.setBackgroundDrawable(background);
                    menuIconToChange.setImageResource(R.drawable.conv_send);
                    menuIconToChange.setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN);
                    menuTitleToChange.setText(getString(R.string.chat_doc_send) + "(" + val.size() + ")");
                    menuIconToChange.setOnClickListener(shareSendOcl);
                    menuIconToChange.setPadding(Screen.dp(10), 0, Screen.dp(5), 0);
                } else {

                    menuIconToChange.setBackgroundDrawable((Drawable) menuIconToChange.getTag(R.id.background));
                    menuIconToChange.setImageDrawable((Drawable) menuIconToChange.getTag(R.id.icon));
                    menuIconToChange.setColorFilter(null);
                    menuIconToChange.setOnClickListener(defaultSendOcl);
                    menuTitleToChange.setText((String) menuTitleToChange.getTag());
                    menuIconToChange.setPadding(0, 0, 0, 0);
                }
            });
        } else {
            fastShare.setVisibility(View.GONE);
        }

        emptyBotSend = findViewById(R.id.botEmptyTextBlock);
        emptyBotHint = (TextView) findViewById(R.id.botEmptyHint);

        checkEmptyBot();
    }

    private void startCamera() {
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(pending_fileName))),
                REQUEST_PHOTO);
    }

    private void handleIntent() {

        //Forwarding
        forwardText = intent.getStringExtra("forward_text");
        forwardTextRaw = intent.getStringExtra("forward_text_raw");
        sendText = intent.getStringExtra("send_text");
        try {
            forwardContent = AbsContent.parse(intent.getByteArrayExtra("forward_content"));
        } catch (Exception e) {

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Notify old chat closed
        messenger().onConversationClosed(peer);

        peer = Peer.fromUniqueId(intent.getExtras().getLong(EXTRA_CHAT_PEER));
        setFragment(null);

        onPerformBind();
        this.intent = intent;
        handleIntent();
    }


    @Override
    protected Fragment onCreateFragment() {
        if (ActorSDK.sharedActor().getDelegate().getChatIntent(peer, false) != null) {
            ActorIntent chatIntent = ActorSDK.sharedActor().getDelegate().getChatIntent(peer, false);
            if (chatIntent instanceof BaseActorChatActivity) {
                return ((BaseActorChatActivity) chatIntent).getChatFragment(peer);
            } else {
                return MessagesDefaultFragment.create(peer);
            }
        } else {
            return MessagesDefaultFragment.create(peer);
        }
    }

    // Activity lifecycle

    @Override
    public void onResume() {
        checkIsBot();
        super.onResume();

        emojiKeyboard.setPeer(peer);

        voiceRecordActor = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
            @Override
            public VoiceCaptureActor create() {
                return new VoiceCaptureActor(ChatActivity.this, new VoiceCaptureActor.VoiceCaptureCallback() {
                    @Override
                    public void onRecordProgress(final long time) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                audioTimer.setText(messenger().getFormatter().formatDuration((int) (time / 1000)));
                            }
                        });
                    }

                    @Override
                    public void onRecordCrash() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideAudio(true);
                            }
                        });
                    }

                    @Override
                    public void onRecordStop(long progress) {
                        if (progress < 1200) {
                            //Cancel
                        } else {
                            messenger().sendVoice(peer, (int) progress, audioFile);
                        }
                    }
                });
            }


        }).changeDispatcher("voice_capture_dispatcher"), "actor/voice_capture");


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

        if (sendText != null && !sendText.isEmpty()) {
            messageEditText.setText(sendText);
            sendText = "";
        }

        if (forwardTextRaw != null && !forwardTextRaw.isEmpty()) {
            addQuote(forwardText, forwardTextRaw);
            forwardText = "";
            forwardTextRaw = "";
        }

        if (forwardContent != null) {
            messenger().forwardContent(peer, forwardContent);
            forwardContent = null;
        }

        if (isBot) {
            emptyBotSend.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            TextView emptyBotSendText = (TextView) emptyBotSend.findViewById(R.id.empty_bot_text);
            emptyBotSendText.setTextColor(ActorSDK.sharedActor().style.getMainColor());

            emptyBotSendText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messenger().sendMessage(peer, "/start");
                }
            });

            checkEmptyBot();
        }

        if (isShareVisible) {
            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Show());
        }

    }

    public void checkIsBot() {
        isBot = (peer.getPeerType() == PeerType.PRIVATE && users().get(peer.getPeerId()).isBot());
    }

    public void checkEmptyBot() {
        if (isBot) {
            messenger().isStarted(peer.getPeerId())
                    .then(empty -> {
                        if (empty) {
                            showView(emptyBotSend);
                            showView(emptyBotHint);
                        } else {
                            hideView(emptyBotSend);
                            hideView(emptyBotHint);
                        }
                    });
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

            // Bind user blocked
            inputBlockedText.setText(R.string.profile_settings_unblock);
            bind(users().get(peer.getPeerId()).getIsBlocked(), (val, valueModel) -> {
                inputBlockContainer.setVisibility(val ? View.VISIBLE : View.GONE);
            });
            inputBlockedText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    execute(messenger().unblockUser(peer.getPeerId()));
                }
            });

            // Bind empty bot about
            if (isBot) {
                bind(users().get(peer.getPeerId()).getAbout(), (about, valueModel) -> {
                    emptyBotHint.setText((about != null && !about.isEmpty()) ? about : getString(R.string.chat_empty_bot_about));
                });
            }

        } else if (peer.getPeerType() == PeerType.GROUP) {

            // Loading group
            GroupVM group = groups().get(peer.getPeerId());
            if (group == null) {
                finish();
                return;
            }

            // Binding membership flag to inputBlockContainer panel
            bind(group.isMember(), (val, Value) -> {
                inputBlockContainer.setVisibility(val ? View.GONE : View.VISIBLE);
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        voiceRecordActor.send(PoisonPill.INSTANCE);
        AudioHolder.stopPlaying();
        // Saving draft
        messenger().saveDraft(peer, messageEditText.getText().toString());
        messenger().getGalleryScannerActor().send(new GalleryScannerActor.Hide());
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
        autocompleteString = "";

        if (rawText.length() == 0) {
            return;
        }

        // Hack for full screen mode
        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageEditText, false);
            messageEditText.clearFocus();
        }

        if (textEditing) {
            execute(messenger().updateMessage(peer, rawText, currentEditRid), new CommandCallback<Void>() {
                @Override
                public void onResult(Void res) {

                }

                @Override
                public void onError(final Exception e) {
                    runOnUiThread(() -> {
                        RpcException re = (RpcException) e;
                        String error;
                        if (re.getTag().equals("NOT_IN_TIME_WINDOW")) {
                            error = getString(R.string.edit_message_error_slowpoke);
                        } else if (re.getTag().equals("NOT_LAST_MESSAGE")) {
                            error = getString(R.string.edit_message_error_not_last);
                        } else {
                            error = re.getMessage();
                        }
                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
            goneView(quoteContainer);
            checkSendButton();
            textEditing = false;
        } else {
            messenger().sendMessage(peer, rawText);
        }
    }

    @Override
    protected void onAttachButtonClicked() {

        if (shareMenuMaxHeight == 0) {
            shareMenuMaxHeight = Screen.dp(245);
        }

        // Trying to open custom share menu
        if (ActorSDK.sharedActor().getDelegate().onAttachMenuClicked(this)) {
            return;
        }

        // Opening default share menu
        if (shareMenuCaontainer.getVisibility() == View.VISIBLE) {
            hideShare();
        } else {
            shareContainer.setVisibility(View.VISIBLE);
            showShare();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                keyboardUtils.setImeVisibility(messageEditText, false);
                messageEditText.clearFocus();
            }
        }
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
                MediaScannerConnection.scanFile(this, new String[]{pending_fileName}, new String[]{"image/jpeg"}, null);
            } else if (requestCode == REQUEST_VIDEO) {
                messenger().sendVideo(peer, pending_fileName);
                MediaScannerConnection.scanFile(this, new String[]{pending_fileName}, new String[]{"image/jpeg"}, null);
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
            } else if (requestCode == REQUEST_CONTACT) {
                ArrayList<String> phones = new ArrayList<String>();
                ArrayList<String> emails = new ArrayList<String>();
                String name = "";
                byte[] photo = null;

                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {


                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phonesCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null);
                        if (phonesCursor.moveToFirst()) {
                            int phoneColumnIndex = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                            do {
                                phones.add(phonesCursor.getString(phoneColumnIndex));
                            } while (phonesCursor.moveToNext());
                            phonesCursor.close();
                        }

                    }
                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));


                    Cursor emailCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                            null, null);
                    if (emailCursor != null && emailCursor.moveToFirst()) {
                        int emailColumnIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        do {
                            emails.add(emailCursor.getString(emailColumnIndex));
                        } while (emailCursor.moveToNext());
                        emailCursor.close();
                    }

                    Uri photoUri = Uri.withAppendedPath(contactData, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                    Cursor photoCursor = getContentResolver().query(photoUri,
                            new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
                    if (photoCursor == null) {

                    } else {
                        try {
                            if (photoCursor.moveToFirst()) {
                                photo = photoCursor.getBlob(0);

                            }
                        } finally {
                            photoCursor.close();
                        }
                    }

                }

                messenger().sendContact(peer, name, phones, emails, photo != null ? (Base64.encodeToString(photo, Base64.NO_WRAP)) : null);

            } else if (requestCode == REQUEST_LOCATION) {
                messenger().sendLocation(peer, data.getDoubleExtra("longitude", 0), data.getDoubleExtra("latitude", 0), data.getStringExtra("street"), data.getStringExtra("place"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Mentions
    // Bot commands

    private void showAutoComplete(boolean initEmpty, boolean isMentions) {
        if (isAutocompleteVisible) {
            return;
        }
        isAutocompleteVisible = true;

        if (!isMentions) {
            autocompleteAdapter = new CommandsAdapter(peer.getPeerId(), this, new CommandsAdapter.CommandsUpdatedCallback() {
                @Override
                public void onMentionsUpdated(int oldRowsCount, int newRowsCount) {
                    onMentionsChanged(oldRowsCount, newRowsCount);
                }
            });
        } else {
            GroupVM groupInfo = groups().get(peer.getPeerId());
            autocompleteAdapter = new MentionsAdapter(groupInfo.getId(), this, new MentionsAdapter.MentionsUpdatedCallback() {

                @Override
                public void onMentionsUpdated(int oldRowsCount, int newRowsCount) {
                    onMentionsChanged(oldRowsCount, newRowsCount);
                }
            }, initEmpty);
        }

//        if(autocompleteAdapter.getCount() == 0){
//            isAutocompleteVisible = false;
//            return;
//        }

        autocompleteList.setAdapter(autocompleteAdapter);
        autocompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null && item instanceof MentionFilterResult) {

                    String origMention = ((MentionFilterResult) item).getMentionString();

                    if (autocompleteTriggerStart != -1 && autocompleteTriggerStart + autocompleteString.length() + 1 <= messageEditText.getText().length()) {

                        String mentionString = origMention + (autocompleteTriggerStart == 0 ? ": " : " ");

                        Editable text = messageEditText.getText();

                        int cursorPosition = autocompleteTriggerStart + mentionString.length();

                        text.replace(autocompleteTriggerStart, autocompleteTriggerStart + autocompleteString.length() + 1, mentionString);

                        messageEditText.setSelection(cursorPosition, cursorPosition);
                    }
                    hideMentions();
                } else if (item != null && item instanceof BotCommand) {
                    messenger().sendMessage(peer, "/".concat(((BotCommand) item).getSlashCommand()));
                    messageEditText.setText("");
                    hideMentions();
                }
            }
        });
        hideShare();

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

    public void insertMention(int uid) {
        UserVM user = users().get(uid);
        String name = user.getName().get();
        String nick = user.getNick().get();
        Editable text = messageEditText.getText();
        if (text.length() > 0 && text.charAt(text.length() - 1) != ' ') text.append(" ");

        String mentionString = ((nick != null && !nick.isEmpty()) ? "@" + nick : name) + (messageEditText.getText().length() > 0 ? " " : ": ");

        text.append(mentionString);
        messageEditText.requestFocus();
        keyboardUtils.setImeVisibility(messageEditText, true);
    }


    // Quotes

    public void addQuote(String quote, String rawQuote) {
        textEditing = false;
        if (quote != null && !quote.isEmpty()) {
            quoteText.setText(quote);
        } else {
            quoteText.setText(rawQuote);
        }
        currentQuote = rawQuote;
        hideShare();
        quoteText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_editor_format_quote_gray), null, null, null);
        showView(quoteContainer);
        checkSendButton();

    }

    // Back button handling

    @Override
    public void onBackPressed() {
        if (isAutocompleteVisible) {
            hideMentions();
        } else if (isShareVisible) {
            hideShare();
        } else if (emojiKeyboard.isShowing()) {
            emojiKeyboard.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Small Hack to a
        keyboardUtils.setImeVisibility(messageEditText, false);
        messageEditText.clearFocus();
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

    public void onEditTextMessage(long rid, String text) {
        currentQuote = null;
        forwardText = null;
        forwardTextRaw = null;
        textEditing = true;
        currentEditRid = rid;
        quoteText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_content_create), null, null, null);
        quoteText.setText(R.string.edit_message);
        messageEditText.setText(text);
        hideShare();
        showView(quoteContainer);
        //we don't force check send button here, because it forces from text watcher
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

            char autocompleteTriggerChar = '@';
            String autocompleteTriggerString = "@";


            if (peer.getPeerType() == PeerType.GROUP || isBot) {

                if (isBot) {
                    autocompleteTriggerChar = '/';
                    autocompleteTriggerString = "/";
                }
                //Open mentions
                if (count == 1 && s.charAt(start) == autocompleteTriggerChar && !str.endsWith(" ")) {
                    showAutoComplete(false, !isBot);
                    autocompleteString = "";

                } else if (currentWord.startsWith(autocompleteTriggerString) && !str.endsWith(" ")) {
                    showAutoComplete(true, !isBot);
                } else {
                    hideMentions();
                }

                //Set mentions query
                autocompleteTriggerStart = firstPeace.lastIndexOf(autocompleteTriggerString);
                if (currentWord.startsWith(autocompleteTriggerString) && currentWord.length() > 1) {
                    autocompleteString = currentWord.substring(1, currentWord.length());
                } else {
                    autocompleteString = "";
                }

                if (autocompleteString.equals(" ")) {
                    hideMentions();
                } else if (autocompleteAdapter != null) {
                    //mentionsDisplay.initSearch(autocompleteString, false);
                    if (autocompleteAdapter instanceof MentionsAdapter) {
                        ((MentionsAdapter) autocompleteAdapter).setQuery(autocompleteString.toLowerCase());
                    } else if (autocompleteAdapter instanceof CommandsAdapter) {
                        ((CommandsAdapter) autocompleteAdapter).setQuery(autocompleteString.toLowerCase());
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkSendButton(s.length() > 0);
        }
    }

    public void checkSendButton() {
        checkSendButton(messageEditText.getText().length() > 0);
    }

    public void checkSendButton(boolean hasText) {
        if (hasText || (currentQuote != null && !currentQuote.isEmpty())) {
            sendButton.setTint(ActorSDK.sharedActor().style.getConvSendEnabledColor());
            sendButton.setEnabled(true);
            zoomInView(sendButton);
            zoomOutView(audioButton);
        } else {
            sendButton.setTint(ActorSDK.sharedActor().style.getConvSendDisabledColor());
            sendButton.setEnabled(false);
            zoomInView(audioButton);
            zoomOutView(sendButton);
        }
    }

    private void showAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissions", "recordAudio - no permission :c");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE}, PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        if (isAudioVisible) {
            return;
        }
        isAudioVisible = true;

        hideView(attachButton);
        hideView(messageEditText);
        hideView(emojiButton);
        hideView(sendContainer);

        audioFile = ActorSDK.sharedActor().getMessenger().getInternalTempFile("voice_msg", "opus");


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        long id = VoiceCaptureActor.LAST_ID.incrementAndGet();
        voiceRecordActor.send(new VoiceCaptureActor.Start(audioFile));

        slideAudio(0);
        audioTimer.setText("00:00");

        TranslateAnimation animation = new TranslateAnimation(Screen.getWidth(), 0, 0, 0);
        animation.setDuration(160);
        audioContainer.clearAnimation();
        audioContainer.setAnimation(animation);
        audioContainer.animate();
        audioContainer.setVisibility(View.VISIBLE);


        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.2f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setRepeatMode(AlphaAnimation.REVERSE);
        alphaAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        recordPoint.clearAnimation();
        recordPoint.setAnimation(alphaAnimation);
        recordPoint.animate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autocompleteAdapter != null) {
            autocompleteAdapter.dispose();
        }
        if (fastShareAdapter != null) {
            fastShareAdapter.release();
            fastShareAdapter = null;
        }
    }

    private void hideAudio(boolean cancel) {
        if (!isAudioVisible) {
            return;
        }
        isAudioVisible = false;

        showView(attachButton);
        showView(messageEditText);
        showView(emojiButton);
        showView(sendContainer);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        voiceRecordActor.send(new VoiceCaptureActor.Stop(cancel));
        TranslateAnimation animation = new TranslateAnimation(0, Screen.getWidth(), 0, 0);
        animation.setDuration(160);
        audioContainer.clearAnimation();
        audioContainer.setAnimation(animation);
        audioContainer.animate();
        audioContainer.setVisibility(View.GONE);
        messageEditText.requestFocus();

    }

    private boolean animationInProgress = false;

    private void showShare() {
        keyboardUtils.setImeVisibility(messageEditText, false);
        messageEditText.clearFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ_MEDIA);

            } else {
                showShareChecked();
            }
        } else {
            showShareChecked();
        }
        menuIconToChange.invalidateDrawable(menuIconToChange.getDrawable());
    }

    private void showShareChecked() {
        if (animationInProgress) {
            return;
        }
        if (animationListener == null) {
            animationListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    animationInProgress = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animationInProgress = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
        }

        TranslateAnimation animation = new TranslateAnimation(0, 0, Screen.getHeight(), 0);
        animation.setDuration(160);
        animation.setAnimationListener(animationListener);
        shareMenuCaontainer.clearAnimation();
        shareMenuCaontainer.setAnimation(animation);
        shareMenuCaontainer.animate();
        shareMenuCaontainer.setVisibility(View.VISIBLE);
        isShareVisible = true;
        if (ActorSDK.sharedActor().isFastShareEnabled()) {
            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Show());
        }
    }

    public void hideShare() {
        if (!isShareVisible || animationInProgress) {
            return;
        }
        isShareVisible = false;
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, Screen.getHeight());
        animation.setDuration(160);

        animation.setAnimationListener(animationListener);
        shareMenuCaontainer.clearAnimation();
        shareMenuCaontainer.setAnimation(animation);
        shareMenuCaontainer.animate();
        shareMenuCaontainer.setVisibility(View.GONE);
        shareContainer.setVisibility(View.GONE);
        if (ActorSDK.sharedActor().isFastShareEnabled()) {
            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Hide());
        }

    }

    private void slideAudio(int value) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(audioSlide, "translationX", audioSlide.getX(), -value);
        oa.setDuration(0);
        oa.start();
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        } else if (requestCode == PERMISSION_REQ_MEDIA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showShareChecked();
            }
        }
    }
}
