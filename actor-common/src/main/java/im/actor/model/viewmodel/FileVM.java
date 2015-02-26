package im.actor.model.viewmodel;

import im.actor.model.Messenger;
import im.actor.model.entity.FileLocation;

/**
 * Created by ex3ndr on 26.02.15.
 */
public abstract class FileVM extends AsyncVM implements FileCallback {
    private Messenger messenger;
    private FileLocation location;

    public FileVM(FileLocation location, boolean isAutostart, Messenger messenger) {
        this.messenger = messenger;
        this.location = location;
        messenger.bindFile(location, isAutostart, this);
    }

    @Override
    public void detach() {
        super.detach();
        messenger.unbindFile(location.getFileId(), this, false);
    }
}