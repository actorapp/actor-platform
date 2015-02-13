package im.actor.messenger.core.actors.files;

import com.droidkit.actors.concurrency.Future;

import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;

/**
 * Created by ex3ndr on 06.09.14.
 */
public interface DownloadManagerInt {
    public void request(int type, int id, long rid, AbsFileMessage fileMessage, boolean isAutomatic);

    public void pause(AbsFileMessage fileMessage);

    public void writeToStorage(String fileName, String name, FileLocation fileLocation);

    public Future<String> downloadedFileName(FileLocation fileLocation);
}
