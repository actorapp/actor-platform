package im.actor.messenger.core.actors.send;

/**
 * Created by ex3ndr on 01.10.14.
 */
public interface MediaSenderInt {

    public void sendOpus(int type, int id, String fileName, int duration, boolean isEncrypted);

    public void sendDocument(int type, int id, String fileName, String name, boolean isEncrypted);

    public void sendPhoto(int type, int id, String fileName, boolean isEncrypted);

    public void sendVideo(int type, int id, String fileName, boolean isEncrypted);

    public void tryAgain(int type, int id, long rid);

    public void pause(int type, int id, long rid);

    public void cancel(int type, int id, long rid);

    public void cancelAll(int type, int id);
}
