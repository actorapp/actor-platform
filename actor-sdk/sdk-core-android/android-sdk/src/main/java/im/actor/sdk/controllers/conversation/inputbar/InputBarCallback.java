package im.actor.sdk.controllers.conversation.inputbar;

import im.actor.core.entity.Sticker;

public interface InputBarCallback {

    void onTextFocusChanged(boolean isFocused);

    void onTyping();

    void onTextChanged(String text);

    void onAutoCompleteWordChanged(String text);

    void onTextSent(String text);

    void onAudioSent(int duration, String descriptor);

    void onStickerSent(Sticker sticker);

    void onAttachPressed();
}
