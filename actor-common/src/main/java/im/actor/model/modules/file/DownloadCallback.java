package im.actor.model.modules.file;

import im.actor.model.files.FileReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface DownloadCallback {
    public void onNotDownloaded();

    public void onDownloading(float progress);

    public void onDownloaded(FileReference reference);
}