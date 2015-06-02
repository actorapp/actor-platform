/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.model.api.DocumentEx;
import im.actor.model.api.DocumentExPhoto;
import im.actor.model.api.DocumentExVideo;
import im.actor.model.api.DocumentMessage;
import im.actor.model.api.OutPeer;
import im.actor.model.api.TextMessage;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestSendMessage;
import im.actor.model.api.rpc.ResponseSeqDate;
import im.actor.model.api.updates.UpdateMessageSent;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.MessageState;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.TextContent;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.modules.Modules;
import im.actor.model.modules.file.UploadManager;
import im.actor.model.modules.messages.entity.PendingMessage;
import im.actor.model.modules.messages.entity.PendingMessagesStorage;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;

public class SenderActor extends ModuleActor {

    private static final String PREFERENCES = "sender_pending";

    private PendingMessagesStorage pendingMessages;

    public SenderActor(Modules messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        pendingMessages = new PendingMessagesStorage();
        byte[] p = preferences().getBytes(PREFERENCES);
        if (p != null) {
            try {
                pendingMessages = PendingMessagesStorage.fromBytes(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean isChanged = false;
        ArrayList<PendingMessage> messages = pendingMessages.getPendingMessages();
        for (PendingMessage pending : messages.toArray(new PendingMessage[messages.size()])) {
            if (pending.getContent() instanceof TextContent) {
                performSendContent(pending.getPeer(), pending.getRid(), pending.getContent());
            } else if (pending.getContent() instanceof DocumentContent) {
                DocumentContent documentContent = (DocumentContent) pending.getContent();
                if (documentContent.getSource() instanceof FileLocalSource) {
                    if (config().getFileSystemProvider() != null &&
                            config().getFileSystemProvider().isFsPersistent()) {
                        performUploadFile(pending.getRid(),
                                ((FileLocalSource) documentContent.getSource()).getFileDescriptor(),
                                ((FileLocalSource) documentContent.getSource()).getFileName());
                    } else {
                        List<Long> rids = new ArrayList<Long>();
                        rids.add(pending.getRid());
                        getConversationActor(pending.getPeer()).send(new ConversationActor.MessagesDeleted(rids));
                        pendingMessages.getPendingMessages().remove(pending);
                        isChanged = true;
                    }
                } else {
                    performSendContent(pending.getPeer(), pending.getRid(),
                            pending.getContent());
                }
            }
        }

        if (isChanged) {
            savePending();
        }
    }

    // Sending text


    public void doSendText(Peer peer, String text, ArrayList<Integer> mentions) {
        long rid = RandomUtils.nextRid();
        long date = Environment.getCurrentSyncedTime();
        long sortDate = date + 365 * 24 * 60 * 60 * 1000L;
        TextContent content = TextContent.create(text, mentions);

        Message message = new Message(rid, sortDate, date, myUid(), MessageState.PENDING, content);
        getConversationActor(peer).send(message);

        pendingMessages.getPendingMessages().add(new PendingMessage(peer, rid, content));
        savePending();

        performSendContent(peer, rid, content);
    }

    // Sending documents

    public void doSendDocument(Peer peer, String fileName, String mimeType, int fileSize,
                               FastThumb fastThumb, String descriptor) {
        long rid = RandomUtils.nextRid();
        long date = Environment.getCurrentSyncedTime();
        long sortDate = date + 365 * 24 * 60 * 60 * 1000L;
        DocumentContent documentContent = DocumentContent.createLocal(fileName, fileSize,
                descriptor, mimeType, fastThumb);

        Message message = new Message(rid, sortDate, date, myUid(), MessageState.PENDING, documentContent);
        getConversationActor(peer).send(message);

        pendingMessages.getPendingMessages().add(new PendingMessage(peer, rid, documentContent));
        savePending();

        performUploadFile(rid, descriptor, fileName);
    }

    public void doSendPhoto(Peer peer, FastThumb fastThumb, String descriptor, String fileName,
                            int fileSize, int w, int h) {
        long rid = RandomUtils.nextRid();
        long date = Environment.getCurrentSyncedTime();
        long sortDate = date + 365 * 24 * 60 * 60 * 1000L;
        PhotoContent photoContent = PhotoContent.createLocalPhoto(descriptor, fileName, fileSize, w, h, fastThumb);

        Message message = new Message(rid, sortDate, date, myUid(), MessageState.PENDING, photoContent);
        getConversationActor(peer).send(message);

        pendingMessages.getPendingMessages().add(new PendingMessage(peer, rid, photoContent));
        savePending();

        performUploadFile(rid, descriptor, fileName);
    }

    public void doSendVideo(Peer peer, String fileName, int w, int h, int duration,
                            FastThumb fastThumb, String descriptor, int fileSize) {
        long rid = RandomUtils.nextRid();
        long date = Environment.getCurrentSyncedTime();
        long sortDate = date + 365 * 24 * 60 * 60 * 1000L;
        VideoContent videoContent = VideoContent.createLocalVideo(descriptor,
                fileName, fileSize, w, h, duration, fastThumb);

        Message message = new Message(rid, sortDate, date, myUid(), MessageState.PENDING, videoContent);
        getConversationActor(peer).send(message);

        pendingMessages.getPendingMessages().add(new PendingMessage(peer, rid, videoContent));
        savePending();

        performUploadFile(rid, descriptor, fileName);
    }

    private void performUploadFile(long rid, String descriptor, String fileName) {
        modules().getFilesModule().requestUpload(rid, descriptor, fileName, self());
    }

    private void onFileUploaded(long rid, FileReference fileReference) {
        PendingMessage msg = findPending(rid);
        if (msg == null) {
            return;
        }

        pendingMessages.getPendingMessages().remove(msg);

        AbsContent nContent;
        if (msg.getContent() instanceof PhotoContent) {
            PhotoContent basePhotoContent = (PhotoContent) msg.getContent();
            nContent = PhotoContent.createRemotePhoto(fileReference, basePhotoContent.getW(),
                    basePhotoContent.getH(), basePhotoContent.getFastThumb());
        } else if (msg.getContent() instanceof VideoContent) {
            VideoContent baseVideoContent = (VideoContent) msg.getContent();
            nContent = VideoContent.createRemotePhoto(fileReference, baseVideoContent.getW(),
                    baseVideoContent.getH(), baseVideoContent.getDuration(),
                    baseVideoContent.getFastThumb());
        } else if (msg.getContent() instanceof DocumentContent) {
            DocumentContent baseDocContent = (DocumentContent) msg.getContent();
            nContent = DocumentContent.createRemoteDocument(fileReference, baseDocContent.getFastThumb());
        } else {
            return;
        }

        pendingMessages.getPendingMessages().add(new PendingMessage(msg.getPeer(), msg.getRid(), nContent));
        getConversationActor(msg.getPeer()).send(new ConversationActor.MessageContentUpdated(msg.getRid(), nContent));

        performSendContent(msg.getPeer(), rid, nContent);
    }

    private void onFileUploadError(long rid) {
        PendingMessage msg = findPending(rid);
        if (msg == null) {
            return;
        }

        self().send(new MessageError(msg.getPeer(), msg.getRid()));
    }

    // Sending content

    private void performSendContent(final Peer peer, final long rid, AbsContent content) {
        final OutPeer outPeer = buidOutPeer(peer);
        final im.actor.model.api.Peer apiPeer = buildApiPeer(peer);
        if (outPeer == null || apiPeer == null) {
            return;
        }

        im.actor.model.api.Message message;
        if (content instanceof TextContent) {
            message = new TextMessage(((TextContent) content).getText(), new ArrayList<Integer>(), null);
        } else if (content instanceof DocumentContent) {
            DocumentContent documentContent = (DocumentContent) content;

            FileRemoteSource source = (FileRemoteSource) documentContent.getSource();

            DocumentEx documentEx = null;

            if (content instanceof PhotoContent) {
                PhotoContent photoContent = (PhotoContent) content;
                documentEx = new DocumentExPhoto(photoContent.getW(), photoContent.getH());
            } else if (content instanceof VideoContent) {
                VideoContent videoContent = (VideoContent) content;
                documentEx = new DocumentExVideo(videoContent.getW(), videoContent.getH(), videoContent.getDuration());
            }

            im.actor.model.api.FastThumb fastThumb = null;
            if (documentContent.getFastThumb() != null) {
                fastThumb = new im.actor.model.api.FastThumb(
                        documentContent.getFastThumb().getW(),
                        documentContent.getFastThumb().getH(),
                        documentContent.getFastThumb().getImage());
            }

            message = new DocumentMessage(source.getFileReference().getFileId(),
                    source.getFileReference().getAccessHash(),
                    source.getFileReference().getFileSize(),
                    source.getFileReference().getFileName(),
                    documentContent.getMimeType(),
                    fastThumb, documentEx);
        } else {
            return;
        }

        request(new RequestSendMessage(outPeer, rid, message),
                new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {
                        self().send(new MessageSent(peer, rid));
                        updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                                response.getState(),
                                UpdateMessageSent.HEADER,
                                new UpdateMessageSent(apiPeer, rid, response.getDate()).toByteArray()));
                    }

                    @Override
                    public void onError(RpcException e) {
                        self().send(new MessageError(peer, rid));
                    }
                });
    }

    private void onSent(Peer peer, long rid) {
        for (PendingMessage pending : pendingMessages.getPendingMessages()) {
            if (pending.getRid() == rid && pending.getPeer().equals(peer)) {
                pendingMessages.getPendingMessages().remove(pending);
                break;
            }
        }
        savePending();
    }

    private void onError(Peer peer, long rid) {
        for (PendingMessage pending : pendingMessages.getPendingMessages()) {
            if (pending.getRid() == rid && pending.getPeer().equals(peer)) {
                pendingMessages.getPendingMessages().remove(pending);
                break;
            }
        }
        savePending();
        getConversationActor(peer).send(new ConversationActor.MessageError(rid));
    }

    private void savePending() {
        preferences().putBytes(PREFERENCES, pendingMessages.toByteArray());
    }

    private PendingMessage findPending(long rid) {
        for (PendingMessage message : pendingMessages.getPendingMessages()) {
            if (message.getRid() == rid) {
                return message;
            }
        }
        return null;
    }

    //region Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof SendText) {
            SendText sendText = (SendText) message;
            doSendText(sendText.getPeer(), sendText.getText(), sendText.getMentions());
        } else if (message instanceof MessageSent) {
            MessageSent messageSent = (MessageSent) message;
            onSent(messageSent.getPeer(), messageSent.getRid());
        } else if (message instanceof MessageError) {
            MessageError messageError = (MessageError) message;
            onError(messageError.getPeer(), messageError.getRid());
        } else if (message instanceof SendDocument) {
            SendDocument sendDocument = (SendDocument) message;
            doSendDocument(sendDocument.getPeer(), sendDocument.getFileName(), sendDocument.getMimeType(),
                    sendDocument.getFileSize(), sendDocument.getFastThumb(), sendDocument.getDescriptor());
        } else if (message instanceof UploadManager.UploadCompleted) {
            UploadManager.UploadCompleted uploadCompleted = (UploadManager.UploadCompleted) message;
            onFileUploaded(uploadCompleted.getRid(), uploadCompleted.getFileReference());
        } else if (message instanceof UploadManager.UploadError) {
            UploadManager.UploadError uploadError = (UploadManager.UploadError) message;
            onFileUploadError(uploadError.getRid());
        } else if (message instanceof SendPhoto) {
            SendPhoto sendPhoto = (SendPhoto) message;
            doSendPhoto(sendPhoto.getPeer(), sendPhoto.getFastThumb(),
                    sendPhoto.getDescriptor(), sendPhoto.getFileName(), sendPhoto.getFileSize(),
                    sendPhoto.getW(), sendPhoto.getH());
        } else if (message instanceof SendVideo) {
            SendVideo sendVideo = (SendVideo) message;
            doSendVideo(sendVideo.getPeer(), sendVideo.getFileName(),
                    sendVideo.getW(), sendVideo.getH(), sendVideo.getDuration(),
                    sendVideo.getFastThumb(), sendVideo.getDescriptor(), sendVideo.getFileSize());
        } else {
            drop(message);
        }
    }

    public static class SendDocument {
        private Peer peer;
        private FastThumb fastThumb;
        private String descriptor;
        private String fileName;
        private String mimeType;
        private int fileSize;

        public SendDocument(Peer peer, String fileName, String mimeType, int fileSize, String descriptor,
                            FastThumb fastThumb) {
            this.peer = peer;
            this.fastThumb = fastThumb;
            this.descriptor = descriptor;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }

        public FastThumb getFastThumb() {
            return fastThumb;
        }

        public int getFileSize() {
            return fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Peer getPeer() {
            return peer;
        }

        public String getDescriptor() {
            return descriptor;
        }
    }

    public static class SendPhoto {
        private Peer peer;
        private FastThumb fastThumb;
        private String descriptor;
        private String fileName;
        private int fileSize;
        private int w;
        private int h;

        public SendPhoto(Peer peer, FastThumb fastThumb, String descriptor, String fileName,
                         int fileSize, int w, int h) {
            this.peer = peer;
            this.fastThumb = fastThumb;
            this.descriptor = descriptor;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.w = w;
            this.h = h;
        }

        public Peer getPeer() {
            return peer;
        }

        public FastThumb getFastThumb() {
            return fastThumb;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public String getFileName() {
            return fileName;
        }

        public int getFileSize() {
            return fileSize;
        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }
    }

    public static class SendVideo {
        private Peer peer;
        private String fileName;
        private int w;
        private int h;
        private int duration;
        private FastThumb fastThumb;
        private String descriptor;
        private int fileSize;

        public SendVideo(Peer peer, String fileName, int w, int h, int duration,
                         FastThumb fastThumb, String descriptor, int fileSize) {
            this.peer = peer;
            this.fileName = fileName;
            this.w = w;
            this.h = h;
            this.duration = duration;
            this.fastThumb = fastThumb;
            this.descriptor = descriptor;
            this.fileSize = fileSize;
        }

        public Peer getPeer() {
            return peer;
        }

        public String getFileName() {
            return fileName;
        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }

        public int getDuration() {
            return duration;
        }

        public FastThumb getFastThumb() {
            return fastThumb;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public int getFileSize() {
            return fileSize;
        }
    }

    public static class SendText {
        private Peer peer;
        private String text;
        private ArrayList<Integer> mentions;

        public SendText(Peer peer, String text, ArrayList<Integer> mentions) {
            this.peer = peer;
            this.text = text;
            this.mentions = mentions;
        }

        public Peer getPeer() {
            return peer;
        }

        public String getText() {
            return text;
        }

        public ArrayList<Integer> getMentions() {
            return mentions;
        }
    }

    public static class MessageSent {
        private Peer peer;
        private long rid;

        public MessageSent(Peer peer, long rid) {
            this.peer = peer;
            this.rid = rid;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }
    }

    public static class MessageError {
        private Peer peer;
        private long rid;

        public MessageError(Peer peer, long rid) {
            this.peer = peer;
            this.rid = rid;
        }

        public Peer getPeer() {
            return peer;
        }

        public long getRid() {
            return rid;
        }
    }

    //endregion
}