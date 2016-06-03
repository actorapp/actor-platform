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
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
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
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.conversation.botcommands.CommandsAdapter;
import im.actor.sdk.controllers.conversation.mentions.MentionsAdapter;
import im.actor.sdk.controllers.conversation.messages.AudioHolder;
import im.actor.sdk.controllers.conversation.view.FastShareAdapter;
import im.actor.sdk.controllers.settings.BaseActorChatActivity;
import im.actor.sdk.core.audio.VoiceCaptureActor;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.util.Randoms;
import im.actor.sdk.util.Screen;
import im.actor.core.utils.GalleryScannerActor;
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
    public static final String EXTRA_CHAT_COMPOSE = "compose";
    public static final String STATE_FILE_NAME = "pending_file_name";
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;
    private static final int REQUEST_CONTACT = 5;
    private static final int PERMISSIONS_REQUEST_CAMERA = 6;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 7;
    private static final int PERMISSIONS_REQUEST_FOR_CALL = 8;
    private static final int PERMISSION_REQ_MEDIA = 11;
    public static final int MAX_USERS_FOR_CALLS = 5;
    // Peer of current chat
    private Peer peer;

    //////////////////////////////////
    // Configuration
    //////////////////////////////////

    //////////////////////////////////
    // Model
    //////////////////////////////////
    // Toolbar title root view
    private View barView;

    //////////////////////////////////
    // Toolbar views
    //////////////////////////////////
    // Toolbar unread counter
    private TextView counter;
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
    private String sendUri;

    //////////////////////////////////
    // Forwarding
    //////////////////////////////////
    private ArrayList<String> sendUriMultiple;
    private int shareUser;
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
    private Menu menu;
    private boolean isBot = false;
    private View emptyBotSend;
    private TextView emptyBotHint;

    public static Intent build(Peer peer, boolean compose, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        intent.putExtra(EXTRA_CHAT_COMPOSE, compose);
        return intent;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        // Reading peer of chat
        intent = getIntent();
        peer = Peer.fromUniqueId(intent.getExtras().getLong(EXTRA_CHAT_PEER));
        checkIsBot();
        if (saveInstance == null) {
            // Set compose state for auto-showing menu
            isCompose = intent.getExtras().getBoolean(EXTRA_CHAT_COMPOSE, false);
        } else {
            // Activity restore
            pending_fileName = saveInstance.getString(STATE_FILE_NAME, null);
        }

        super.onCreate(saveInstance);

        onCreateToolbar();

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
        TableLayout shareMenu = (TableLayout) findViewById(R.id.share_menu);
        shareMenu.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        findViewById(R.id.fast_share).setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        shareMenuCaontainer = findViewById(R.id.share_container);
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

        final TextView contactText = (TextView) findViewById(R.id.contact_text);
        final View shareContact = findViewById(R.id.share_contact);
        findViewById(R.id.share_hide).setVisibility(View.GONE);

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

        findViewById(R.id.share_gallery).setOnClickListener(shareMenuOCL);
        findViewById(R.id.share_video).setOnClickListener(shareMenuOCL);
        findViewById(R.id.share_camera).setOnClickListener(shareMenuOCL);
        shareContact.setOnClickListener(shareMenuOCL);
        findViewById(R.id.share_file).setOnClickListener(shareMenuOCL);
        findViewById(R.id.share_hide).setOnClickListener(shareMenuOCL);
        View shareLocation = findViewById(R.id.share_location);
        shareLocation.setOnClickListener(shareMenuOCL);
        ActorSDK.sharedActor().getDelegate().onShareMenuCreated(shareMenu);

        handleIntent();

        try {
            Class.forName("com.google.android.gms.maps.GoogleMap");
        } catch (ClassNotFoundException e) {
            shareLocation.setVisibility(View.GONE);
            findViewById(R.id.share_hide).setVisibility(View.VISIBLE);
            findViewById(R.id.location_text).setVisibility(View.INVISIBLE);
        }


        final ImageButton shareMenuSend = (ImageButton) findViewById(R.id.share_send);
        shareMenuSend.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        shareMenuSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> strings = fastShareAdapter.getSelectedVM().get();
                for (String s : strings.toArray(new String[strings.size()])) {
                    execute(messenger().sendUri(peer, Uri.fromFile(new File(s))));
                }
                fastShareAdapter.clearSelected();
                hideShare();
            }
        });

        RecyclerView fastShare = (RecyclerView) findViewById(R.id.fast_share);
        if (ActorSDK.sharedActor().isFastShareEnabled()) {
            fastShareAdapter = new FastShareAdapter(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            fastShare.setAdapter(fastShareAdapter);
            fastShare.setLayoutManager(layoutManager);

            fastShareAdapter.getSelectedVM().subscribe(new ValueChangedListener<Set<String>>() {
                @Override
                public void onChanged(Set<String> val, Value<Set<String>> valueModel) {
                    if (val.size() > 0) {
                        shareContact.setVisibility(View.INVISIBLE);
                        shareMenuSend.setVisibility(View.VISIBLE);
                        contactText.setText(getString(R.string.chat_doc_send) + "(" + val.size() + ")");
                    } else {
                        shareContact.setVisibility(View.VISIBLE);
                        shareMenuSend.setVisibility(View.INVISIBLE);
                        contactText.setText(getString(R.string.share_menu_contact));
                    }
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
        // Sharing
        sendUri = intent.getStringExtra("send_uri");
        sendUriMultiple = intent.getStringArrayListExtra("send_uri_multiple");
        shareUser = intent.getIntExtra("share_user", 0);

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
        MessagesFragment fragment;
        if (ActorSDK.sharedActor().getDelegate().getChatIntent(peer, false) != null) {
            ActorIntent chatIntent = ActorSDK.sharedActor().getDelegate().getChatIntent(peer, false);
            if (chatIntent instanceof BaseActorChatActivity) {
                return ((BaseActorChatActivity) chatIntent).getChatFragment(peer);
            } else {
                return MessagesFragment.create(peer);
            }
        } else {
            return MessagesFragment.create(peer);
        }
    }

    protected void onCreateToolbar() {
        // Loading Toolbar header views
        // Binding to real data is performed in onResume method
        ActorStyle style = ActorSDK.sharedActor().style;
        barView = LayoutInflater.from(this).inflate(R.layout.bar_conversation, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        setToolbar(barView, layout, false);
        findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        counter = (TextView) barView.findViewById(R.id.counter);

        counter.setTextColor(style.getDialogsCounterTextColor());
        counter.setBackgroundResource(R.drawable.ic_counter_circle);
        counter.getBackground().setColorFilter(style.getDialogsCounterBackgroundColor(), PorterDuff.Mode.MULTIPLY);
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
                    ActorSDK.sharedActor().startProfileActivity(ChatActivity.this, peer.getPeerId());
                } else if (peer.getPeerType() == PeerType.GROUP) {
                    ActorSDK.sharedActor().startGroupInfoActivity(ChatActivity.this, peer.getPeerId());
                } else {
                    // Nothing to do
                }
            }
        });
    }

    // Activity lifecycle

    @Override
    public void onResume() {
        checkIsBot();
        super.onResume();

        if (peer.getPeerType() == PeerType.PRIVATE && menu != null) {
            menu.findItem(R.id.add_to_contacts).setVisible(users().get(peer.getPeerId()).isContact().get());
            invalidateOptionsMenu();
        }

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

            // Binding User Avatar to Toolbar
            bind(barAvatar, user.getId(), user.getAvatar(), user.getName());

            // Binding User name to Toolbar
            bind(barTitle, user.getName());

            bind(user.getIsVerified(), new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {

                    barTitle.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            val ? new TintDrawable(
                                    getResources().getDrawable(R.drawable.ic_verified_user_black_18dp),
                                    ActorSDK.sharedActor().style.getVerifiedColor()) : null,
                            null);
                }
            });


            // Binding User presence to Toolbar
            bind(barSubtitle, user);

            // Binding User typing to Toolbar
            bindPrivateTyping(barTyping, barTypingContainer, barSubtitle, messenger().getTyping(user.getId()));

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

            // Binding membership flag to inputBlockContainer panel
            bind(messenger().getGroups().get(peer.getPeerId()).isMember(), new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> Value) {
                    inputBlockContainer.setVisibility(val ? View.GONE : View.VISIBLE);
                }
            });
        }

        bindGlobalCounter(new ValueChangedListener<Integer>() {
            @Override
            public void onChanged(Integer val, Value<Integer> valueModel) {
                if (val != null && val > 0) {
                    counter.setText(Integer.toString(val));
                    showView(counter);
                } else {
                    hideView(counter);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        voiceRecordActor.send(PoisonPill.INSTANCE);
        AudioHolder.stopPlaying();
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RpcException re = (RpcException) e;
                            String error = "";
                            if (re.getTag().equals("NOT_IN_TIME_WINDOW")) {
                                error = getString(R.string.edit_message_error_slowpoke);
                            } else if (re.getTag().equals("NOT_LAST_MESSAGE")) {
                                error = getString(R.string.edit_message_error_not_last);
                            }
                            Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                        }
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

    // Options Menu

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.menu = menu;

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

        boolean callsEnabled = ActorSDK.sharedActor().isCallsEnabled();
        if (callsEnabled) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                callsEnabled = !users().get(peer.getPeerId()).isBot();
            } else if (peer.getPeerType() == PeerType.GROUP) {
                callsEnabled = groups().get(peer.getPeerId()).getMembersCount() <= MAX_USERS_FOR_CALLS;
            }
        }
        menu.findItem(R.id.call).setVisible(callsEnabled);

        if (peer.getPeerType() == PeerType.PRIVATE) {
            bind(users().get(peer.getPeerId()).isContact(), new ValueChangedListener<Boolean>() {
                @Override
                public void onChanged(Boolean val, Value<Boolean> valueModel) {
                    menu.findItem(R.id.add_to_contacts).setVisible(!val);
                    invalidateOptionsMenu();
                }
            });
        }

        // Hide unsupported files menu
        menu.findItem(R.id.files).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        keyboardUtils.setImeVisibility(messageEditText, false);
        messageEditText.clearFocus();
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.clear) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.alert_delete_all_messages_text)
                    .setPositiveButton(R.string.alert_delete_all_messages_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            execute(messenger().clearChat(peer), R.string.progress_common,
                                    new CommandCallback<Void>() {
                                        @Override
                                        public void onResult(Void res) {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Toast.makeText(getApplicationContext(), R.string.toast_unable_clear_chat, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

        } else if (i == R.id.leaveGroup) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.alert_leave_group_message)
                            .replace("%1$s", groups().get(peer.getPeerId()).getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            execute(messenger().leaveGroup(peer.getPeerId()), R.string.progress_common, new CommandCallback<Void>() {
                                @Override
                                public void onResult(Void res) {

                                }

                                @Override
                                public void onError(final Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.toast_unable_leave, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

        } else if (i == R.id.contact) {
            ActorSDK.sharedActor().startProfileActivity(ChatActivity.this, peer.getPeerId());

        } else if (i == R.id.groupInfo) {
            ActorSDK.sharedActor().startGroupInfoActivity(ChatActivity.this, peer.getPeerId());

        } else if (i == R.id.files) {// startActivity(Intents.openDocs(chatType, chatId, ChatActivity.this));

        } else if (i == R.id.add_to_contacts) {
            execute(messenger().addContact(peer.getPeerId()));
        }

        if (ActorSDK.sharedActor().isCallsEnabled()) {
            if (item.getItemId() == R.id.call) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "call - no permission :c");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK},
                            PERMISSIONS_REQUEST_FOR_CALL);

                } else {
                    startCall();
                }
            }

//            Context context = ActorSDK.sharedActor().getMessenger().getContext();
//            Intent callIntent = new Intent(context, CallActivity.class);
//            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//            callIntent.putExtra("callId", 0);
//            context.startActivity(callIntent);
//            context.startActivity(callIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void startCall() {
        Command<Long> cmd;
        if (peer.getPeerType() == PeerType.PRIVATE) {
            cmd = messenger().doCall(peer.getPeerId());

        } else {
            cmd = messenger().doGroupCall(peer.getPeerId());
        }
        execute(cmd, R.string.progress_common);

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

        if (barAvatar != null) {
            barAvatar.unbind();
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
        } else if (requestCode == PERMISSIONS_REQUEST_FOR_CALL) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall();
            }
        }
    }
}
