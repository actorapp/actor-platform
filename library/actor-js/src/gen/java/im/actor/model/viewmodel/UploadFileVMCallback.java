package im.actor.model.viewmodel;

/**
 * Created by ex3ndr on 03.03.15.
 */
public interface UploadFileVMCallback {
    public void onNotUploaded();

    public void onUploading(float progress);

    public void onUploaded();
}
