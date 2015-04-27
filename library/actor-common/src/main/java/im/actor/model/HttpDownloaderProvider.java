package im.actor.model;

import im.actor.model.http.FileDownloadCallback;
import im.actor.model.http.FileUploadCallback;

/**
 * Created by ex3ndr on 23.04.15.
 */
public interface HttpDownloaderProvider {
    void downloadPart(String url, int startOffset, int size, int totalSize, FileDownloadCallback callback);

    void uploadPart(String url, byte[] contents, FileUploadCallback callback);
}
