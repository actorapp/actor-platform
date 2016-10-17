package im.actor.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.JsonContent;
import im.actor.develop.R;
import im.actor.runtime.json.JSONArray;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

public class BotKeyboard extends EmojiKeyboard implements MessagesFragment.NewMessageListener {

    private FrameLayout container;
    private FrameLayout buttonsScrollContainer;
    private ScrollView buttonsScroll;
    private LinearLayout buttonsContainer;
    private View emoji;
    private BotButtonListener botButtonListener;
    private BotKeyboardButton[][] keyboard;
    private boolean buttonsRequested;
    private boolean buttonsAvailable = false;

    public BotKeyboard(Activity activity, EditText messageBody) {
        super(activity, messageBody);
    }

    public BotKeyboard(Activity activity, EditText messageBody, @NotNull BotButtonListener botButtonListener) {
        super(activity, messageBody);
        this.botButtonListener = botButtonListener;
    }

    @Override
    protected View createView() {
        emoji = super.createView();
        container = new FrameLayout(getActivity());
        container.addView(emoji, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        buttonsScrollContainer = new FrameLayout(getActivity());
        buttonsScrollContainer.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        container.addView(buttonsScrollContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        buttonsContainer = new LinearLayout(getActivity());
        buttonsContainer.setOrientation(LinearLayout.VERTICAL);

        buttonsScroll = new ScrollView(getActivity());

        if (keyboard != null) {
            bindBotButtons(keyboard);
        }

        return container;
    }

    public void bindBotButtons(BotKeyboardButton[][] keyboardButtons) {
        buttonsScrollContainer.removeAllViews();
        buttonsScroll.removeAllViews();
        buttonsContainer.removeAllViews();
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        LinearLayout.LayoutParams rowsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        for (BotKeyboardButton[] row : keyboardButtons) {
            LinearLayout rowContainer = new LinearLayout(getActivity());
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            for (BotKeyboardButton button : row) {
                ViewParent parent = button.getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeAllViews();
                }
                rowContainer.addView(button, buttonParams);
            }
            buttonsContainer.addView(rowContainer, rowsParams);
        }

        if (keyboardButtons.length > 5) {
            buttonsScroll.addView(buttonsContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            buttonsScrollContainer.addView(buttonsScroll, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        } else {
            buttonsScrollContainer.addView(buttonsContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }

    }

    public void toggle(boolean showBotButtons) {
        container.bringChildToFront(showBotButtons ? buttonsScrollContainer : emoji);
        super.toggle();
    }

    @Override
    public void show() {
        show(false);
    }

    public void show(boolean showBotButtons) {
        switchMode(showBotButtons);
        super.show();
    }

    protected void switchMode(boolean showBotButtons) {
        buttonsRequested = showBotButtons;
        if (buttonsContainer != null) {
            container.bringChildToFront(buttonsRequested ? buttonsScrollContainer : emoji);
        }
        if (keyboardStatusListener != null && keyboardStatusListener instanceof BotKeyboardStatusListener) {
            ((BotKeyboardStatusListener) keyboardStatusListener).onBotKeyboardStatusChanged(showBotButtons, buttonsAvailable);
        }
    }

    @Override
    protected void showInternal() {
        super.showInternal();
        container.bringChildToFront(buttonsRequested ? buttonsScrollContainer : emoji);
    }

    @Override
    public void onNewMessage(Message m) {
        if (m.getSenderId() == ActorSDK.sharedActor().getMessenger().myUid()) {
            return;
        }
        AbsContent content = m.getContent();
        if (content instanceof JsonContent) {
            try {
                String rawJson = ((JsonContent) content).getRawJson();

                JSONObject json = new JSONObject(rawJson);
                if (json.getString("dataType").equals("textWithKeyboard")) {
                    JSONObject data = json.getJSONObject("data");
                    //Check if keyboard must shown in message
                    if (!data.optBoolean("isInMessage", false)) {
                        // Unpack keyboard from json
                        JSONArray keyboardRows = data.getJSONArray("keyboardRows");
                        JSONArray rowJson;
                        JSONObject buttonJson;
                        keyboard = new BotKeyboardButton[keyboardRows.length()][];
                        for (int i = 0; i < keyboardRows.length(); i++) {
                            rowJson = keyboardRows.getJSONArray(i);
                            BotKeyboardButton[] row = new BotKeyboardButton[rowJson.length()];
                            for (int j = 0; j < rowJson.length(); j++) {
                                buttonJson = rowJson.getJSONObject(j);
                                row[j] = new BotKeyboardButton(getActivity(),
                                        buttonJson.getString("content"),
                                        buttonJson.getString("title"),
                                        buttonJson.optBoolean("isDraft"),
                                        buttonJson.optBoolean("isSend", true),
                                        buttonJson.optInt("color", 0xffffffff),
                                        botButtonListener
                                );
                            }
                            keyboard[i] = row;
                        }
                        buttonsAvailable = true;
                        if (!isShowing()) {
                            show(true);
                        } else {
                            bindBotButtons(keyboard);
                        }

                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //if not returned before, hide old bot keyboard
        buttonsAvailable = false;
        switchMode(false);
    }

    public static class BotKeyboardButton extends TextView {

        public static final int verticalPadding = Screen.dp(60);

        public BotKeyboardButton(Context context, String content, String title, boolean isDraft, boolean isSend, int color, BotButtonListener listener) {
            super(context);
            setPadding(0, verticalPadding / 2, 0, verticalPadding / 2);
            setText(title);
            setGravity(Gravity.CENTER);
            setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            setTextSize(15);
            setBackgroundResource(R.drawable.conv_bubble_media_in);
            getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);

            setOnClickListener(view -> listener.onBotButtonClick(content, isDraft, isSend));
        }
    }

    public interface BotButtonListener {
        void onBotButtonClick(String content, boolean isDraft, boolean isSend);
    }
}
