package im.actor.model.modules.profile;

import im.actor.model.api.rpc.RequestEditAvatar;
import im.actor.model.api.rpc.ResponseEditAvatar;
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
    public OwnAvatarChangeActor(Modules modules) {
        super(modules);
    }

    public void changeAvatar(String descriptor) {
        modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(descriptor, true));

        modules().getFilesModule().requestUpload(RandomUtils.nextRid(), descriptor, self());
    }

    public void uploadCompleted(long rid, FileLocation fileLocation) {
        request(new RequestEditAvatar(new im.actor.model.api.FileLocation(fileLocation.getFileId(),
                fileLocation.getAccessHash())), new RpcCallback<ResponseEditAvatar>() {
            @Override
            public void onResult(ResponseEditAvatar response) {
                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }

            @Override
            public void onError(RpcException e) {
                modules().getProfile().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, false));
            }
        });
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof ChangeAvatar) {
            ChangeAvatar changeAvatar = (ChangeAvatar) message;
            changeAvatar(changeAvatar.getDescriptor());
        } else if (message instanceof UploadManager.UploadCompleted) {
            UploadManager.UploadCompleted uploadCompleted = (UploadManager.UploadCompleted) message;
            uploadCompleted(uploadCompleted.getRid(), uploadCompleted.getFileLocation());
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
}
