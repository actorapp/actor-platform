package im.actor.sdk.controllers.conversation.mentions;

public interface AutocompleteCallback {
    
    void onMentionPicked(String name);

    void onCommandPicked(String command);
}
