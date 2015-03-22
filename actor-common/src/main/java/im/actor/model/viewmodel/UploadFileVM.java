package im.actor.model.viewmodel;

import im.actor.model.modules.Modules;
import im.actor.model.mvvm.AsyncVM;

/**
 * Created by ex3ndr on 03.03.15.
 */
public class UploadFileVM extends AsyncVM {
    private long rid;
    private Modules modules;
    private UploadFileVMCallback vmCallback;
    private UploadCallback callback;

    public UploadFileVM(long rid, UploadFileVMCallback vmCallback, Modules modules) {
        this.rid = rid;
        this.modules = modules;
        this.vmCallback = vmCallback;
        this.callback = new UploadCallback() {
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
