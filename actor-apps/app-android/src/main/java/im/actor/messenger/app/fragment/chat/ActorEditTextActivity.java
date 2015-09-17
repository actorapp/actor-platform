package im.actor.messenger.app.fragment.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.BaseActivity;
import im.actor.messenger.app.view.KeyboardHelper;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.app.view.keyboard.KeyboardStatusListener;
import im.actor.messenger.app.view.keyboard.emoji.EmojiKeyboard;

import static im.actor.messenger.app.core.Core.messenger;

public abstract class ActorEditTextActivity extends BaseActivity {

    //////////////////////////////////
    // Input panel
    //////////////////////////////////

    // Message edit text
    protected EditText messageEditText;
    // Send message button
    protected TintImageView sendButton;
    // Attach button
    protected ImageButton attachButton;
    // Removed from group panel
    protected View removedFromGroup;

    // Helper for hide/show keyboard
    protected KeyboardHelper keyboardUtils;

    // Emoji keyboard
    protected EmojiKeyboard emojiKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting transparent BG for keyboard open optimization
        getWindow().setBackgroundDrawable(null);

        // Setting main layout
        setContentView(R.layout.activity_dialog);

        // Setting fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.messagesFragment, onCreateFragment())
                    .commit();
        }

        // Message Body
        messageEditText = (EditText) findViewById(R.id.et_message);
        // messageEditText.addTextChangedListener(new TextWatcherImp());

        // Handling selection changed

//        messageEditText.setOnSelectionListener(new SelectionListenerEditText.OnSelectedListener() {
//            @Override
//            public void onSelected(int selStart, int selEnd) {
//                //TODO: Fix full select
//                Editable text = messageEditText.getText();
//                if (selEnd != selStart && text.charAt(selStart) == '@') {
//                    if (text.charAt(selEnd - 1) == MENTION_BOUNDS_CHR) {
//                        messageEditText.setSelection(selStart + 2, selEnd - 1);
//                    } else if (text.length() >= 3 && text.charAt(selEnd - 2) == MENTION_BOUNDS_CHR) {
//                        messageEditText.setSelection(selStart + 2, selEnd - 2);
//                    }
//                }
//            }
//        });

        // Hardware keyboard events
        messageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                if (messenger().isSendByEnterEnabled()) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) {
                        onSendButtonPressed();
                        return true;
                    }
                }
                return false;
            }
        });

        // Software keyboard events
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
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
            }
        });

        // Send Button
        sendButton = (TintImageView) findViewById(R.id.ib_send);
        sendButton.setResource(R.drawable.conv_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonPressed();
            }
        });

        // Attach Button
        attachButton = (ImageButton) findViewById(R.id.ib_attach);
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachButtonClicked();
            }
        });

        // Kick panel
        removedFromGroup = findViewById(R.id.kickedFromChat);

        // Emoji keyboard
        final ImageView emojiButton = (ImageView) findViewById(R.id.ib_emoji);
        emojiKeyboard = new EmojiKeyboard(this);
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
                emojiKeyboard.toggle(messageEditText);
            }
        });

        // Keyboard helper for show/hide keyboard
        keyboardUtils = new KeyboardHelper(this);
    }

    protected abstract Fragment onCreateFragment();


    protected void onSendButtonPressed() {

    }

    protected void onAttachButtonClicked() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Destroy emoji keyboard
        emojiKeyboard.destroy();
    }
}
