package im.actor.messenger.core.actors.send;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.widget.Toast;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.tasks.AskFuture;
import com.droidkit.actors.tasks.AskProgressCallback;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.engine.persistence.BserMap;
import com.droidkit.engine.persistence.PersistenceSet;
import com.droidkit.engine.persistence.storage.SqliteStorage;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.common.ImageMetadata;
import com.droidkit.images.common.ImageSaveException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.images.ops.ImageRotating;
import com.droidkit.images.ops.ImageScaling;
import com.droidkit.images.sources.FileSource;

import im.actor.api.scheme.FileExPhoto;
import im.actor.api.scheme.FileExVideo;
import im.actor.api.scheme.FileExVoice;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.ToastActor;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.files.DownloadManager;
import im.actor.messenger.core.actors.files.base.UploadActor;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.UploadModel;
import im.actor.messenger.model.UploadState;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.messages.FastThumb;
import im.actor.messenger.storage.scheme.messages.PendingUpload;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ex3ndr on 01.10.14.
 */
public class MediaSenderActor extends TypedActor<MediaSenderInt> implements MediaSenderInt {

    private static final Object LOCK = new Object();
    private static MediaSenderInt dialogInterface;

    public static MediaSenderInt mediaSender() {
        if (dialogInterface == null) {
            synchronized (LOCK) {
                if (dialogInterface == null) {
                    dialogInterface = TypedCreator.typed(ActorSystem.system().actorOf(MediaSenderActor.class, "sender_media"),
                            MediaSenderInt.class);
                }
            }
        }
        return dialogInterface;
    }

    public MediaSenderActor() {
        super(MediaSenderInt.class);
    }

    private HashMap<Long, AskFuture> futures = new HashMap<Long, AskFuture>();

    private PersistenceSet<PendingUpload> pendingUploads;

    @Override
    public void preStart() {
        pendingUploads = new PersistenceSet<PendingUpload>(new BserMap<PendingUpload>(
                new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()), "pending_uploads"), PendingUpload.class));
//        for (PendingUpload pendingUpload : pendingUploads) {
//            self().send(pendingUpload);
//        }
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof PendingUpload) {
            PendingUpload pendingUpload = (PendingUpload) message;

            MessageModel messageModel = ListEngines.getMessagesListEngine(
                    DialogUids.getDialogUid(pendingUpload.getChatType(), pendingUpload.getChatId()))
                    .getValue(pendingUpload.getRid());

            if (messageModel == null) {
                return;
            }

            if (pendingUpload.isStopped()) {
                return;
            }

            sendFile(pendingUpload);
        }
    }

    @Override
    public void sendDocument(final int type, final int id, final String fileName,
                             final String realName, boolean isEncrypted) {
        File file = new File(fileName);
        final int size = (int) file.length();
        if (size <= 0) {
            return;
        }

        byte[] thumb = null;
        FastThumb fastThumb = null;

        try {
            Bitmap preview = ImageLoading.loadBitmapOptimized(fileName);
            Bitmap smallThumb = ImageScaling.scaleFit(preview, 90, 90);
            byte[] smallThumbData = ImageLoading.saveJpeg(smallThumb, ImageLoading.JPEG_QUALITY_LOW);
            fastThumb = new FastThumb(smallThumb.getWidth(), smallThumb.getHeight(), smallThumbData);
            thumb = fastThumb.toByteArray();
        } catch (ImageLoadException e) {
            e.printStackTrace();
        } catch (ImageSaveException e) {
            e.printStackTrace();
        }

        DocumentMessage documentMessage = new DocumentMessage(fileName, realName, size, isEncrypted,
                fastThumb);

        sendFile(type, id, realName, thumb, documentMessage, 0, new byte[0], isEncrypted);
    }

    @Override
    public void sendPhoto(final int type, final int id, String fileName, boolean isEncrypted) {
        try {
            // Preparing image
            ImageMetadata metadata = new FileSource(fileName).getImageMetadata();
            Bitmap preloaded = ImageLoading.loadBitmapOptimizedHQ(fileName);
            final Bitmap optimized = ImageRotating.fixExif(preloaded, metadata.getExifOrientation());
            final Bitmap smallThumb = ImageScaling.scaleFit(optimized, 90, 90);
            final byte[] data = ImageLoading.saveJpeg(smallThumb, ImageLoading.JPEG_QUALITY_LOW);
            final String resultFileName = AppContext.getExternalTempFile("image", "jpg");
            if (resultFileName == null) {
                return;
            }
            ImageLoading.save(optimized, resultFileName);

            // Preparing parameters
            final FastThumb thumb = new FastThumb(
                    smallThumb.getWidth(),
                    smallThumb.getHeight(),
                    data);
            byte[] thumbData = thumb.toByteArray();

            // Successful prepare

            PhotoMessage photoMessage = new PhotoMessage(resultFileName, optimized.getWidth(),
                    optimized.getHeight(), thumb, isEncrypted);

            byte[] extension;
            int extType;
            if (isEncrypted) {
                extension = EncryptedMessages.photoMetadata(optimized.getWidth(), optimized.getHeight());
                extType = 0x01;
            } else {
                extension = new FileExPhoto(optimized.getWidth(), optimized.getHeight()).toByteArray();
                extType = 0x01;
            }

            sendFile(type, id, "image.jpg", thumbData, photoMessage, extType, extension, isEncrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendVideo(final int type, final int id, final String fileName, boolean isEncrypted) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fileName);
            int duration = (int) (Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000L);
            Bitmap img = retriever.getFrameAtTime(0);
            int width = img.getWidth();
            int height = img.getHeight();
            Bitmap smallThumb = ImageScaling.scaleFit(img, 90, 90);
            byte[] smallThumbData = ImageLoading.saveJpeg(smallThumb, ImageLoading.JPEG_QUALITY_LOW);

            FastThumb thumb = new FastThumb(smallThumb.getWidth(), smallThumb.getHeight(), smallThumbData);
            byte[] thumbData = thumb.toByteArray();

            VideoMessage videoMessage = new VideoMessage(fileName, duration, width, height, thumb,
                    isEncrypted);

            byte[] extension;
            int extType;
            if (isEncrypted) {
                extension = EncryptedMessages.videoMetadata(duration, width, height);
                extType = 0x02;
            } else {
                extension = new FileExVideo(width, height, duration).toByteArray();
                extType = 0x02;
            }

            sendFile(type, id, "video.mp4", thumbData, videoMessage, extType, extension, isEncrypted);
        } catch (Exception e) {
            e.printStackTrace();
            ToastActor.get().show("Unable to open video");
        }
    }

    @Override
    public void sendOpus(final int type, final int id, final String fileName, final int duration,
                         boolean isEncrypted) {
        AudioMessage audioMessage = new AudioMessage(fileName, duration, isEncrypted);

        byte[] extension;
        int extType;
        if (isEncrypted) {
            extension = EncryptedMessages.opusMetadata(duration);
            extType = 0x03;
        } else {
            extension = new FileExVoice(duration).toByteArray();
            extType = 0x03;
        }

        sendFile(type, id, "voice.ogg", new byte[0], audioMessage, extType, extension, isEncrypted);
    }

    @Override
    public void tryAgain(int type, int id, long rid) {
        for (PendingUpload pendingUpload : pendingUploads) {
            if (pendingUpload.getChatType() == type && pendingUpload.getChatId() == id &&
                    pendingUpload.getRid() == rid) {
                if (!pendingUpload.isStopped()) {
                    return;
                }
                pendingUploads.remove(pendingUpload);
                PendingUpload started = pendingUpload.start();
                pendingUploads.add(started);
                self().send(started);
                break;
            }
        }
    }

    @Override
    public void pause(int type, int id, long rid) {
        if (futures.containsKey(rid)) {
            futures.get(rid).cancel();
            UploadModel.uploadState(rid).change(new UploadState(UploadState.State.NONE));
        }

        for (PendingUpload pendingUpload : pendingUploads) {
            if (pendingUpload.getChatType() == type && pendingUpload.getChatId() == id &&
                    pendingUpload.getRid() == rid) {
                pendingUploads.remove(pendingUpload);
                pendingUploads.add(pendingUpload.stop());
                break;
            }
        }
    }

    @Override
    public void cancel(int type, int id, long rid) {
        if (futures.containsKey(rid)) {
            futures.get(rid).cancel();
            UploadModel.uploadState(rid).change(new UploadState(UploadState.State.NONE));
        }
        for (PendingUpload pendingUpload : pendingUploads) {
            if (pendingUpload.getChatType() == type && pendingUpload.getChatId() == id &&
                    pendingUpload.getRid() == rid) {
                pendingUploads.remove(pendingUpload);
                break;
            }
        }
    }

    @Override
    public void cancelAll(int type, int id) {
        // TODO: Fix
        Iterator<PendingUpload> iterator = pendingUploads.iterator();
        while (iterator.hasNext()) {
            PendingUpload upload = iterator.next();
            if (upload.getChatType() == type && upload.getChatId() == id) {
                if (futures.containsKey(upload.getRid())) {
                    futures.get(upload.getRid()).cancel();
                    UploadModel.uploadState(upload.getRid()).change(new UploadState(UploadState.State.NONE));
                }
                iterator.remove();
            }
        }
    }

    private void sendFile(final int type, final int id,
                          final String name, final byte[] thumb,
                          final AbsFileMessage fileMessage,
                          final int extType, final byte[] extension,
                          final boolean isEncrypted) {

        ask(ConversationActor.conv(type, id).onStartUpload(fileMessage),
                new FutureCallback<MessageModel>() {
                    @Override
                    public void onResult(MessageModel result) {
                        PendingUpload pendingUpload = new PendingUpload(type, id, result.getRid(),
                                fileMessage.getUploadPath(), name, extType, extension,
                                thumb, false, isEncrypted);

                        pendingUploads.add(pendingUpload);
                        sendFile(pendingUpload);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private void sendFile(final PendingUpload pendingUpload) {
        final long rid = pendingUpload.getRid();
        UploadModel.uploadState(rid).change(new UploadState(UploadState.State.UPLOADING, 0));

        if (futures.containsKey(rid)) {
            futures.remove(rid).cancel();
        }

        AskFuture future = ask(UploadActor.upload(pendingUpload.getFileName(), pendingUpload.isEncrypted()),
                new AskProgressCallback<FileLocation, Integer>() {
                    @Override
                    public void onResult(FileLocation result) {
                        futures.remove(rid);
                        pendingUploads.remove(pendingUpload);

                        DownloadManager.downloader().writeToStorage(pendingUpload.getFileName(), pendingUpload.getName(), result);
                        UploadModel.uploadState(rid).change(new UploadState(UploadState.State.UPLOADED, 100));

                        MessageSendActor.messageSender().sendFile(pendingUpload.getChatType(), pendingUpload.getChatId(),
                                pendingUpload.getRid(), result, pendingUpload.getName(), pendingUpload.getMessageType(),
                                pendingUpload.getMetadata(), pendingUpload.getThumb(), pendingUpload.isEncrypted());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        futures.remove(rid);
                        pendingUploads.remove(pendingUpload);
                        ConversationActor.conv(pendingUpload.getChatType(), pendingUpload.getChatId()).onMessageError(rid);
                        UploadModel.uploadState(rid).change(new UploadState(UploadState.State.NONE, 0));
                    }

                    @Override
                    public void onProgress(Integer integer) {
                        UploadModel.uploadState(rid).change(new UploadState(UploadState.State.UPLOADING, integer));
                    }
                });

        futures.put(rid, future);
    }
}
