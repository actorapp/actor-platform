package im.actor.messenger.core.actors.groups;

import android.graphics.Bitmap;

import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.images.cache.DiskCache;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageMetadata;
import com.droidkit.images.common.ImageSaveException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageRotating;
import com.droidkit.images.ops.ImageScaling;
import com.droidkit.images.sources.FileSource;
import com.droidkit.mvvm.ValueModel;

import java.io.File;
import java.io.IOException;

import im.actor.api.scheme.GroupOutPeer;
import im.actor.api.scheme.rpc.ResponseEditGroupAvatar;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.rpc.ResponseSeqDate;
import im.actor.api.scheme.updates.UpdateGroupAvatarChanged;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.Core;
import im.actor.messenger.core.actors.ToastActor;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.base.TypedActorHolder;
import im.actor.messenger.core.actors.files.base.UploadActor;
import im.actor.messenger.core.actors.profile.AvatarChangeState;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.core.images.FileKeys;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.util.RandomUtil;
import im.actor.messenger.util.io.IOUtils;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;

/**
 * Created by ex3ndr on 30.11.14.
 */
public class GroupAvatarActor extends TypedActor<GroupAvatarInt> implements GroupAvatarInt {

    private static final TypedActorHolder<GroupAvatarInt> HOLDER = new TypedActorHolder<GroupAvatarInt>(
            GroupAvatarInt.class, GroupAvatarActor.class, "avatar/group");

    public static GroupAvatarInt get() {
        return HOLDER.get();
    }

    public GroupAvatarActor() {
        super(GroupAvatarInt.class);
    }

    @Override
    public void changeAvatar(int gid, String fileName) {
        try {
            ImageMetadata metadata = new FileSource(fileName).getImageMetadata();
            Bitmap preloaded = ImageLoading.loadBitmapOptimized(fileName);
            Bitmap optimized = ImageRotating.fixExif(preloaded, metadata.getExifOrientation());
            optimized = ImageScaling.scaleFill(optimized, 800, 800);
            String externalFile = AppContext.getInternalTempFile("avatar", "jpg");
            ImageLoading.save(optimized, externalFile);
            performUpload(gid, externalFile);
        } catch (ImageLoadException e) {
            e.printStackTrace();
            ToastActor.get().show("Unable to load file");
        } catch (ImageSaveException e) {
            e.printStackTrace();
            ToastActor.get().show("Unable to load file");
        }
    }

    @Override
    public void clearAvatar(final int gid) {
        final ValueModel<GroupAvatarState.StateHolder> state
                = GroupAvatarState.getGroupState(gid);
        state.change(new GroupAvatarState.StateHolder(GroupAvatarState.State.UPLOADING));
        final GroupModel groupModel = groups().get(gid);
        final long rid = RandomUtil.randomId();
        ask(requests().removeGroupAvatar(new GroupOutPeer(gid, groupModel.getAccessHash()), rid),
                new FutureCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate result) {
                        system().actorOf(SequenceActor.sequence())
                                .send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                                        new UpdateGroupAvatarChanged(gid, rid, myUid(), null, result.getDate())));

                        system().actorOf(SequenceActor.sequence()).send(new Runnable() {
                            @Override
                            public void run() {
                                state.change(new GroupAvatarState.StateHolder(GroupAvatarState.State.NONE));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        AvatarChangeState.uploadingState().change(AvatarChangeState.State.ERROR);
                        ToastActor.get().show("Unable to remove avatar");
                    }
                });
    }

    @Override
    public void cancelChangingAvatar(int gid) {
        final ValueModel<GroupAvatarState.StateHolder> state
                = GroupAvatarState.getGroupState(gid);
        state.change(new GroupAvatarState.StateHolder(GroupAvatarState.State.NONE));
    }

    @Override
    public void tryAgain(int gid) {
        final ValueModel<GroupAvatarState.StateHolder> state
                = GroupAvatarState.getGroupState(gid);
        performUpload(gid, state.getValue().getFileName());
    }


    private void performUpload(final int gid, final String fileName) {
        final ValueModel<GroupAvatarState.StateHolder> state
                = GroupAvatarState.getGroupState(gid);
        final GroupModel groupModel = groups().get(gid);
        state.change(new GroupAvatarState.StateHolder(fileName, GroupAvatarState.State.UPLOADING));

        ask(UploadActor.upload(fileName, false), new AskCallback<FileLocation>() {
            @Override
            public void onResult(FileLocation result) {
                if (state.getValue().getState() != GroupAvatarState.State.UPLOADING) {
                    return;
                }
                final long rid = RandomUtil.randomId();
                ask(requests().editGroupAvatar(new GroupOutPeer(gid, groupModel.getAccessHash()), rid,
                        new im.actor.api.scheme.FileLocation(result.getFileId(),
                                result.getAccessHash())), new FutureCallback<ResponseEditGroupAvatar>() {
                    @Override
                    public void onResult(ResponseEditGroupAvatar result) {
                        DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();

                        try {
                            String fullKey = FileKeys.avatarKey(result.getAvatar().getFullImage().getFileLocation().getFileId());
                            String file = diskCache.startWriteFile(fullKey);
                            IOUtils.copy(new File(fileName), new File(file));
                            diskCache.commitFile(fullKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        system().actorOf(SequenceActor.sequence())
                                .send(new SequenceActor.SeqUpdate(result.getSeq(), result.getState(),
                                        new UpdateGroupAvatarChanged(gid, rid, myUid(), result.getAvatar(),
                                                result.getDate())));

                        // state.change(new GroupAvatarState.StateHolder(fileName, GroupAvatarState.State.NONE));
                        system().actorOf(SequenceActor.sequence()).send(new Runnable() {
                            @Override
                            public void run() {
                                state.change(new GroupAvatarState.StateHolder(fileName, GroupAvatarState.State.NONE));
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        state.change(new GroupAvatarState.StateHolder(fileName, GroupAvatarState.State.ERROR));
                        ToastActor.get().show("Unable to change avatar");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                state.change(new GroupAvatarState.StateHolder(fileName, GroupAvatarState.State.ERROR));
                ToastActor.get().show("Unable to change avatar");
            }
        });
    }
}
