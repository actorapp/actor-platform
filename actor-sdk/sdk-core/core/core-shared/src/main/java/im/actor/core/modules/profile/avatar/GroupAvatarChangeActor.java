/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.profile.avatar;

import java.util.HashMap;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestEditGroupAvatar;
import im.actor.core.api.rpc.RequestRemoveGroupAvatar;
import im.actor.core.api.rpc.ResponseEditGroupAvatar;
import im.actor.core.api.rpc.ResponseSeqDate;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.Group;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.file.UploadManager;
import im.actor.core.modules.sequence.internal.ExecuteAfter;
import im.actor.core.modules.ModuleActor;
import im.actor.core.util.RandomUtils;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.AvatarUploadState;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.function.Consumer;

public class GroupAvatarChangeActor extends ModuleActor {

    // j2objc bug workaround
    private static final Void DUMB = null;

    private HashMap<Integer, Long> currentTasks = new HashMap<>();
    private HashMap<Long, Integer> tasksMap = new HashMap<>();

    public GroupAvatarChangeActor(ModuleContext context) {
        super(context);
    }

    public void changeAvatar(int gid, String descriptor) {
        if (currentTasks.containsKey(gid)) {
            context().getFilesModule().cancelUpload(currentTasks.get(gid));
            currentTasks.remove(gid);
        }
        long rid = RandomUtils.nextRid();
        currentTasks.put(gid, rid);
        tasksMap.put(rid, gid);

        context().getGroupsModule().getAvatarVM(gid).getUploadState().change(new AvatarUploadState(descriptor, true));
        context().getFilesModule().requestUpload(rid, descriptor, "avatar.jpg", self());
    }

    public void uploadCompleted(final long rid, FileReference fileReference) {
        if (!tasksMap.containsKey(rid)) {
            return;
        }

        final int gid = tasksMap.get(rid);
        long accessHash = getGroup(gid).getAccessHash();

        if (currentTasks.get(gid) != rid) {
            return;
        }

        api(new RequestEditGroupAvatar(new ApiGroupOutPeer(gid, accessHash), rid, new ApiFileLocation(fileReference.getFileId(),
                fileReference.getAccessHash())))
                .flatMap(responseEditGroupAvatar ->
                        updates().applyUpdate(
                                responseEditGroupAvatar.getSeq(),
                                responseEditGroupAvatar.getState(),
                                new UpdateGroupAvatarChanged(
                                        gid, rid, myUid(),
                                        responseEditGroupAvatar.getAvatar(),
                                        responseEditGroupAvatar.getDate())
                        ))
                .then(v -> avatarChanged(gid, rid))
                .failure(e -> {
                    if (!tasksMap.containsKey(rid)) {
                        return;
                    }
                    final int gid2 = tasksMap.get(rid);
                    if (currentTasks.get(gid2) != rid) {
                        return;
                    }
                    currentTasks.remove(gid2);
                    tasksMap.remove(rid);

                    context().getGroupsModule().getAvatarVM(gid2).getUploadState().change(new AvatarUploadState(null, false));
                });
    }

    public void avatarChanged(int gid, long rid) {
        if (!currentTasks.containsKey(gid)) {
            return;
        }
        if (currentTasks.get(gid) != rid) {
            return;
        }
        currentTasks.remove(gid);
        context().getGroupsModule().getAvatarVM(gid).getUploadState().change(new AvatarUploadState(null, false));
    }

    public void uploadError(long rid) {
        if (!tasksMap.containsKey(rid)) {
            return;
        }
        final int gid = tasksMap.get(rid);
        if (currentTasks.get(gid) != rid) {
            return;
        }
        currentTasks.remove(gid);
        tasksMap.remove(rid);

        context().getGroupsModule().getAvatarVM(gid).getUploadState().change(new AvatarUploadState(null, false));
    }

    public void removeAvatar(final int gid) {
        if (currentTasks.containsKey(gid)) {
            context().getFilesModule().cancelUpload(currentTasks.get(gid));
            currentTasks.remove(gid);
        }
        final long rid = RandomUtils.nextRid();
        currentTasks.put(gid, rid);
        tasksMap.put(rid, gid);

        Group group = context().getGroupsModule().getGroups().getValue(gid);
        ApiGroupOutPeer outPeer = new ApiGroupOutPeer(gid, group.getAccessHash());

        context().getProfileModule().getOwnAvatarVM().getUploadState().change(new AvatarUploadState(null, true));
        api(new RequestRemoveGroupAvatar(outPeer, rid))
                .flatMap(responseSeqDate ->
                        updates().applyUpdate(
                                responseSeqDate.getSeq(),
                                responseSeqDate.getState(),
                                new UpdateGroupAvatarChanged(
                                        gid,
                                        rid,
                                        myUid(),
                                        null,
                                        responseSeqDate.getDate())))
                .then(aVoid -> avatarChanged(gid, rid))
                .failure(e -> {
                    if (!tasksMap.containsKey(rid)) {
                        return;
                    }
                    final int gid2 = tasksMap.get(rid);
                    if (currentTasks.get(gid) != rid) {
                        return;
                    }
                    currentTasks.remove(gid2);
                    tasksMap.remove(rid);

                    context().getGroupsModule().getAvatarVM(gid2).getUploadState().change(new AvatarUploadState(null, false));
                });
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ChangeAvatar) {
            ChangeAvatar changeAvatar = (ChangeAvatar) message;
            changeAvatar(changeAvatar.getGid(), changeAvatar.getDescriptor());
        } else if (message instanceof UploadManager.UploadCompleted) {
            UploadManager.UploadCompleted uploadCompleted = (UploadManager.UploadCompleted) message;
            uploadCompleted(uploadCompleted.getRid(), uploadCompleted.getFileReference());
        } else if (message instanceof UploadManager.UploadError) {
            UploadManager.UploadError uploadError = (UploadManager.UploadError) message;
            uploadError(uploadError.getRid());
        } else if (message instanceof RemoveAvatar) {
            RemoveAvatar removeAvatar = (RemoveAvatar) message;
            removeAvatar(removeAvatar.getGid());
        } else {
            super.onReceive(message);
        }
    }

    public static class ChangeAvatar {
        private int gid;
        private String descriptor;

        public ChangeAvatar(int gid, String descriptor) {
            this.gid = gid;
            this.descriptor = descriptor;
        }

        public int getGid() {
            return gid;
        }

        public String getDescriptor() {
            return descriptor;
        }
    }

    public static class RemoveAvatar {
        private int gid;

        public RemoveAvatar(int gid) {
            this.gid = gid;
        }

        public int getGid() {
            return gid;
        }
    }
}
