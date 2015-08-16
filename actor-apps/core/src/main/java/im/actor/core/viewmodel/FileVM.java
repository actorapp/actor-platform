/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import im.actor.core.entity.FileReference;
import im.actor.core.modules.Modules;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.mvvm.AsyncVM;

/**
 * Value Model handler for File.
 * <p></p>
 * Create by calling method in Messenger object and ALWAYS release by calling detach method.
 */
public class FileVM extends AsyncVM {
    private Modules modules;
    private FileReference location;
    private FileCallback callback;
    private FileVMCallback vmCallback;

    /**
     * <p>INTERNAL API</p>
     * Create FileVM
     *
     * @param location    file reference
     * @param isAutostart is perform autostart
     * @param modules     im.actor.android.modules reference
     * @param vmCallback  View Model callback
     */
    public FileVM(FileReference location, boolean isAutostart, Modules modules,
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

    /**
     * Detach FileVM from Messenger.
     * Don't use object after detaching.
     */
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