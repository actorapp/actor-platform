package im.actor.sdk.controllers.conversation.messages;

public interface MessagesFragmentCallback {
    void onMessageEdit(long rid, String text);

    void onMessageQuote(String text);
}
