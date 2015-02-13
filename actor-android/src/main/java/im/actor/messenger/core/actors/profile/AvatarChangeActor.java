package im.actor.messenger.core.actors.profile;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.images.cache.DiskCache;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageMetadata;
import com.droidkit.images.common.ImageSaveException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageRotating;
import com.droidkit.images.ops.ImageScaling;
import com.droidkit.images.sources.FileSource;

import im.actor.api.scheme.rpc.ResponseEditAvatar;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.updates.UpdateUserAvatarChanged;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.Core;
import im.actor.messenger.core.actors.ToastActor;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.core.actors.files.base.UploadActor;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.core.images.FileKeys;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class AvatarChangeActor extends TypedActor<AvatarChangeInt> implements AvatarChangeInt {

    private static Object LOCK = new Object();
    private static AvatarChangeInt instance;

    public static AvatarChangeInt avatarSender() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = TypedCreator.typed(
                            ActorSystem.system().actorOf(AvatarChangeActor.class, "avatar/my"),
                            AvatarChangeInt.class);
                }
            }
        }
        return instance;
    }

    public AvatarChangeActor() {
        super(AvatarChangeInt.class);
    }

    private boolean isCanceled = false;

    @Override
    public void changeAvatar(String fileName) {
        isCanceled = false;
        try {
            ImageMetadata metadata = new FileSource(fileName).getImageMetadata();
            Bitmap preloaded = ImageLoading.loadBitmapOptimized(fileName);
            Bitmap optimized = ImageRotating.fixExif(preloaded, metadata.getExifOrientation());
            optimized = ImageScaling.scaleFill(optimized, 800, 800);
            String externalFile = AppContext.getInternalTempFile("avatar", "jpg");
            ImageLoading.save(optimized, externalFile);
            performUpload(externalFile);
        } catch (ImageLoadException e) {
            e.printStackTrace();
            ToastActor.get().show("Unable to load file");
        } catch (ImageSaveException e) {
            e.printStackTrace();
            ToastActor.get().show("Unable to load file");
        }
    }

    @Override
    public void clearAvatar() {
        AvatarChangeState.setFileName(null);
        AvatarChangeState.uploadingState().change(AvatarChangeState.State.UPLOADING);

        ask(requests().removeAvatar(), new FutureCallback<ResponseSeq>() {
            @Override
            public void onResult(ResponseSeq result) {
                UserActor.userActor().onAvatarChanged(myUid(), null);
                // Change avatar upload state only after user update
                UserActor.userActorRef().send(new Runnable() {
                    @Override
                    public void run() {
                        AvatarChangeState.uploadingState().change(AvatarChangeState.State.NONE);
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
    public void cancelChangingAvatar() {
        AvatarChangeState.uploadingState().change(AvatarChangeState.State.NONE);
        isCanceled = true;
    }

    @Override
    public void tryAgain() {
        performUpload(AvatarChangeState.getFileName());
        isCanceled = false;
    }


    private void performUpload(final String fileName) {
        AvatarChangeState.setFileName(fileName);
        AvatarChangeState.uploadingState().change(AvatarChangeState.State.UPLOADING);
        ask(UploadActor.upload(fileName, false), new AskCallback<FileLocation>() {
            @Override
            public void onResult(FileLocation result) {
                if (isCanceled) {
                    return;
                }
                ask(requests().editAvatar(new im.actor.api.scheme.FileLocation(result.getFileId(),
                        result.getAccessHash())), new FutureCallback<ResponseEditAvatar>() {
                    @Override
                    public void onResult(ResponseEditAvatar result) {
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
                                        new UpdateUserAvatarChanged(myUid(), result.getAvatar())));

                        system().actorOf(SequenceActor.sequence()).send(new Runnable() {
                            @Override
                            public void run() {
                                AvatarChangeState.uploadingState().change(AvatarChangeState.State.NONE);
                            }
                        });

//                        // Change avatar upload state only after user update
//                        UserActor.userActorRef().send(new Runnable() {
//                            @Override
//                            public void run() {
//                                AvatarChangeState.uploadingState().change(AvatarChangeState.State.NONE);
//                            }
//                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        AvatarChangeState.uploadingState().change(AvatarChangeState.State.ERROR);
                        ToastActor.get().show("Unable to change avatar");
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ToastActor.get().show("Unable to change avatar");
                AvatarChangeState.uploadingState().change(AvatarChangeState.State.ERROR);
            }
        });
    }
}
