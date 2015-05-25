/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.model.api.OutPeer;
import im.actor.model.api.base.SeqUpdate;
import im.actor.model.api.rpc.RequestClearChat;
import im.actor.model.api.rpc.RequestDeleteChat;
import im.actor.model.api.rpc.ResponseSeq;
import im.actor.model.api.updates.UpdateChatClear;
import im.actor.model.api.updates.UpdateChatDelete;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListStorage;
import im.actor.model.droidkit.engine.SyncKeyValue;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Group;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.files.FileSystemReference;
import im.actor.model.modules.messages.ConversationActor;
import im.actor.model.modules.messages.ConversationHistoryActor;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.DialogsHistoryActor;
import im.actor.model.modules.messages.MessageDeleteActor;
import im.actor.model.modules.messages.OwnReadActor;
import im.actor.model.modules.messages.CursorReaderActor;
import im.actor.model.modules.messages.CursorReceiverActor;
import im.actor.model.modules.messages.SenderActor;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.RpcInternalException;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class Messages extends BaseModule {

    private ListEngine<Dialog> dialogs;
    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;
    private ActorRef ownReadActor;
    private ActorRef plainReadActor;
    private ActorRef plainReceiverActor;
    private ActorRef sendMessageActor;
    private ActorRef deletionsActor;

    private final HashMap<Peer, ListEngine<Message>> conversationEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ListEngine<Message>> conversationMediaEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ActorRef> conversationActors = new HashMap<Peer, ActorRef>();
    private final HashMap<Peer, ActorRef> conversationHistoryActors = new HashMap<Peer, ActorRef>();

    private final SyncKeyValue conversationPending;
    private final SyncKeyValue cursorStorage;

    public Messages(final Modules messenger) {
        super(messenger);

        this.conversationPending = new SyncKeyValue(storage().createKeyValue(STORAGE_PENDING));
        this.cursorStorage = new SyncKeyValue(storage().createKeyValue(STORAGE_CURSOR));
        this.dialogs = storage().createDialogsList(storage().createList(STORAGE_DIALOGS));
    }

    public void run() {
        this.dialogsActor = system().actorOf(Props.create(DialogsActor.class, new ActorCreator<DialogsActor>() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(modules());
            }
        }), "actor/dialogs");
        this.dialogsHistoryActor = system().actorOf(Props.create(DialogsHistoryActor.class, new ActorCreator<DialogsHistoryActor>() {
            @Override
            public DialogsHistoryActor create() {
                return new DialogsHistoryActor(modules());
            }
        }), "actor/dialogs/history");
        this.ownReadActor = system().actorOf(Props.create(OwnReadActor.class, new ActorCreator<OwnReadActor>() {
            @Override
            public OwnReadActor create() {
                return new OwnReadActor(modules());
            }
        }), "actor/read/own");
        this.plainReadActor = system().actorOf(Props.create(CursorReaderActor.class, new ActorCreator<CursorReaderActor>() {
            @Override
            public CursorReaderActor create() {
                return new CursorReaderActor(modules());
            }
        }), "actor/plain/read");
        this.plainReceiverActor = system().actorOf(Props.create(CursorReceiverActor.class, new ActorCreator<CursorReceiverActor>() {
            @Override
            public CursorReceiverActor create() {
                return new CursorReceiverActor(modules());
            }
        }), "actor/plain/receive");
        this.sendMessageActor = system().actorOf(Props.create(SenderActor.class, new ActorCreator<SenderActor>() {
            @Override
            public SenderActor create() {
                return new SenderActor(modules());
            }
        }), "actor/sender/small");
        this.deletionsActor = system().actorOf(Props.create(MessageDeleteActor.class, new ActorCreator<MessageDeleteActor>() {
            @Override
            public MessageDeleteActor create() {
                return new MessageDeleteActor(modules());
            }
        }), "actor/deletions");
    }

    public ActorRef getSendMessageActor() {
        return sendMessageActor;
    }

    public ActorRef getPlainReadActor() {
        return plainReadActor;
    }

    public ActorRef getPlainReceiverActor() {
        return plainReceiverActor;
    }

    public ActorRef getOwnReadActor() {
        return ownReadActor;
    }

    public SyncKeyValue getConversationPending() {
        return conversationPending;
    }

    public SyncKeyValue getCursorStorage() {
        return cursorStorage;
    }

    private void assumeConvActor(final Peer peer) {
        synchronized (conversationActors) {
            if (!conversationActors.containsKey(peer)) {
                conversationActors.put(peer, system().actorOf(Props.create(ConversationActor.class,
                        new ActorCreator<ConversationActor>() {
                            @Override
                            public ConversationActor create() {
                                return new ConversationActor(peer, modules());
                            }
                        }), "actor/conv_" + peer.getPeerType() + "_" + peer.getPeerId()));
                conversationHistoryActors.put(peer, system().actorOf(Props.create(ConversationHistoryActor.class, new ActorCreator<ConversationHistoryActor>() {
                    @Override
                    public ConversationHistoryActor create() {
                        return new ConversationHistoryActor(peer, modules());
                    }
                }), "actor/conv_" + peer.getPeerType() + "_" + peer.getPeerId() + "/history"));
            }
        }
    }

    public ActorRef getConversationHistoryActor(final Peer peer) {
        assumeConvActor(peer);
        synchronized (conversationActors) {
            return conversationHistoryActors.get(peer);
        }
    }

    public ActorRef getConversationActor(final Peer peer) {
        assumeConvActor(peer);
        synchronized (conversationActors) {
            return conversationActors.get(peer);
        }
    }

    public void onConversationOpen(Peer peer) {
        assumeConvActor(peer);
    }

    public ListEngine<Message> getConversationEngine(Peer peer) {
        synchronized (conversationEngines) {
            if (!conversationEngines.containsKey(peer)) {
                ListStorage storage = storage().createList(STORAGE_CHAT_PREFIX + peer.getUnuqueId());
                conversationEngines.put(peer, storage().createMessagesList(peer, storage));
            }
            return conversationEngines.get(peer);
        }
    }

    public ListEngine<Message> getMediaEngine(Peer peer) {
        synchronized (conversationMediaEngines) {
            if (!conversationMediaEngines.containsKey(peer)) {
                ListStorage storage = storage().createList(STORAGE_CHAT_MEDIA_PREFIX + peer.getUnuqueId());
                conversationMediaEngines.put(peer, storage().createMessagesList(peer, storage));
            }
            return conversationMediaEngines.get(peer);
        }
    }

    public ActorRef getDialogsActor() {
        return dialogsActor;
    }

    public ActorRef getDialogsHistoryActor() {
        return dialogsHistoryActor;
    }

    public ListEngine<Dialog> getDialogsEngine() {
        return dialogs;
    }

    public void deleteMessages(Peer peer, long[] rids) {
        ActorRef conversationActor = getConversationActor(peer);
        ArrayList<Long> deleted = new ArrayList<Long>();
        for (long rid : rids) {
            deleted.add(rid);
        }
        conversationActor.send(new ConversationActor.MessagesDeleted(deleted));
        deletionsActor.send(new MessageDeleteActor.DeleteMessage(peer, rids));
    }

    public void loadMoreDialogs() {
        dialogsHistoryActor.send(new DialogsHistoryActor.LoadMore());
    }

    public void loadMoreHistory(Peer peer) {
        getConversationHistoryActor(peer).send(new ConversationHistoryActor.LoadMore());
    }

    public void sendMessage(final Peer peer, final String message) {
        sendMessageActor.send(new SenderActor.SendText(peer, message));
    }

    public void sendPhoto(Peer peer, String fileName, int w, int h, FastThumb fastThumb,
                          String descriptor) {
        FileSystemReference reference =
                modules().getConfiguration().getFileSystemProvider().fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendPhoto(peer, fastThumb,
                descriptor,
                fileName, reference.getSize(), w, h));
    }

    public void sendVideo(Peer peer, String fileName, int w, int h, int duration,
                          FastThumb fastThumb, String descriptor) {
        FileSystemReference reference =
                modules().getConfiguration().getFileSystemProvider().fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendVideo(peer, fileName, w, h, duration,
                fastThumb, descriptor, reference.getSize()));
    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FastThumb fastThumb,
                             String descriptor) {
        FileSystemReference reference =
                modules().getConfiguration().getFileSystemProvider().fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendDocument(peer, fileName, mimeType,
                reference.getSize(), reference.getDescriptor(), fastThumb));
    }

    public void onInMessageShown(Peer peer, long sortDate) {
        ownReadActor.send(new OwnReadActor.MessageRead(peer, sortDate));
    }

    public void saveReadState(Peer peer, long lastReadDate) {
        preferences().putLong("read_state_" + peer.getUnuqueId(), lastReadDate);
    }

    public long loadReadState(Peer peer) {
        return preferences().getLong("read_state_" + peer.getUnuqueId(), 0);
    }

    public void saveDraft(Peer peer, String draft) {
        preferences().putString("draft_" + peer.getUnuqueId(), draft);
    }

    public String loadDraft(Peer peer) {
        String res = preferences().getString("draft_" + peer.getUnuqueId());
        if (res == null) {
            return "";
        } else {
            return res;
        }
    }

    public Command<Boolean> deleteChat(final Peer peer) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                OutPeer outPeer;
                final im.actor.model.api.Peer apiPeer;
                if (peer.getPeerType() == PeerType.PRIVATE) {
                    User user = users().getValue(peer.getPeerId());
                    if (user == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                        return;
                    }
                    outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(),
                            user.getAccessHash());
                    apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.PRIVATE,
                            user.getUid());
                } else if (peer.getPeerType() == PeerType.GROUP) {
                    Group group = groups().getValue(peer.getPeerId());
                    if (group == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                        return;
                    }
                    outPeer = new OutPeer(im.actor.model.api.PeerType.GROUP, group.getGroupId(),
                            group.getAccessHash());
                    apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.GROUP,
                            group.getGroupId());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestDeleteChat(outPeer), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                                response.getState(),
                                UpdateChatDelete.HEADER,
                                new UpdateChatDelete(apiPeer).toByteArray()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    public Command<Boolean> clearChat(final Peer peer) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                OutPeer outPeer;
                final im.actor.model.api.Peer apiPeer;
                if (peer.getPeerType() == PeerType.PRIVATE) {
                    User user = users().getValue(peer.getPeerId());
                    if (user == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                        return;
                    }
                    outPeer = new OutPeer(im.actor.model.api.PeerType.PRIVATE, user.getUid(),
                            user.getAccessHash());
                    apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.PRIVATE,
                            user.getUid());
                } else if (peer.getPeerType() == PeerType.GROUP) {
                    Group group = groups().getValue(peer.getPeerId());
                    if (group == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(new RpcInternalException());
                            }
                        });
                        return;
                    }
                    outPeer = new OutPeer(im.actor.model.api.PeerType.GROUP, group.getGroupId(),
                            group.getAccessHash());
                    apiPeer = new im.actor.model.api.Peer(im.actor.model.api.PeerType.GROUP,
                            group.getGroupId());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestClearChat(outPeer), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {
                        updates().onUpdateReceived(new SeqUpdate(response.getSeq(),
                                response.getState(),
                                UpdateChatClear.HEADER,
                                new UpdateChatClear(apiPeer).toByteArray()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResult(true);
                            }
                        });
                    }

                    @Override
                    public void onError(final RpcException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(e);
                            }
                        });
                    }
                });
            }
        };
    }

    private class ConversationHolder {
        private ActorRef conversationActor;
        private ActorRef historyActor;

        private ConversationHolder(ActorRef conversationActor, ActorRef historyActor) {
            this.conversationActor = conversationActor;
            this.historyActor = historyActor;
        }

        public ActorRef getConversationActor() {
            return conversationActor;
        }

        public ActorRef getHistoryActor() {
            return historyActor;
        }
    }
}