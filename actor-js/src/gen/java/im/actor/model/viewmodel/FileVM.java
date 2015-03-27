package im.actor.model.viewmodel;

import im.actor.model.entity.FileReference;
import im.actor.model.files.FileSystemReference;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.AsyncVM;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class FileVM extends AsyncVM {
    private Modules modules;
    private FileReference location;
    private DownloadCallback callback;
    private FileVMCallback vmCallback;

    public FileVM(FileReference location, boolean isAutostart, Modules modules,
                  FileVMCallback vmCallback) {
        this.modules = modules;
        this.location = location;
        this.vmCallback = vmCallback;
        this.callback = new DownloadCallback() {
            @Override
            public void onNotDownloaded() {
                post(new OnNotDownloaded());
            }

            @Override
            public void onDownloading(float progress) {
                post(new OnDownloading(progress));
            }

            @Override
            public void onDownloaded(FileSystemReference reference) {
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
            vmCallback.onDownloaded(((OnDownloaded) obj).getFileSystemReference());
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
        private FileSystemReference fileSystemReference;

        private OnDownloaded(FileSystemReference fileSystemReference) {
            this.fileSystemReference = fileSystemReference;
        }

        public FileSystemReference getFileSystemReference() {
            return fileSystemReference;
        }
    }
}