package im.actor.model.http;

/**
 * Created by ex3ndr on 23.04.15.
 */
public interface FileDownloadCallback {
    void onDownloaded(byte[] data);

    void onDownloadFailure();
}
