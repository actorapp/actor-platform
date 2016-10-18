package im.actor.sdk.controllers.conversation.inputbar;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesDefaultFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.sdk.core.audio.VoiceCaptureActor;
import im.actor.sdk.util.KeyboardHelper;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.keyboard.KeyboardStatusListener;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;
import im.actor.sdk.view.markdown.AndroidMarkdown;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ViewUtils.zoomInView;
import static im.actor.sdk.util.ViewUtils.zoomOutView;
import static im.actor.sdk.view.emoji.SmileProcessor.emoji;

public class InputBarFragment extends BaseFragment implements MessagesDefaultFragment.NewMessageListener {

    private static final int SLIDE_LIMIT = Screen.dp(180);
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;


    //
    // Configuration
    //

    private boolean isAudioEnabled = true;
    private boolean isAttachEnabled = true;
    private boolean isDisableOnEmptyText = true;

    //
    // Texting
    //
    private boolean isTypingDisabled = false;
    protected BarEditText messageEditText;
    protected TintImageView sendButton;
    protected ImageButton attachButton;
    protected String lastWord = "";

    //
    // Voice
    //
    private View audioContainer;
    private View recordPoint;
    private View audioSlide;
    private int slideStart;
    private TextView audioTimer;
    private boolean isAudioVisible;
    private ActorRef voiceRecordActor;
    private String audioFile;
    private ImageView audioButton;

    // Helper for hide/show keyboard
    protected KeyboardHelper keyboardUtils;

    // Emoji keyboard
    protected EmojiKeyboard emojiKeyboard;
    protected ImageView emojiButton;
    private Message lastMessage;

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        keyboardUtils = new KeyboardHelper(getActivity());

        if (saveInstance != null) {
            isAudioEnabled = saveInstance.getBoolean("isAudioEnabled");
            isDisableOnEmptyText = saveInstance.getBoolean("isDisableOnEmptyText");
            isAudioVisible = saveInstance.getBoolean("isAudioVisible");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View res = inflater.inflate(R.layout.fragment_inputbar, container, false);
        res.setBackgroundColor(style.getMainBackgroundColor());

        //
        // Message Body
        //
        messageEditText = (BarEditText) res.findViewById(R.id.et_message);
        messageEditText.setTextColor(style.getTextPrimaryColor());
        messageEditText.setHintTextColor(style.getTextHintColor());
        // Hardware keyboard events
        messageEditText.setOnKeyListener((view, keycode, keyEvent) -> {
            if (messenger().isSendByEnterEnabled()) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) {
                    onSendButtonPressed();
                    return true;
                }
            }
            return false;
        });
        // Software keyboard events
        messageEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEND) {
                onSendButtonPressed();
                return true;
            }
            if (i == EditorInfo.IME_ACTION_DONE) {
                onSendButtonPressed();
                return true;
            }
            if (messenger().isSendByEnterEnabled()) {
                if (keyEvent != null && i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    onSendButtonPressed();
                    return true;
                }
            }
            return false;
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onBeforeTextChanged(charSequence, i, i1, i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                InputBarFragment.this.onTextChanged(charSequence, i, i1, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                onAfterTextChanged(editable);
            }
        });
        messageEditText.addSelectionListener((start, length) -> {
            InputBarFragment.this.onSelectionChanged(start, length);
        });
        messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            Fragment parent = getParentFragment();
            if (parent instanceof InputBarCallback) {
                ((InputBarCallback) parent).onTextFocusChanged(hasFocus);
            }
        });


        //
        // Send Button
        //
        sendButton = (TintImageView) res.findViewById(R.id.ib_send);
        sendButton.setResource(R.drawable.conv_send);
        sendButton.setOnClickListener(v -> {
            onSendButtonPressed();
        });


        //
        // Attach Button
        //
        attachButton = (ImageButton) res.findViewById(R.id.ib_attach);
        attachButton.setOnClickListener(v -> {
            onAttachButtonClicked();
        });


        //
        // Emoji keyboard
        //
        emojiButton = (ImageView) res.findViewById(R.id.ib_emoji);
        emojiButton.setOnClickListener(v -> emojiKeyboard.toggle());
        emojiKeyboard = getEmojiKeyboard();
        emojiKeyboard.setOnStickerClickListener(sticker -> {
            Fragment parent = getParentFragment();
            if (parent instanceof InputBarCallback) {
                ((InputBarCallback) parent).onStickerSent(sticker);
            }
        });
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


        //
        // Audio
        //
        audioContainer = res.findViewById(R.id.audioContainer);
        audioTimer = (TextView) res.findViewById(R.id.audioTimer);
        audioSlide = res.findViewById(R.id.audioSlide);
        recordPoint = res.findViewById(R.id.record_point);

        audioButton = (ImageView) res.findViewById(R.id.record_btn);
        audioButton.setVisibility(View.VISIBLE);
        audioButton.setOnTouchListener((v, event) -> {
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
        });

        return res;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (lastMessage != null) {
            onNewMessage(lastMessage);
            lastMessage = null;
        }

    }

    @NonNull
    protected EmojiKeyboard getEmojiKeyboard() {
        return new EmojiKeyboard(getActivity(), messageEditText);
    }

    public void requestFocus() {
        messageEditText.requestFocus();
        keyboardUtils.setImeVisibility(messageEditText, true);
    }

    public void clearFocus() {
        messageEditText.clearFocus();
        keyboardUtils.setImeVisibility(messageEditText, false);
    }

    public void setText(String text) {
        setText(text, false);
    }

    public void setText(String text, boolean selectAll) {
        isTypingDisabled = true;
        Spannable spantext = AndroidMarkdown.processOnlyLinks(text);
        spantext = emoji().processEmojiCompatMutable(spantext, SmileProcessor.CONFIGURATION_BUBBLES);
        messageEditText.setText(spantext);
        if (selectAll) {
            messageEditText.setSelection(messageEditText.getText().length());
        }
        isTypingDisabled = false;
    }

    public void replaceCurrentWord(String word) {
        isTypingDisabled = true;

        Editable sequence = messageEditText.getText();

        // Initial Word End - symbol right after current selection
        int wordEnd = messageEditText.getSelectionEnd();
        // Initial Word Start - symbol before end
        int wordStart = messageEditText.getSelectionStart() - 1;

        // Searching for first space or new line
        for (; wordStart >= 0; wordStart--) {
            if (sequence.charAt(wordStart) == ' ' || sequence.charAt(wordStart) == '\n') {
                break;
            }
        }
        wordStart++;

        // Searching for first space or new line at the tail
        for (; wordEnd < sequence.length(); wordEnd++) {
            if (sequence.charAt(wordEnd) == ' ' || sequence.charAt(wordEnd) == '\n') {
                break;
            }
        }

        // Reading word
        if (0 <= wordStart && wordStart < wordEnd && wordEnd <= sequence.length()) {
            boolean wasLast = wordEnd == sequence.length();
            sequence = sequence.replace(wordStart, wordEnd, word);
            if (wasLast) {
                sequence = sequence.append(' ');
            }
            messageEditText.setText(sequence);
            if (wasLast) {
                messageEditText.setSelection(sequence.length());
            } else {
                messageEditText.setSelection(wordStart + word.length() + 1);
            }
        }
        isTypingDisabled = false;
    }

    public String getText() {
        return messageEditText.getText().toString();
    }

    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        isAudioEnabled = audioEnabled;
        hideAudio(true);
        checkSendButton();
    }

    public boolean isAttachEnabled() {
        return isAttachEnabled;
    }

    public void setAttachEnabled(boolean attachEnabled) {
        isAttachEnabled = attachEnabled;
        hideAudio(true);
        checkSendButton();
    }

    public boolean isDisableOnEmptyText() {
        return isDisableOnEmptyText;
    }

    public void setDisableOnEmptyText(boolean disableOnEmptyText) {
        isDisableOnEmptyText = disableOnEmptyText;
        checkSendButton();
    }

    protected void onSendButtonPressed() {

        // Hack for full screen mode
        if (getResources().getDisplayMetrics().heightPixels <=
                getResources().getDisplayMetrics().widthPixels) {
            keyboardUtils.setImeVisibility(messageEditText, false);
            messageEditText.clearFocus();
        }

        Fragment parent = getParentFragment();
        if (parent instanceof InputBarCallback) {
            ((InputBarCallback) parent).onTextSent(messageEditText.getText().toString());
        }
    }

    protected void onAttachButtonClicked() {
        Fragment parent = getParentFragment();
        if (parent instanceof InputBarCallback) {
            ((InputBarCallback) parent).onAttachPressed();
        }
    }

    protected void onBeforeTextChanged(CharSequence s, int start, int count, int after) {
        Fragment parent = getParentFragment();
        if (parent instanceof InputBarCallback) {
            if (after > count && !isTypingDisabled) {
                ((InputBarCallback) parent).onTyping();
            }
        }
    }

    protected void onTextChanged(CharSequence s, int start, int before, int count) {
        Fragment parent = getParentFragment();
        if (parent instanceof InputBarCallback) {
            ((InputBarCallback) parent).onTextChanged(s.toString());
        }
        recalculateCurrentWord();
    }

    protected void onSelectionChanged(int start, int length) {
        recalculateCurrentWord();
    }

    protected void recalculateCurrentWord() {

        CharSequence sequence = messageEditText.getText();

        // Initial Word End - symbol right after current selection
        int wordEnd = messageEditText.getSelectionStart();
        // Initial Word Start - symbol before end
        int wordStart = wordEnd - 1;

        // Searching for first space or new line
        for (; wordStart >= 0; wordStart--) {
            if (sequence.charAt(wordStart) == ' ' || sequence.charAt(wordStart) == '\n') {
                break;
            }
        }
        wordStart++;

        // Reading word
        String foundWord;
        if (wordStart >= 0 && wordStart < wordEnd && wordStart < sequence.length()) {
            foundWord = sequence.subSequence(wordStart, wordEnd).toString();
        } else {
            foundWord = "";
        }

        // Update last word
        if (!lastWord.equals(foundWord)) {
            lastWord = foundWord;
            Log.d("InputBarFragment", "word: " + foundWord);
            Fragment parent = getParentFragment();
            if (parent instanceof InputBarCallback) {
                ((InputBarCallback) parent).onAutoCompleteWordChanged(lastWord);
            }
        }
    }

    protected void onAfterTextChanged(Editable editable) {
        checkSendButton(editable.length() > 0);
    }

    protected void checkSendButton() {
        checkSendButton(messageEditText.getText().length() > 0);
    }

    protected void checkSendButton(boolean hasText) {
        if (hasText || !isDisableOnEmptyText) {
            sendButton.setTint(ActorSDK.sharedActor().style.getConvSendEnabledColor());
            sendButton.setEnabled(true);
            if (isAttachEnabled) {
                if (hasText) {
                    zoomOutView(attachButton);
                } else {
                    zoomInView(attachButton);
                }
            } else {
                zoomOutView(attachButton);
            }
            zoomInView(sendButton);
            zoomOutView(audioButton);
        } else {
            sendButton.setTint(ActorSDK.sharedActor().style.getConvSendDisabledColor());
            sendButton.setEnabled(false);
            if (isAttachEnabled) {
                zoomInView(attachButton);
            } else {
                zoomOutView(attachButton);
            }
            if (isAudioEnabled) {
                zoomInView(audioButton);
                zoomOutView(sendButton);
            } else {
                zoomInView(sendButton);
                zoomOutView(audioButton);
            }
        }
    }

    protected void showAudio() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE}, PERMISSION_REQUEST_RECORD_AUDIO);
                return;
            }
        }

        if (isAudioVisible) {
            return;
        }
        isAudioVisible = true;

        hideView(attachButton);
        hideView(messageEditText);
        hideView(emojiButton);

        audioFile = ActorSDK.sharedActor().getMessenger().getInternalTempFile("voice_msg", "opus");

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

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

    protected void slideAudio(int value) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(audioSlide, "translationX", audioSlide.getX(), -value);
        oa.setDuration(0);
        oa.start();
    }

    protected void hideAudio(boolean cancel) {
        if (!isAudioVisible) {
            return;
        }
        isAudioVisible = false;

        showView(attachButton);
        showView(messageEditText);
        showView(emojiButton);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        voiceRecordActor.send(new VoiceCaptureActor.Stop(cancel));
        TranslateAnimation animation = new TranslateAnimation(0, Screen.getWidth(), 0, 0);
        animation.setDuration(160);
        audioContainer.clearAnimation();
        audioContainer.setAnimation(animation);
        audioContainer.animate();
        audioContainer.setVisibility(View.GONE);
        messageEditText.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        emojiKeyboard.destroy();
        voiceRecordActor.send(PoisonPill.INSTANCE);
    }

    @Override
    public void onResume() {
        super.onResume();
        voiceRecordActor = ActorSystem.system().actorOf(Props.create(() -> new VoiceCaptureActor(getActivity(), new VoiceCaptureActor.VoiceCaptureCallback() {
            @Override
            public void onRecordProgress(final long time) {
                getActivity().runOnUiThread(() -> {
                    audioTimer.setText(messenger().getFormatter().formatDuration((int) (time / 1000)));
                });
            }

            @Override
            public void onRecordCrash() {
                getActivity().runOnUiThread(() -> {
                    hideAudio(true);
                });
            }

            @Override
            public void onRecordStop(long progress) {
                if (progress < 1200) {
                    //Cancel
                } else {
                    Fragment parent = getParentFragment();
                    if (parent instanceof InputBarCallback) {
                        ((InputBarCallback) parent).onAudioSent((int) progress, audioFile);
                    }
                }
            }
        })).changeDispatcher("voice_capture_dispatcher"), "actor/voice_capture");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isAudioVisible", isAudioVisible);
        outState.putBoolean("isAudioEnabled", isAudioEnabled);
        outState.putBoolean("isDisableOnEmptyText", isDisableOnEmptyText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emojiKeyboard.release();
        emojiKeyboard = null;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (emojiKeyboard != null) {
            emojiKeyboard.onConfigurationChange();
        }
    }

    public boolean onBackPressed() {
        return emojiKeyboard.onBackPressed();
    }

    @Override
    public void onNewMessage(Message m) {
        if (emojiKeyboard == null) {
            // Inputbar fragment not yet created, store last message for later use
            lastMessage = m;
        }
        if (emojiKeyboard instanceof MessagesFragment.NewMessageListener) {
            ((MessagesFragment.NewMessageListener) emojiKeyboard).onNewMessage(m);
        }
    }
}