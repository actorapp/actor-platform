package im.actor.messenger.app.util.images.common;

import java.io.IOException;

/**
 * Exception while image loading
 */
public class ImageLoadException extends IOException {
    public ImageLoadException() {
    }

    public ImageLoadException(String detailMessage) {
        super(detailMessage);
    }

    public ImageLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageLoadException(Throwable cause) {
        super(cause);
    }
}
