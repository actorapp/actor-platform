package im.actor.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
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
import im.actor.sdk.controllers.conversation.messages.MessagesFragment;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

public class BotKeyboard extends EmojiKeyboard implements MessagesFragment.NewMessageListener {

    private FrameLayout container;
    private FrameLayout buttonsScrollContainer;
    private ScrollView buttonsScroll;
    private LinearLayout buttonsContainer;
    private View emoji;
    private BotButtonListener botButtonListener;

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
        container.addView(buttonsScrollContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        buttonsContainer = new LinearLayout(getActivity());
        buttonsContainer.setOrientation(LinearLayout.VERTICAL);

        buttonsScroll = new ScrollView(getActivity());

        return container;
    }

    public void bindBotButtons(BotKeyboardButton[][] keyboardButtons) {
        buttonsScrollContainer.removeAllViews();
        buttonsContainer.removeAllViews();
        buttonsScroll.removeAllViews();
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams rowsParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1);
        for (BotKeyboardButton[] row : keyboardButtons) {
            LinearLayout rowContainer = new LinearLayout(getActivity());
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            for (BotKeyboardButton button : row) {
                rowContainer.addView(button, buttonParams);
            }
            buttonsContainer.addView(rowContainer, rowsParams);
        }

        if (keyboardButtons.length > 3) {
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

    public void show(boolean showBotButtons) {
        container.bringChildToFront(showBotButtons ? buttonsScrollContainer : emoji);
        super.show();
    }

    @Override
    public void onNewMessage(Message m) {
        AbsContent content = m.getContent();
        if (content instanceof JsonContent) {
            try {
                JSONObject json = new JSONObject(((JsonContent) content).getRawJson());
                if (json.getString("dataType").equals("botKeyboard")) {
                    JSONArray keyboardRows = json.getJSONObject("data").getJSONArray("keyboardRows");
                    JSONArray row;
                    JSONObject button;
                    for (int i = 0; i < keyboardRows.length(); i++) {
                        row = keyboardRows.getJSONArray(i);

                        for (int j = 0; j < row.length(); i++) {
                            button = row.getJSONObject(j);

                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class BotKeyboardButton extends TextView {

        public BotKeyboardButton(Context context, String content, String title, boolean isDraft, boolean isSend, int color, BotButtonListener listener) {
            super(context);

            setText(title);
            setBackgroundResource(R.drawable.conv_bubble_media_in);
            getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);

            setOnClickListener(view -> {
                listener.onBotButtonClick(content, isDraft, isSend);
            });
        }
    }

    public interface BotButtonListener {
        void onBotButtonClick(String content, boolean isDraft, boolean isSend);
    }
}
