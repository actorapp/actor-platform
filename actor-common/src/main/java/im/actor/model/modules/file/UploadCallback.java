package im.actor.model.modules.file;

/**
 * Created by ex3ndr on 03.03.15.
 */
public interface UploadCallback {
    public void onNotUploading();

    public void onUploading(float progress);

    public void onUploaded();
}
