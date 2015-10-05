/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.avatar;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestEditAvatar;
import im.actor.core.api.rpc.RequestRemoveAvatar;
import im.actor.core.api.rpc.ResponseEditAvatar;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateUserAvatarChanged;
import im.actor.core.entity.FileReference;
import im.actor.core.modules.Modules;
import im.actor.core.modules.internal.file.UploadManager;
import im.actor.core.modules.updates.internal.ExecuteAfter;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.modules.utils.RandomUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.AvatarUploadState;

public class OwnAvatarChangeActor extends ModuleActor {

    private long currentChangeTask = 0;

    public OwnAvatarChangeActor(Modules modules) {
        super(modules);
    }

    public void changeAvatar(String descriptor) {
        if (currentChangeTask != 0) {
            context().getFilesModule().cancelUpload(currentChangeTask);
            currentChangeTask = 0;
        }
        currentChangeTask = RandomUtils.nextRid();

        context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(descriptor, true));

        context().getFilesModule().requestUpload(currentChangeTask, descriptor, "avatar.jpg", self());
    }

    public void uploadCompleted(final long rid, FileReference fileReference) {
        if (rid != currentChangeTask) {
            return;
        }
        request(new RequestEditAvatar(new ApiFileLocation(fileReference.getFileId(),
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
                context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }
        });
    }

    public void avatarChanged(long rid) {
        if (rid != currentChangeTask) {
            return;
        }
        currentChangeTask = 0;
        context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
    }

    public void uploadError(long rid) {
        if (rid != currentChangeTask) {
            return;
        }
        currentChangeTask = 0;
        context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
    }

    public void removeAvatar() {
        if (currentChangeTask != 0) {
            context().getFilesModule().cancelUpload(currentChangeTask);
            currentChangeTask = 0;
        }
        currentChangeTask = RandomUtils.nextRid();

        context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, true));
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
                context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
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
