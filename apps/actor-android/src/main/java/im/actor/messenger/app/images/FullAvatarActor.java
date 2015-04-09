package im.actor.messenger.app.images;

import im.actor.images.common.ImageLoadException;
import im.actor.images.loading.ImageLoader;
import im.actor.images.loading.actors.base.BasicTaskActor;
import im.actor.images.ops.ImageLoading;

import im.actor.android.AndroidFileSystemReference;
import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.viewmodel.FileCallback;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 29.10.14.
 */
public class FullAvatarActor extends BasicTaskActor<FullAvatarTask> {

    private FileCallback fileCallback = new ActorFileCallback();
    private FileReference location;

    public FullAvatarActor(FullAvatarTask task, ImageLoader loader) {
        super(task, loader);
        this.location = task.getAvatarImage().getFileReference();
    }

    @Override
    public void startTask() {
        messenger().bindRawFile(location, true, fileCallback);
    }

    private void onDownloaded(FileSystemReference reference) {
        if (isCompleted()) {
            return;
        }
        AndroidFileSystemReference fileDescriptor = (AndroidFileSystemReference) reference;
        try {
            completeTask(ImageLoading.loadBitmap(fileDescriptor.getDescriptor()));
        } catch (ImageLoadException e) {
            e.printStackTrace();
            error(e);
        }
    }

    @Override
    public void onTaskObsolete() {
        messenger().unbindRawFile(location.getFileId(), false, fileCallback);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnDownloaded) {
            onDownloaded(((OnDownloaded) message).getReference());
        } else {
            super.onReceive(message);
        }
    }

    private class ActorFileCallback implements FileCallback {

        @Override
        public void onNotDownloaded() {

        }

        @Override
        public void onDownloading(float progress) {

        }

        @Override
        public void onDownloaded(FileSystemReference reference) {
            self().send(new OnDownloaded(reference));
        }
    }

    private class OnDownloaded {
        private FileSystemReference reference;

        private OnDownloaded(FileSystemReference reference) {
            this.reference = reference;
        }

        public FileSystemReference getReference() {
            return reference;
        }
    }
}
