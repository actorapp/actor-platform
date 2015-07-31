package im.actor.messenger.app.util.images.common;

import java.io.IOException;

/**
 * Exception for image saving
 */
public class ImageSaveException extends IOException {
    public ImageSaveException() {
    }

    public ImageSaveException(String detailMessage) {
        super(detailMessage);
    }

    public ImageSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageSaveException(Throwable cause) {
        super(cause);
    }
}
