package im.actor.model.viewmodel;

import im.actor.model.files.FileReference;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface FileCallback {
    public void onNotDownloaded();

    public void onDownloading(float progress);

    public void onDownloaded(FileReference reference);
}
