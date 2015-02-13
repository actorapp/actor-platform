package im.actor.messenger.core.actors.send;

/**
 * Created by ex3ndr on 26.11.14.
 */
public interface MessageDeliveryInt {
    public void sendText(int chatType, int chatId, String text);

    public void sendOpus(int type, int id, String fileName, int duration);

    public void sendDocument(int type, int id, String fileName, String name);

    public void sendPhoto(int type, int id, String fileName);

    public void sendVideo(int type, int id, String fileName);

    public void mediaTryAgain(int type, int id, long rid);

    public void mediaPause(int type, int id, long rid);

    public void mediaCancel(int type, int id, long rid);

    public void mediaCancelAll(int type, int id);
}
