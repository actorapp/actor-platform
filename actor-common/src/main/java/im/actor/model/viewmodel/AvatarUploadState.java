package im.actor.model.viewmodel;

/**
 * Created by ex3ndr on 04.03.15.
 */
public class AvatarUploadState {
    private String descriptor;
    private boolean isUploading;

    public AvatarUploadState(String descriptor, boolean isUploading) {
        this.descriptor = descriptor;
        this.isUploading = isUploading;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isUploading() {
        return isUploading;
    }
}
