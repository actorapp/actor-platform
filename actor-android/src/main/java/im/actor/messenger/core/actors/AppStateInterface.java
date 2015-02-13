package im.actor.messenger.core.actors;

/**
 * Created by ex3ndr on 06.09.14.
 */
public interface AppStateInterface {

    public void onConversationOpen(int type, int id);

    public void onConversationClose(int type, int id);

    public void onActivityOpen();

    public void onActivityClose();
}
