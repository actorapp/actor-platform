package im.actor.model.viewmodel;

import im.actor.model.entity.FileLocation;
import im.actor.model.files.FileReference;
import im.actor.model.modules.Modules;
import im.actor.model.modules.file.FileCallback;
import im.actor.model.mvvm.AsyncVM;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class FileVM extends AsyncVM {
    private Modules modules;
    private FileLocation location;
    private FileCallback callback;
    private FileVMCallback vmCallback;

    public FileVM(FileLocation location, boolean isAutostart, Modules modules,
                  FileVMCallback vmCallback) {
        this.modules = modules;
        this.location = location;
        this.vmCallback = vmCallback;
        this.callback = new FileCallback() {
            @Override
            public void onNotDownloaded() {
                post(new OnNotDownloaded());
            }

            @Override
            public void onDownloading(float progress) {
                post(new OnDownloading(progress));
            }

            @Override
            public void onDownloaded(FileReference reference) {
                post(new OnDownloaded(reference));
            }
        };
        modules.getFilesModule().bindFile(location, isAutostart, callback);
    }

    @Override
    protected void onObjectReceived(Object obj) {
        if (obj instanceof OnNotDownloaded) {
            vmCallback.onNotDownloaded();
        } else if (obj instanceof OnDownloading) {
            vmCallback.onDownloading(((OnDownloading) obj).getProgress());
        } else if (obj instanceof OnDownloaded) {
            vmCallback.onDownloaded(((OnDownloaded) obj).fileReference);
        }
    }

    @Override
    public void detach() {
        super.detach();
        modules.getFilesModule().unbindFile(location.getFileId(), callback, false);
    }

    private class OnNotDownloaded {

    }

    private class OnDownloading {
        private float progress;

        private OnDownloading(float progress) {
            this.progress = progress;
        }

        public float getProgress() {
            return progress;
        }
    }

    private class OnDownloaded {
        private FileReference fileReference;

        private OnDownloaded(FileReference fileReference) {
            this.fileReference = fileReference;
        }

        public FileReference getFileReference() {
            return fileReference;
        }
    }
}