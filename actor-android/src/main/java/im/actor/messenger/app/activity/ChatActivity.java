package im.actor.messenger.app.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.droidkit.mvvm.ui.Listener;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarActivity;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.app.view.TypingDrawable;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.audio.OpusRecorder;
import im.actor.messenger.core.actors.chat.ChatActionsActor;
import im.actor.messenger.core.actors.send.*;
import im.actor.messenger.core.actors.typing.MyTypingActor;
import im.actor.messenger.model.*;
import im.actor.messenger.settings.ChatSettings;
import im.actor.messenger.settings.NotificationSettings;
import im.actor.messenger.storage.DialogStorage;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.util.*;
import im.actor.messenger.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.droidkit.actors.ActorSystem.system;
import static im.actor.messenger.app.view.ViewUtils.*;
import static im.actor.messenger.core.actors.AppStateBroker.stateBroker;
import static im.actor.messenger.core.actors.groups.GroupsActor.groupUpdates;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

public class ChatActivity extends BaseBarActivity implements Listener<GroupState> {

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_VIDEO = 2;
    private static final int REQUEST_DOC = 3;
    private static final int REQUEST_LOCATION = 4;

    private int chatType;
    private int chatId;

    private EditText messageBody;
    private TintImageView sendButton;
    private ImageButton attachButton;

    private View customView;

    private AvatarView avatar;
    private TextView title;
    private View subtitleContainer;
    private TextView subtitle;
    private View typingContainer;
    private ImageView typingIcon;
    private TextView typing;

    private View recordingPanel;
    private ImageView audioMessage;

    private String fileName;

    private View cancelView;

    private TextView recordTimer;

    private View kicked;

    private KeyboardHelper keyboardUtils;

    private boolean isTypingDisabled = false;

    private boolean isCompose = false;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);

        keyboardUtils = new KeyboardHelper(this);

        chatType = getIntent().getExtras().getInt(Intents.EXTRA_CHAT_TYPE);
        chatId = getIntent().getExtras().getInt(Intents.EXTRA_CHAT_ID);
        if (saveInstance != null) {
            isCompose = false;
        } else {
            isCompose = getIntent().getExtras().getBoolean(Intents.EXTRA_CHAT_COMPOSE, false);
        }

        // Init action bar

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        customView = LayoutInflater.from(this).inflate(R.layout.bar_conversation, null);
        title = (TextView) customView.findViewById(R.id.title);
        subtitleContainer = customView.findViewById(R.id.subtitleContainer);
        typingIcon = (ImageView) customView.findViewById(R.id.typingImage);
        typingIcon.setImageDrawable(new TypingDrawable());
        typing = (TextView) customView.findViewById(R.id.typing);
        subtitle = (TextView) customView.findViewById(R.id.subtitle);
        typingContainer = customView.findViewById(R.id.typingContainer);
        typingContainer.setVisibility(View.INVISIBLE);
        avatar = (AvatarView) customView.findViewById(R.id.avatarPreview);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(customView, layout);
        customView.findViewById(R.id.titleContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatType == DialogType.TYPE_USER) {
                    startActivity(Intents.openProfile(chatId, ChatActivity.this));
                } else if (chatType == DialogType.TYPE_GROUP) {
                    startActivity(Intents.openGroup(chatId, ChatActivity.this));
                }
            }
        });
//        customView.findViewById(R.id.backContainer).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        // Init view

        setContentView(R.layout.activity_dialog);

        getWindow().setBackgroundDrawable(null);

        getFragmentManager().beginTransaction()
                .add(R.id.messagesFragment, MessagesFragment.create(chatType, chatId))
                .commit();

        messageBody = (EditText) findViewById(R.id.et_message);
        messageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (after > count && !isTypingDisabled) {
                    MyTypingActor.myTyping().onType(chatType, chatId);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (BuildConfig.ENABLE_VOICE) {
                    if (s.length() > 0) {
                        sendButton.setVisibility(View.VISIBLE);
                        audioMessage.setVisibility(View.GONE);
                    } else {
                        sendButton.setVisibility(View.GONE);
                        audioMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (s.length() > 0) {
                        sendButton.setTint(getResources().getColor(R.color.conv_send_enabled));
                        sendButton.setEnabled(true);
                    } else {
                        sendButton.setTint(getResources().getColor(R.color.conv_send_disabled));
                        sendButton.setEnabled(false);
                    }
                }
            }
        });
        messageBody.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                if (ChatSettings.getInstance().isSendByEnter()) {
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
                if (ChatSettings.getInstance().isSendByEnter()) {
                    if (keyEvent != null && i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });

        kicked = findViewById(R.id.kickedFromChat);
        kicked.setVisibility(View.GONE);

        findViewById(R.id.kickedButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ChatActivity.this)
                        .setMessage(R.string.alert_delete_group_title)
                        .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                groupUpdates().deleteChat(chatId);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(false);
            }
        });

        recordingPanel = findViewById(R.id.recordingPanel);
        recordingPanel.setVisibility(View.GONE);

        audioMessage = (ImageView) findViewById(R.id.audioMessage);
        cancelView = findViewById(R.id.cancelSlide);
        recordTimer = (TextView) findViewById(R.id.recordTimer);

        audioMessage.setOnTouchListener(new View.OnTouchListener() {

            private int startX;
            private boolean isActive = false;
            private long recordStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    system().actorOf(OpusRecorder.recorder()).send(new OpusRecorder.StartRecord());
                    startX = (int) event.getX();
                    audioMessage.setImageResource(R.drawable.conv_voice_pressed);

                    cancelView.setTranslationX(0);

                    recordingPanel.setVisibility(View.VISIBLE);
                    recordingPanel.setTranslationX(recordingPanel.getWidth());
                    recordingPanel.setAlpha(0);

                    recordingPanel.animate()
                            .translationX(0)
                            .alpha(1)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(150).start();

                    recordTimer.setText("00:00");
                    recordTimer.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isActive) {
                                recordTimer.setText(Formatter.duration(
                                        (int) ((SystemClock.uptimeMillis() - recordStartTime) / 1000)));
                                recordTimer.postDelayed(this, 500);
                            }
                        }
                    });
                    recordStartTime = SystemClock.uptimeMillis();
                    isActive = true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!isActive) {
                        return false;
                    }
                    int delta = (int) (startX - event.getX()) - Screen.dp(32);
                    if (delta < 0) {
                        delta = 0;
                    }
                    if (delta > Screen.dp(180)) {
                        system().actorOf(OpusRecorder.recorder()).send(new OpusRecorder.AbortRecord());
                        audioMessage.setImageResource(R.drawable.conv_voice_normal);
                        recordingPanel.setVisibility(View.GONE);
                        isActive = false;
                    } else {
                        cancelView.setTranslationX(-delta);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!isActive) {
                        return false;
                    }
                    system().actorOf(OpusRecorder.recorder()).send(new OpusRecorder.SendAudio(chatType, chatId));
                    audioMessage.setImageResource(R.drawable.conv_voice_normal);
                    recordingPanel.setVisibility(View.GONE);
                    isActive = false;
                }
                return true;
            }
        });

        if (!BuildConfig.ENABLE_VOICE) {
            audioMessage.setVisibility(View.GONE);
            audioMessage.setEnabled(false);
        }

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

                if (BuildConfig.ENABLE_CHROME) {
                    popup.getMenuInflater().inflate(R.menu.attach_popup_chrome, popup.getMenu());
                } else {
                    popup.getMenuInflater().inflate(R.menu.attach_popup, popup.getMenu());
                }

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

                            fileName = externalPath + "/actor/capture_" + RandomUtil.randomId() + ".jpg";

                            Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                    .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(fileName)));
                            startActivityForResult(i, REQUEST_VIDEO);
                            return true;
                        } else if (item.getItemId() == R.id.file) {
                            startActivityForResult(Intents.pickFile(ChatActivity.this), REQUEST_DOC);
                        } else if (item.getItemId() == R.id.location) {
                            startActivityForResult(com.droidkit.pickers.Intents.pickLocation(ChatActivity.this), REQUEST_LOCATION);
                        }
                        return false;
                    }
                });
                popup.show();

            }
        });
    }

    private void updateImeConfig() {
//        if (ChatSettings.getInstance().isSendByEnter()) {
//            messageBody.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
//            messageBody.setImeOptions(EditorInfo.IME_ACTION_SEND);
//        } else {
//            messageBody.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
//            messageBody.setImeOptions(EditorInfo.IME_ACTION_SEND);
//        }
        messageBody.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        messageBody.setImeOptions(EditorInfo.IME_ACTION_SEND);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateImeConfig();

        if (chatType == DialogType.TYPE_USER) {
            final UserModel user = users().get(chatId);

            if (user == null) {
                finish();
                return;
            }

            avatar.setEmptyDrawable(AvatarDrawable.create(user, 18, this));
            getBinder().bind(user.getAvatar(), new Listener<Avatar>() {
                @Override
                public void onUpdated(Avatar a) {
                    if (a != null) {
                        avatar.bindFastAvatar(38, a);
                    } else {
                        avatar.unbind();
                    }
                }
            });

            getBinder().bindText(title, user.getNameModel());

            getBinder().bind(user.getPresence(), new Listener<UserPresence>() {
                @Override
                public void onUpdated(UserPresence presence) {
                    updateUserStatus(presence, TypingModel.privateChatTyping(user.getId()).getValue());
                }
            });
            getBinder().bind(TypingModel.privateChatTyping(user.getId()), new Listener<Boolean>() {
                @Override
                public void onUpdated(Boolean aBoolean) {
                    updateUserStatus(user.getPresence().getValue(), aBoolean);
                }
            });
        } else if (chatType == DialogType.TYPE_GROUP) {
            final GroupModel groupInfo = groups().get(chatId);

            if (groupInfo == null) {
                finish();
                return;
            }


            avatar.setEmptyDrawable(AvatarDrawable.create(groupInfo, 18, this));
            getBinder().bind(groupInfo.getAvatarModel(), new Listener<Avatar>() {
                @Override
                public void onUpdated(Avatar a) {
                    if (a != null) {
                        avatar.bindFastAvatar(38, a);
                    } else {
                        avatar.unbind();
                    }
                }
            });

            getBinder().bindText(title, groupInfo.getTitleModel());

            subtitle.setVisibility(View.VISIBLE);

            getBinder().bind(groupInfo.getStateModel(), this);

            getBinder().bind(TypingModel.groupChatTyping(chatId), new Listener<int[]>() {
                @Override
                public void onUpdated(int[] ints) {
                    updateGroupStatus(groupInfo.getOnlineModel().getValue(), ints);
                }
            });
            getBinder().bind(groupInfo.getOnlineModel(), new Listener<int[]>() {
                @Override
                public void onUpdated(int[] ints) {
                    updateGroupStatus(ints, TypingModel.groupChatTyping(chatId).getValue());
                }
            });
        } else if (chatType == DialogType.TYPE_NOTIFICATIONS) {
            title.setText("Notifications");
        }

        int left = 0;
        int right = 0;

//        if (chatType == DialogType.TYPE_USER) {
//            left = R.drawable.conv_secure_name;
//        }

        if (!NotificationSettings.getInstance().convValue(DialogUids.getDialogUid(chatType, chatId)).getValue()) {
            right = R.drawable.conv_mute;
        }

        title.setCompoundDrawablesWithIntrinsicBounds(left, 0, right, 0);

        if (isCompose) {
            messageBody.requestFocus();
            keyboardUtils.setImeVisibility(messageBody, true);
        }
        isCompose = false;

        stateBroker().onConversationOpen(chatType, chatId);

        isTypingDisabled = true;
        String text = DialogStorage.draftStorage().get(DialogUids.getDialogUid(chatType, chatId));
        if (text != null) {
            messageBody.setText(text);
        } else {
            messageBody.setText("");
        }
        isTypingDisabled = false;

//        messageBody.requestFocus();
//        messageBody.post(new Runnable() {
//            @Override
//            public void run() {
//                messageBody.requestFocus();
//                if (messageBody.getText().length() > 0) {
//                    messageBody.setSelection(0, messageBody.getText().length());
//                }
//            }
//        });
    }

    private void updateUserStatus(UserPresence presence, boolean isTyping) {
        String s = Formatter.formatPresence(presence, users().get(chatId).getRaw().getSex());
        if (s == null) {
            subtitleContainer.setVisibility(View.GONE);
        } else {
            subtitleContainer.setVisibility(View.VISIBLE);
            subtitle.setText(s);
        }

        if (isTyping) {
            typing.setText(R.string.typing_private);
            showView(typingContainer);
            hideView(subtitle);
        } else {
            hideView(typingContainer);
            showView(subtitle);
        }
    }

    private void updateGroupStatus(int[] onlines, int[] typings) {
        if (typings.length > 0) {
            typing.setText(Formatter.formatTyping(typings));
            showView(typingContainer);
            hideView(subtitle);
        } else {
            if (onlines.length == 1) {
                SpannableStringBuilder builder = new SpannableStringBuilder(
                        getString(R.string.chat_group_members).replace("{0}", onlines[0] + ""));
                builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                subtitle.setText(builder);
            } else {
                SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.chat_group_members).replace("{0}", onlines[0] + "") + ", ");
                builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                builder.append(getString(R.string.chat_group_members_online).replace("{0}", onlines[1] + ""));
                subtitle.setText(builder);
            }
            hideView(typingContainer);
            showView(subtitle);
        }
    }

    private void sendMessage() {
        final String text = messageBody.getText().toString().trim();
        messageBody.setText("");
        if (text.length() == 0) {
            return;
        }

        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageBody, false);
        }

        MessageDeliveryActor.messageSender().sendText(chatType, chatId, text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                if (data.getData() != null) {
                    sendUri(data.getData());
                }
            } else if (requestCode == REQUEST_PHOTO) {
                MessageDeliveryActor.messageSender().sendPhoto(chatType, chatId, fileName);
            } else if (requestCode == REQUEST_VIDEO) {
                MessageDeliveryActor.messageSender().sendVideo(chatType, chatId, fileName);
            } else if (requestCode == REQUEST_DOC) {
                if (data.getData() != null) {
                    sendUri(data.getData());
                } else if (data.hasExtra("picked")) {
                    ArrayList<String> files = data.getStringArrayListExtra("picked");
                    if (files != null) {
                        for (String s : files) {
                            MessageDeliveryActor.messageSender().sendDocument(chatType, chatId, s,
                                    new File(s).getName());
                        }
                    }
                }
            }
        }
    }

    private void sendUri(final Uri uri) {
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(ChatActivity.this);
                progressDialog.setMessage(getString(R.string.pick_downloading));
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.MIME_TYPE,
                        MediaStore.Video.Media.TITLE};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                String mimeType = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
                String fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[2]));
                if (mimeType == null) {
                    mimeType = "?/?";
                }
                cursor.close();

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

                if (mimeType.startsWith("video/")) {
                    MessageDeliveryActor.messageSender().sendVideo(chatType, chatId, picturePath);
                } else if (mimeType.startsWith("image/")) {
                    MessageDeliveryActor.messageSender().sendPhoto(chatType, chatId, picturePath);
                } else {
                    MessageDeliveryActor.messageSender().sendDocument(chatType, chatId, picturePath,
                            fileName);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        if (chatType == DialogType.TYPE_USER) {
            menu.findItem(R.id.contact).setVisible(true);
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            menu.findItem(R.id.call).setVisible(tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE);
        } else {
            menu.findItem(R.id.contact).setVisible(false);
            menu.findItem(R.id.call).setVisible(false);
        }

        if (chatType == DialogType.TYPE_GROUP) {
            menu.findItem(R.id.groupInfo).setVisible(true);
            menu.findItem(R.id.leaveGroup).setVisible(true);
        } else {
            menu.findItem(R.id.groupInfo).setVisible(false);
            menu.findItem(R.id.leaveGroup).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.clear:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_delete_all_messages_text)
                        .setPositiveButton(R.string.alert_delete_all_messages_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatActionsActor.actions().clearChat(chatType, chatId);
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.leaveGroup:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.alert_delete_group_title)
                        .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog2, int which) {
                                groupUpdates().leaveChat(chatId);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
                break;
            case R.id.contact:
                startActivity(Intents.openProfile(chatId, ChatActivity.this));
                break;
            case R.id.groupInfo:
                startActivity(Intents.openGroup(chatId, ChatActivity.this));
                break;
            case R.id.files:
                startActivity(Intents.openDocs(chatType, chatId, ChatActivity.this));
                break;
            case R.id.call:
                UserModel user = users().get(chatId);
                startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:+" + user.getPhone())));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        String text = messageBody.getText().toString().trim();
        if (text.length() > 0) {
            DialogStorage.draftStorage().put(DialogUids.getDialogUid(chatType, chatId), text);
        } else {
            DialogStorage.draftStorage().remove(DialogUids.getDialogUid(chatType, chatId));
        }
        keyboardUtils.setImeVisibility(messageBody, false);
        stateBroker().onConversationClose(chatType, chatId);
    }

    @Override
    public void onUpdated(GroupState groupState) {
        if (groupState == GroupState.JOINED) {
            goneView(kicked, false);
        } else if (groupState == GroupState.KICKED) {
            showView(kicked, false);
        } else {
            finish();
        }
    }
}
