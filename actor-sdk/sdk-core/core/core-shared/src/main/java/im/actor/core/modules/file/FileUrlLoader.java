package im.actor.core.modules.file;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.ApiFileUrlDescription;
import im.actor.core.api.rpc.RequestGetFileUrls;
import im.actor.core.api.rpc.ResponseGetFileUrls;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

public class FileUrlLoader extends ModuleActor {

    private HashMap<Long, Promise<String>> requestedFiles = new HashMap<>();

    private ArrayList<RequestedFile> pendingFiles = new ArrayList<>();
    private boolean isExecuting = false;
    private Cancellable checkCancellable;

    public FileUrlLoader(ModuleContext context) {
        super(context);
    }

    public void checkQueue() {
        if (isExecuting) {
            return;
        }

        if (pendingFiles.size() == 0) {
            return;
        }

        final ArrayList<RequestedFile> destFiles = new ArrayList<>(pendingFiles);
        pendingFiles.clear();

        isExecuting = true;
        ArrayList<ApiFileLocation> locations = new ArrayList<>();
        for (RequestedFile f : destFiles) {
            Log.d("FileUrlLoader", "api: " + f.getFileId());
            locations.add(new ApiFileLocation(f.getFileId(), f.getAccessHash()));
        }
        api(new RequestGetFileUrls(locations)).then(responseGetFileUrls -> {

            outer:
            for (RequestedFile f : destFiles) {
                for (ApiFileUrlDescription urlDescription : responseGetFileUrls.getFileUrls()) {
                    if (f.getFileId() == urlDescription.getFileId()) {
                        Log.d("FileUrlLoader", "resp: " + f.getFileId());
                        // TODO: Implement Timeouts
                        f.getResolver().result(urlDescription.getUrl());

                        continue outer;
                    }
                }
            }
            isExecuting = false;
            scheduleCheck();
        }).failure(e -> {
            for (RequestedFile f : destFiles) {
                f.getResolver().error(e);
            }
            isExecuting = false;
            scheduleCheck();
        });
    }

    public Promise<String> askUrl(final long fileId, final long accessHash) {
        Log.d("FileUrlLoader", "request: " + fileId);
        if (requestedFiles.containsKey(fileId)) {
            return requestedFiles.get(fileId);
        }
        final Promise<String> res = new Promise<>((PromiseFunc<String>) resolver -> {
            pendingFiles.add(new RequestedFile(fileId, accessHash, resolver));
            scheduleCheck();
        });
        requestedFiles.put(fileId, res);
        return res;
    }

    private void scheduleCheck() {
        if (checkCancellable != null) {
            checkCancellable.cancel();

        }
        checkCancellable = schedule(new CheckQueue(), 50);
    }


    //
    // Messages
    //

    @Override
    public Promise onAsk(Object message) throws Exception {
        if (message instanceof AskUrl) {
            AskUrl askUrl = (AskUrl) message;
            return askUrl(askUrl.getFileId(), askUrl.getAccessHash());
        } else {
            return super.onAsk(message);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof CheckQueue) {
            checkQueue();
        } else {
            super.onReceive(message);
        }
    }

    private static class RequestedFile {

        private final long fileId;
        private final long accessHash;
        private final PromiseResolver<String> resolver;

        public RequestedFile(long fileId, long accessHash, PromiseResolver<String> resolver) {
            this.fileId = fileId;
            this.accessHash = accessHash;
            this.resolver = resolver;
        }

        public long getFileId() {
            return fileId;
        }

        public long getAccessHash() {
            return accessHash;
        }

        public PromiseResolver<String> getResolver() {
            return resolver;
        }
    }

    public static class AskUrl implements AskMessage<String> {

        private long fileId;
        private long accessHash;

        public AskUrl(long fileId, long accessHash) {
            this.fileId = fileId;
            this.accessHash = accessHash;
        }

        public long getFileId() {
            return fileId;
        }

        public long getAccessHash() {
            return accessHash;
        }
    }

    private static class CheckQueue {

    }
}