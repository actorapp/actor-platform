package im.actor.model.modules.profile;

import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestEditAvatar;
import im.actor.model.api.rpc.RequestRemoveAvatar;
import im.actor.model.api.rpc.ResponseEditAvatar;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateUserAvatarChanged;
import im.actor.model.entity.FileLocation;
import im.actor.model.modules.Modules;
import im.actor.model.modules.file.UploadManager;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.viewmodel.AvatarUploadState;

/**
 * Created by ex3ndr on 04.03.15.
 */
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

    public void uploadCompleted(long rid, FileLocation fileLocation) {
        if (rid != currentChangeTask) {
            return;
        }
        request(new RequestEditAvatar(new im.actor.model.api.FileLocation(fileLocation.getFileId(),
                fileLocation.getAccessHash())), new RpcCallback<ResponseEditAvatar>() {
            @Override
            public void onResult(ResponseEditAvatar response) {

                // Put update to sequence
                updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                        response.getState(), UpdateUserAvatarChanged.HEADER,
                        new UpdateUserAvatarChanged(myUid(), response.getAvatar()).toByteArray()));

                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }

            @Override
            public void onError(RpcException e) {
                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }
        });
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
        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, true));
        request(new RequestRemoveAvatar(), new RpcCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq response) {

                updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                        response.getState(), UpdateUserAvatarChanged.HEADER,
                        new UpdateUserAvatarChanged(myUid(), null).toByteArray()));

                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }

            @Override
            public void onError(RpcException e) {
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
            uploadCompleted(uploadCompleted.getRid(), uploadCompleted.getFileLocation());
        } else if (message instanceof UploadManager.UploadError) {
            UploadManager.UploadError uploadError = (UploadManager.UploadError) message;
            uploadError(uploadError.getRid());
        } else if (message instanceof RemoveAvatar) {
            removeAvatar();
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

    //endregion
}
