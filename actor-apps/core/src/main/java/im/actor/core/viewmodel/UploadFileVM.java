/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import im.actor.core.modules.Modules;
import im.actor.runtime.mvvm.AsyncVM;

/**
 * Value Model handler for Uploading File.
 * <p></p>
 * Create by calling method in Messenger object and ALWAYS release by calling detach method.
 */
public class UploadFileVM extends AsyncVM {
    private long rid;
    private Modules modules;
    private UploadFileVMCallback vmCallback;
    private UploadFileCallback callback;

    /**
     * <p>INTERNAL API</p>
     * Create UploadFileVM
     *
     * @param rid        file random id
     * @param vmCallback file value model callback
     * @param modules    im.actor.android.modules reference
     */
    public UploadFileVM(long rid, UploadFileVMCallback vmCallback, Modules modules) {
        this.rid = rid;
        this.modules = modules;
        this.vmCallback = vmCallback;
        this.callback = new UploadFileCallback() {
            @Override
            public void onNotUploading() {
                post(new NotUploading());
            }

            @Override
            public void onUploading(float progress) {
                post(new Uploading(progress));
            }

            @Override
            public void onUploaded() {
                post(new Uploaded());
            }
        };
        modules.getFilesModule().bindUploadFile(rid, callback);
    }

    @Override
    protected void onObjectReceived(Object obj) {
        if (obj instanceof NotUploading) {
            vmCallback.onNotUploaded();
        } else if (obj instanceof Uploading) {
            vmCallback.onUploading(((Uploading) obj).getProgress());
        } else if (obj instanceof Uploaded) {
            vmCallback.onUploaded();
        }
    }

    /**
     * Detach UploadFileVM from Messenger.
     * Don't use object after detaching.
     */
    @Override
    public void detach() {
        super.detach();
        modules.getFilesModule().unbindUploadFile(rid, callback);
    }

    private class NotUploading {

    }

    private class Uploading {
        private float progress;

        private Uploading(float progress) {
            this.progress = progress;
        }

        public float getProgress() {
            return progress;
        }
    }

    private class Uploaded {

    }
}
