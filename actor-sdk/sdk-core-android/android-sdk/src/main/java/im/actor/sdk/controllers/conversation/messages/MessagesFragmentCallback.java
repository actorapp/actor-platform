package im.actor.sdk.controllers.conversation.messages;

public interface MessagesFragmentCallback {

    void onAvatarClick(int uid);

    void onAvatarLongClick(int uid);

    void onMessageEdit(long rid, String text);

    void onMessageQuote(String text);
}
