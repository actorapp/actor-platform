package im.actor.model.viewmodel;

import im.actor.model.files.FileSystemReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface DownloadCallback {
    public void onNotDownloaded();

    public void onDownloading(float progress);

    public void onDownloaded(FileSystemReference reference);
}