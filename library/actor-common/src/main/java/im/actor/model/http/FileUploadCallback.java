/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.http;

public interface FileUploadCallback {
    void onUploaded();

    void onUploadFailure();
}
