package im.actor.model.viewmodel;

import im.actor.model.files.FileSystemReference;

/**
 * Created by ex3ndr on 27.02.15.
 */
public interface FileVMCallback {
    public void onNotDownloaded();

    public void onDownloading(float progress);

    public void onDownloaded(FileSystemReference reference);
}
