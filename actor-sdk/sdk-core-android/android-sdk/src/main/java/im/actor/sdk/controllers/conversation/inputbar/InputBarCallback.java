package im.actor.sdk.controllers.conversation.inputbar;

import im.actor.core.entity.Sticker;

public interface InputBarCallback {

    void onTextFocusChanged(boolean isFocused);

    void onTyping();

    void onTextChanged(CharSequence s, int start, int before, int count);

    void onTextSent(String sequence);

    void onAudioSent(int duration, String descriptor);

    void onStickerSent(Sticker sticker);

    void onAttachPressed();
}
