/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestClearChat;
import im.actor.core.api.rpc.RequestDeleteChat;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.internal.messages.ConversationActor;
import im.actor.core.modules.internal.messages.ConversationHistoryActor;
import im.actor.core.modules.internal.messages.CursorReaderActor;
import im.actor.core.modules.internal.messages.CursorReceiverActor;
import im.actor.core.modules.internal.messages.DialogsActor;
import im.actor.core.modules.internal.messages.DialogsHistoryActor;
import im.actor.core.modules.internal.messages.MessageDeleteActor;
import im.actor.core.modules.internal.messages.MessageShownActor;
import im.actor.core.modules.internal.messages.MessageShownFilter;
import im.actor.core.modules.internal.messages.OwnReadActor;
import im.actor.core.modules.internal.messages.SenderActor;
import im.actor.core.modules.internal.messages.entity.MessageShownEvent;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.tools.BounceFilterActor;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;

public class MessagesModule extends AbsModule implements BusSubscriber {

    private ListEngine<Dialog> dialogs;
    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;
    private ActorRef ownReadActor;
    private ActorRef plainReadActor;
    private ActorRef plainReceiverActor;
    private ActorRef sendMessageActor;
    private ActorRef deletionsActor;
    private ActorRef messageShownActor;

    private final HashMap<Peer, ListEngine<Message>> conversationEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ListEngine<Message>> conversationDocsEngines = new HashMap<Peer, ListEngine<Message>>();
    private final HashMap<Peer, ActorRef> conversationActors = new HashMap<Peer, ActorRef>();
    private final HashMap<Peer, ActorRef> conversationHistoryActors = new HashMap<Peer, ActorRef>();
    private final HashMap<Peer, ActorRef> messageShownFilter = new HashMap<Peer, ActorRef>();

    private final SyncKeyValue cursorStorage;

    public MessagesModule(final ModuleContext context) {
        super(context);

        this.cursorStorage = new SyncKeyValue(Storage.createKeyValue(STORAGE_CURSOR));
        this.dialogs = Storage.createList(STORAGE_DIALOGS, Dialog.CREATOR);
    }

    public void run() {
        this.dialogsActor = system().actorOf(Props.create(DialogsActor.class, new ActorCreator<DialogsActor>() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(context());
            }
        }), "actor/dialogs");
        this.dialogsHistoryActor = system().actorOf(Props.create(DialogsHistoryActor.class, new ActorCreator<DialogsHistoryActor>() {
            @Override
            public DialogsHistoryActor create() {
                return new DialogsHistoryActor(context());
            }
        }), "actor/dialogs/history");
        this.ownReadActor = system().actorOf(Props.create(OwnReadActor.class, new ActorCreator<OwnReadActor>() {
            @Override
            public OwnReadActor create() {
                return new OwnReadActor(context());
            }
        }), "actor/read/own");
        this.plainReadActor = system().actorOf(Props.create(CursorReaderActor.class, new ActorCreator<CursorReaderActor>() {
            @Override
            public CursorReaderActor create() {
                return new CursorReaderActor(context());
            }
        }).changeDispatcher("heavy"), "actor/plain/read");
        this.plainReceiverActor = system().actorOf(Props.create(CursorReceiverActor.class, new ActorCreator<CursorReceiverActor>() {
            @Override
            public CursorReceiverActor create() {
                return new CursorReceiverActor(context());
            }
        }).changeDispatcher("heavy"), "actor/plain/receive");
        this.sendMessageActor = system().actorOf(Props.create(SenderActor.class, new ActorCreator<SenderActor>() {
            @Override
            public SenderActor create() {
                return new SenderActor(context());
            }
        }), "actor/sender/small");
        this.deletionsActor = system().actorOf(Props.create(MessageDeleteActor.class, new ActorCreator<MessageDeleteActor>() {
            @Override
            public MessageDeleteActor create() {
                return new MessageDeleteActor(context());
            }
        }), "actor/deletions");
        this.messageShownActor = system().actorOf(Props.create(MessageShownActor.class, new ActorCreator<MessageShownActor>() {
            @Override
            public MessageShownActor create() {
                return new MessageShownActor(context());
            }
        }), "actor/shown");

        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
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
                                return new ConversationActor(peer, context());
                            }
                        }), "actor/conv_" + peer.getPeerType() + "_" + peer.getPeerId()));
                conversationHistoryActors.put(peer, system().actorOf(Props.create(ConversationHistoryActor.class, new ActorCreator<ConversationHistoryActor>() {
                    @Override
                    public ConversationHistoryActor create() {
                        return new ConversationHistoryActor(peer, context());
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

    public ListEngine<Message> getConversationEngine(Peer peer) {
        synchronized (conversationEngines) {
            if (!conversationEngines.containsKey(peer)) {
                conversationEngines.put(peer,
                        Storage.createList(STORAGE_CHAT_PREFIX + peer.getUnuqueId(), Message.CREATOR));
            }
            return conversationEngines.get(peer);
        }
    }

    public ListEngine<Message> getConversationDocsEngine(Peer peer) {
        synchronized (conversationDocsEngines) {
            if (!conversationDocsEngines.containsKey(peer)) {
                conversationDocsEngines.put(peer,
                        Storage.createList(STORAGE_CHAT_DOCS_PREFIX + peer.getUnuqueId(), Message.CREATOR));
            }
            return conversationDocsEngines.get(peer);
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
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                dialogsHistoryActor.send(new DialogsHistoryActor.LoadMore());
            }
        });
    }

    public void loadMoreHistory(final Peer peer) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                getConversationHistoryActor(peer).send(new ConversationHistoryActor.LoadMore());
            }
        });
    }

    public void sendMessage(@NotNull Peer peer, @NotNull String message, @Nullable String markDownText,
                            @Nullable ArrayList<Integer> mentions, boolean autoDetect) {
        sendMessageActor.send(new SenderActor.SendText(peer, message, markDownText, mentions,
                autoDetect));
    }

    public void sendPhoto(@NotNull Peer peer, @NotNull String fileName, int w, int h, @Nullable FastThumb fastThumb,
                          @NotNull String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendPhoto(peer, fastThumb,
                descriptor,
                fileName, reference.getSize(), w, h));
    }

    public void sendVideo(Peer peer, String fileName, int w, int h, int duration,
                          FastThumb fastThumb, String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendVideo(peer, fileName, w, h, duration,
                fastThumb, descriptor, reference.getSize()));
    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FastThumb fastThumb,
                             String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendDocument(peer, fileName, mimeType,
                reference.getSize(), reference.getDescriptor(), fastThumb));
    }

    public void onMessageShown(final Peer peer, final int sender, final long sortDate) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                if (sender != myUid()) {
                    if (!messageShownFilter.containsKey(peer)) {
                        messageShownFilter.put(peer, system().actorOf(Props.create(MessageShownFilter.class, new ActorCreator<MessageShownFilter>() {
                            @Override
                            public MessageShownFilter create() {
                                return new MessageShownFilter();
                            }
                        }), "actor/shown_filter_" + peer.getPeerType() + "_" + peer.getPeerId()));
                    }

                    messageShownFilter.get(peer).send(new BounceFilterActor.Message(new MessageShownEvent(peer, sortDate),
                            messageShownActor));
                }
            }
        });
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
                ApiOutPeer outPeer;
                final ApiPeer apiPeer;
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
                    outPeer = new ApiOutPeer(ApiPeerType.PRIVATE, user.getUid(),
                            user.getAccessHash());
                    apiPeer = new ApiPeer(ApiPeerType.PRIVATE,
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
                    outPeer = new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(),
                            group.getAccessHash());
                    apiPeer = new ApiPeer(ApiPeerType.GROUP,
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
                ApiOutPeer outPeer;
                final ApiPeer apiPeer;
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
                    outPeer = new ApiOutPeer(ApiPeerType.PRIVATE, user.getUid(),
                            user.getAccessHash());
                    apiPeer = new ApiPeer(ApiPeerType.PRIVATE,
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
                    outPeer = new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(),
                            group.getAccessHash());
                    apiPeer = new ApiPeer(ApiPeerType.GROUP,
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

    public void resetModule() {
        // TODO: Implement
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof PeerChatOpened) {
            assumeConvActor(((PeerChatOpened) event).getPeer());
        }
    }
}