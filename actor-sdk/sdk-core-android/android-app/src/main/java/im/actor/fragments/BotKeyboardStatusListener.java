package im.actor.fragments;

import im.actor.sdk.view.emoji.keyboard.KeyboardStatusListener;

public interface BotKeyboardStatusListener extends KeyboardStatusListener {
    void onBotKeyboardStatusChanged(boolean botKeyboardModeOn, boolean keyboardAvailable);
}
