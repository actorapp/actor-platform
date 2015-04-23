package im.actor.model.http;

/**
 * Created by ex3ndr on 23.04.15.
 */
public interface FileUploadCallback {
    public void onUploaded();

    public void onUploadFailure();
}
