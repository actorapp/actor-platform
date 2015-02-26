package im.actor.model.viewmodel;

import im.actor.model.files.FileReference;

/**
 * Created by ex3ndr on 27.02.15.
 */
public interface FileVMCallback {
    public void onNotDownloaded();

    public void onDownloading(float progress);

    public void onDownloaded(FileReference reference);
}
