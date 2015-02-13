package im.actor.messenger.core.actors.files;

import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.tasks.AskFuture;
import com.droidkit.actors.tasks.AskProgressCallback;
import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.chat.ConversationActor;
import im.actor.messenger.core.actors.files.base.DownloadActor;
import im.actor.messenger.model.DownloadModel;
import im.actor.messenger.model.DownloadState;
import im.actor.messenger.storage.scheme.FileLocation;
import im.actor.messenger.storage.scheme.media.Downloaded;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;
import im.actor.messenger.util.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import static com.droidkit.actors.typed.TypedCreator.typed;
import static im.actor.messenger.storage.KeyValueEngines.downloaded;

/**
 * Created by ex3ndr on 05.09.14.
 */
public class DownloadManager extends TypedActor<DownloadManagerInt> implements DownloadManagerInt {

    public DownloadManager() {
        super(DownloadManagerInt.class);
    }

    public static DownloadManagerInt downloader() {
        return typed(ActorSystem.system().actorOf(selection()), DownloadManagerInt.class);
    }

    public static ActorSelection selection() {
        return new ActorSelection(Props.create(DownloadManager.class), "download_manager");
    }

    private static final String TAG = "DownloadManager";

    private static final int MAX_DOWNLOADS = 2;

    private ArrayList<DownloadRequest> requests = new ArrayList<DownloadRequest>();

    private HashSet<Long> cancelled = new HashSet<Long>();

    @Override
    public void request(int type, int id, long rid, AbsFileMessage fileMessage, boolean isAutomatic) {
        FileLocation location = fileMessage.getLocation();
        String name;
        if (fileMessage instanceof PhotoMessage) {
            name = "image.jpg";
        } else if (fileMessage instanceof AudioMessage) {
            name = "voice.ogg";
        } else if (fileMessage instanceof DocumentMessage) {
            name = ((DocumentMessage) fileMessage).getName();
        } else if (fileMessage instanceof VideoMessage) {
            name = "video.mp4";
        } else {
            name = "file.bin";
        }

        Logger.d(TAG, "Requested #" + location.getFileId());

        Downloaded downloaded = downloaded().get(location.getFileId());
        if (downloaded != null) {
            Logger.d(TAG, "Already Downloaded");
            ConversationActor.conv(type, id).onMessageDownloaded(rid);
            return;
        }

        for (int i = 0; i < requests.size(); i++) {
            DownloadRequest downloadRequest = requests.get(i);
            if (downloadRequest.location.getFileId() == location.getFileId()) {
                if (downloadRequest.rids.contains(rid)) {
                    Logger.d(TAG, "Already in list");
                    return;
                }

                downloadRequest.rids.add(rid);

                if (!downloadRequest.isActive) {
                    Logger.d(TAG, "Move request to top");
                    requests.remove(downloadRequest);
                    requests.add(0, downloadRequest);
                }

                return;
            }
        }

        if (isAutomatic && cancelled.contains(location.getFileId())) {
            return;
        }

        Logger.d(TAG, "Adding request to queue");
        ArrayList<Long> rids = new ArrayList<Long>();
        rids.add(rid);
        requests.add(0, new DownloadRequest(type, id, rids, name, location));

        DownloadModel.downloadState(location.getFileId()).change(new DownloadState(DownloadState.State.DOWNLOADING));

        checkQueue();
    }

    @Override
    public void pause(AbsFileMessage fileMessage) {
        FileLocation location = fileMessage.getLocation();
        for (int i = 0; i < requests.size(); i++) {
            DownloadRequest downloadRequest = requests.get(i);
            if (downloadRequest.location.getFileId() == location.getFileId()) {
                if (downloadRequest.isActive) {
                    downloadRequest.future.cancel();
                }
                requests.remove(downloadRequest);
                DownloadModel.downloadState(location.getFileId()).change(new DownloadState(DownloadState.State.NONE));
                cancelled.add(location.getFileId());
            }
        }
    }

    @Override
    public void writeToStorage(String fileName, String name, FileLocation fileLocation) {
        String destFileName = getDestFileName(name);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(fileName);
            out = new FileOutputStream(destFileName);

            // Transfer bytes from in to out
            byte[] buf = new byte[4 * 1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                Thread.yield();
                out.write(buf, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        downloaded().put(new Downloaded(fileLocation.getFileId(), fileLocation.getFileSize(), "file.bin", destFileName));
    }

    @Override
    public Future<String> downloadedFileName(FileLocation fileLocation) {
        Downloaded downloaded = downloaded().get(fileLocation.getFileId());
        if (downloaded == null) {
            throw new RuntimeException("Image not downloaded");
        }
        return result(downloaded.getDownloadedPath());
    }

    private String getDestFileName(String name) {
        File externalFile = AppContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }

        String externalPath = externalFile.getAbsolutePath();
        new File(externalPath + "/actor/").mkdirs();

        String prefix = name;
        String ext = "";

        if (name.contains(".")) {
            prefix = name.substring(0, name.lastIndexOf('.'));
            ext = name.substring(prefix.length());
        }

        int index = 0;
        String n;
        while (true) {
            n = index == 0 ? prefix + ext : prefix + "_" + index + ext;
            if (!new File(externalPath + "/actor/" + n).exists()) {
                break;
            }
            index++;
        }
        String res = externalPath + "/actor/" + n;
        File f = new File(res);
        // f.mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private void checkQueue() {
        Logger.d(TAG, "checkQueue");

        int count = 0;
        for (DownloadRequest request : requests) {
            if (request.isActive) {
                count++;
            }
        }

        if (count >= MAX_DOWNLOADS) {
            Logger.d(TAG, "Already have required number of downloads");
            return;
        }

        DownloadRequest req = null;

        for (DownloadRequest request : requests) {
            if (!request.isActive) {
                req = request;
                break;
            }
        }

        if (req != null) {
            final String path = getDestFileName(req.name);
            if (path == null) {
                Logger.d(TAG, "No external folder: aborting");
                requests.remove(req);
                DownloadModel.downloadState(req.location.getFileId()).change(new DownloadState(DownloadState.State.NONE));
                return;
            }
            final long fileId = req.location.getFileId();
            final DownloadRequest finalReq = req;

            Logger.d(TAG, "Starting work for #" + req.location.getFileId() + " to " + path);
            req.isActive = true;
            req.future = ask(DownloadActor.download(req.location, path), new AskProgressCallback<String, Integer>() {
                @Override
                public void onResult(String result) {
                    downloaded().putSync(new Downloaded(finalReq.location.getFileId(), finalReq.location.getFileSize(), finalReq.name, result));

                    DownloadModel.downloadState(finalReq.location.getFileId()).change(new DownloadState(DownloadState.State.DOWNLOADED));

                    for (long rid : finalReq.rids) {
                        ConversationActor.conv(finalReq.type, finalReq.id).onMessageDownloaded(rid);
                    }

                    Logger.d(TAG, "Downloaded #" + finalReq.location.getFileId() + " to " + result);
                    requests.remove(finalReq);

                    checkQueue();
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.d(TAG, "Error during download");
                    requests.remove(finalReq);

                    DownloadModel.downloadState(finalReq.location.getFileId()).change(new DownloadState(DownloadState.State.NONE));

                    checkQueue();
                }

                @Override
                public void onProgress(Integer progress) {
                    Logger.d(TAG, "Downloaded #" + fileId + " " + progress + "%");

                    DownloadModel.downloadState(finalReq.location.getFileId()).change(new DownloadState(DownloadState.State.DOWNLOADING, progress));

                    checkQueue();
                }
            });
            checkQueue();
        } else {
            Logger.d(TAG, "No work for downloader");
        }
    }

    private class DownloadRequest {
        private FileLocation location;
        private String name;
        private int type;
        private int id;
        private boolean isActive;
        private ArrayList<Long> rids;
        private AskFuture future;

        private DownloadRequest(int type, int id, ArrayList<Long> rids, String name, FileLocation location) {
            this.location = location;
            this.type = type;
            this.id = id;
            this.name = name;
            this.rids = rids;
        }
    }
}
