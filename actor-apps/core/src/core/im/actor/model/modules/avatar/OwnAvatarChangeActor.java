/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.avatar;

import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestEditAvatar;
import im.actor.model.api.rpc.RequestRemoveAvatar;
import im.actor.model.api.rpc.ResponseEditAvatar;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateUserAvatarChanged;
import im.actor.model.entity.FileReference;
import im.actor.model.modules.Modules;
import im.actor.model.modules.file.UploadManager;
import im.actor.model.modules.updates.internal.ExecuteAfter;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.viewmodel.AvatarUploadState;

public class OwnAvatarChangeActor extends ModuleActor {

    private long currentChangeTask = 0;

    public OwnAvatarChangeActor(Modules modules) {
        super(modules);
    }

    public void changeAvatar(String descriptor) {
        if (currentChangeTask != 0) {
            modules().getFilesModule().cancelUpload(currentChangeTask);
            currentChangeTask = 0;
        }
        currentChangeTask = RandomUtils.nextRid();

        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(descriptor, true));

        modules().getFilesModule().requestUpload(currentChangeTask, descriptor, "avatar.jpg", self());
    }

    public void uploadCompleted(final long rid, FileReference fileReference) {
        if (rid != currentChangeTask) {
            return;
        }
        request(new RequestEditAvatar(new im.actor.model.api.FileLocation(fileReference.getFileId(),
                fileReference.getAccessHash())), new RpcCallback<ResponseEditAvatar>() {
            @Override
            public void onResult(ResponseEditAvatar response) {

                // Put update to sequence
                updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                        response.getState(), UpdateUserAvatarChanged.HEADER,
                        new UpdateUserAvatarChanged(myUid(), response.getAvatar()).toByteArray()));

                // After update applied turn of uploading state
                updates().onUpdateReceived(new ExecuteAfter(response.getSeq(), new Runnable() {
                    @Override
                    public void run() {
                        self().send(new AvatarChanged(rid));
                    }
                }));
            }

            @Override
            public void onError(RpcException e) {
                if (rid != currentChangeTask) {
                    return;
                }
                currentChangeTask = 0;
                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }
        });
    }

    public void avatarChanged(long rid) {
        if (rid != currentChangeTask) {
            return;
        }
        currentChangeTask = 0;
        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
    }

    public void uploadError(long rid) {
        if (rid != currentChangeTask) {
            return;
        }
        currentChangeTask = 0;
        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
    }

    public void removeAvatar() {
        if (currentChangeTask != 0) {
            modules().getFilesModule().cancelUpload(currentChangeTask);
            currentChangeTask = 0;
        }
        currentChangeTask = RandomUtils.nextRid();

        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, true));
        final long currentRid = currentChangeTask;
        request(new RequestRemoveAvatar(), new RpcCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq response) {
                updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                        response.getState(), UpdateUserAvatarChanged.HEADER,
                        new UpdateUserAvatarChanged(myUid(), null).toByteArray()));

                // After update applied turn of uploading state
                updates().onUpdateReceived(new ExecuteAfter(response.getSeq(), new Runnable() {
                    @Override
                    public void run() {
                        self().send(new AvatarChanged(currentRid));
                    }
                }));
            }

            @Override
            public void onError(RpcException e) {
                if (currentRid != currentChangeTask) {
                    return;
                }
                currentChangeTask = 0;
                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }
        });
    }

    //region Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof ChangeAvatar) {
            ChangeAvatar changeAvatar = (ChangeAvatar) message;
            changeAvatar(changeAvatar.getDescriptor());
        } else if (message instanceof UploadManager.UploadCompleted) {
            UploadManager.UploadCompleted uploadCompleted = (UploadManager.UploadCompleted) message;
            uploadCompleted(uploadCompleted.getRid(), uploadCompleted.getFileReference());
        } else if (message instanceof UploadManager.UploadError) {
            UploadManager.UploadError uploadError = (UploadManager.UploadError) message;
            uploadError(uploadError.getRid());
        } else if (message instanceof RemoveAvatar) {
            removeAvatar();
        } else if (message instanceof AvatarChanged) {
            avatarChanged(((AvatarChanged) message).getRid());
        } else {
            drop(message);
        }
    }

    public static class ChangeAvatar {
        private String descriptor;

        public ChangeAvatar(String descriptor) {
            this.descriptor = descriptor;
        }

        public String getDescriptor() {
            return descriptor;
        }
    }

    public static class RemoveAvatar {

    }

    public static class AvatarChanged {
        private long rid;

        public AvatarChanged(long rid) {
            this.rid = rid;
        }

        public long getRid() {
            return rid;
        }
    }

    //endregion
}
