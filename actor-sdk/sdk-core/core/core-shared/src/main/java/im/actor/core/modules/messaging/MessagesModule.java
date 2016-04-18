/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.api.ApiMessage;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.ApiTextMessage;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestArchiveChat;
import im.actor.core.api.rpc.RequestClearChat;
import im.actor.core.api.rpc.RequestDeleteChat;
import im.actor.core.api.rpc.RequestFavouriteDialog;
import im.actor.core.api.rpc.RequestMessageRemoveReaction;
import im.actor.core.api.rpc.RequestMessageSetReaction;
import im.actor.core.api.rpc.RequestUnfavouriteDialog;
import im.actor.core.api.rpc.RequestUpdateMessage;
import im.actor.core.api.rpc.ResponseDialogsOrder;
import im.actor.core.api.rpc.ResponseLoadArchived;
import im.actor.core.api.rpc.ResponseReactionsResponse;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.rpc.ResponseSeqDate;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateReactionsUpdate;
import im.actor.core.entity.ConversationState;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.Sticker;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.events.PeerChatPreload;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.messaging.history.ArchivedDialogsActor;
import im.actor.core.modules.messaging.actions.CursorReaderActor;
import im.actor.core.modules.messaging.actions.CursorReceiverActor;
import im.actor.core.modules.messaging.dialogs.DialogsActor;
import im.actor.core.modules.messaging.history.ConversationHistoryActor;
import im.actor.core.modules.messaging.history.DialogsHistoryActor;
import im.actor.core.modules.messaging.actions.MessageDeleteActor;
import im.actor.core.modules.messaging.actions.SenderActor;
import im.actor.core.modules.messaging.router.RouterInt;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.core.viewmodel.DialogGroupsVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;


public class MessagesModule extends AbsModule implements BusSubscriber {

    private static final String DIALOGS_KEY_VERSION = "_1";

    private ListEngine<Dialog> dialogs;

    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;
    private ActorRef archivedDialogsActor;
    private ActorRef plainReadActor;
    private ActorRef plainReceiverActor;
    private ActorRef sendMessageActor;
    private ActorRef deletionsActor;
    private RouterInt router;
    private final HashMap<Peer, ActorRef> historyLoaderActors = new HashMap<>();

    private MVVMCollection<ConversationState, ConversationVM> conversationStates;

    private final HashMap<Peer, ListEngine<Message>> conversationEngines = new HashMap<>();
    private final HashMap<Peer, ListEngine<Message>> conversationDocsEngines = new HashMap<>();

    private final SyncKeyValue cursorStorage;

    private final DialogGroupsVM dialogGroups = new DialogGroupsVM();

    public MessagesModule(final ModuleContext context) {
        super(context);
        this.conversationStates = Storage.createKeyValue(STORAGE_CHAT_STATES,
                ConversationVM.CREATOR,
                ConversationState.CREATOR,
                ConversationState.DEFAULT_CREATOR);
        this.cursorStorage = new SyncKeyValue(Storage.createKeyValue(STORAGE_CURSOR));
        this.dialogs = Storage.createList(STORAGE_DIALOGS + DIALOGS_KEY_VERSION, Dialog.CREATOR);
    }

    public void run() {

        this.router = new RouterInt(context());

        this.dialogsActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(context());
            }
        }), "actor/dialogs");
        this.dialogsHistoryActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public DialogsHistoryActor create() {
                return new DialogsHistoryActor(context());
            }
        }), "actor/dialogs/history");
        this.archivedDialogsActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public ArchivedDialogsActor create() {
                return new ArchivedDialogsActor(context());
            }
        }), "actor/dialogs/archived");

        this.plainReadActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public CursorReaderActor create() {
                return new CursorReaderActor(context());
            }
        }).changeDispatcher("heavy"), "actor/plain/read");
        this.plainReceiverActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public CursorReceiverActor create() {
                return new CursorReceiverActor(context());
            }
        }).changeDispatcher("heavy"), "actor/plain/receive");
        this.sendMessageActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public SenderActor create() {
                return new SenderActor(context());
            }
        }), "actor/sender/small");
        this.deletionsActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public MessageDeleteActor create() {
                return new MessageDeleteActor(context());
            }
        }), "actor/deletions");

        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
        context().getEvents().subscribe(this, PeerChatPreload.EVENT);
    }

    public DialogGroupsVM getDialogGroupsVM() {
        return dialogGroups;
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

    public ActorRef getHistoryActor(final Peer peer) {
        synchronized (historyLoaderActors) {
            if (!historyLoaderActors.containsKey(peer)) {
                historyLoaderActors.put(peer, system().actorOf("history/" + peer, new ActorCreator() {
                    @Override
                    public Actor create() {
                        return new ConversationHistoryActor(peer, context());
                    }
                }));
            }
            return historyLoaderActors.get(peer);
        }
    }

    public SyncKeyValue getCursorStorage() {
        return cursorStorage;
    }

    public RouterInt getRouter() {
        return router;
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

    public MVVMCollection<ConversationState, ConversationVM> getConversationStates() {
        return conversationStates;
    }

    public ConversationVM getConversationVM(Peer peer) {
        return conversationStates.get(peer.getUnuqueId());
    }

    public ActorRef getDialogsActor() {
        return dialogsActor;
    }

    public ListEngine<Dialog> getDialogsEngine() {
        return dialogs;
    }

    public void deleteMessages(Peer peer, long[] rids) {
        ArrayList<Long> deleted = new ArrayList<>();
        for (long rid : rids) {
            deleted.add(rid);
        }
        router.onMessagesDeleted(peer, deleted);
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

    public void loadMoreArchivedDialogs(final boolean init, final RpcCallback<ResponseLoadArchived> callback) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                archivedDialogsActor.send(new ArchivedDialogsActor.LoadMore(init, callback));
            }
        });
    }

    public void loadMoreHistory(final Peer peer) {
        im.actor.runtime.Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                getHistoryActor(peer).send(new ConversationHistoryActor.LoadMore());
            }
        });
    }


    public Command<ResponseSeqDate> updateMessage(final Peer peer, final String message, final long rid) {
        context().getTypingModule().onMessageSent(peer);
        return new Command<ResponseSeqDate>() {

            @Override
            public void start(final CommandCallback<ResponseSeqDate> callback) {
                ArrayList<Integer> mentions = new ArrayList<Integer>();
                TextContent content = TextContent.create(message, null, mentions);

                if (peer.getPeerType() == PeerType.GROUP) {
                    Group group = groups().getValue(peer.getPeerId());
                    String lowText = message.toLowerCase();
                    for (GroupMember member : group.getMembers()) {
                        User user = users().getValue(member.getUid());
                        if (user.getNick() != null) {
                            String nick = "@" + user.getNick().toLowerCase();
                            // TODO: Better filtering
                            if (lowText.contains(nick + ":")
                                    || lowText.contains(nick + " ")
                                    || lowText.contains(" " + nick)
                                    || lowText.endsWith(nick)
                                    || lowText.equals(nick)) {
                                mentions.add(user.getUid());
                            }
                        }
                    }
                }

                ApiMessage editMessage = new ApiTextMessage(message, ((TextContent) content).getMentions(), ((TextContent) content).getTextMessageEx());
                request(new RequestUpdateMessage(buidOutPeer(peer), rid, editMessage), new RpcCallback<ResponseSeqDate>() {
                    @Override
                    public void onResult(ResponseSeqDate response) {
                        callback.onResult(response);
                    }

                    @Override
                    public void onError(RpcException e) {
                        callback.onError(e);
                    }
                });
            }
        };

    }

    public ApiOutPeer buidOutPeer(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            User user = users().getValue(peer.getPeerId());
            if (user == null) {
                return null;
            }
            return new ApiOutPeer(ApiPeerType.PRIVATE, user.getUid(), user.getAccessHash());
        } else if (peer.getPeerType() == PeerType.GROUP) {
            Group group = groups().getValue(peer.getPeerId());
            if (group == null) {
                return null;
            }
            return new ApiOutPeer(ApiPeerType.GROUP, group.getGroupId(), group.getAccessHash());
        } else {
            throw new RuntimeException("Unknown peer: " + peer);
        }
    }


    public void sendMessage(@NotNull Peer peer, @NotNull String message, @Nullable String markDownText,
                            @Nullable ArrayList<Integer> mentions, boolean autoDetect) {
        // Notify typing about message sent
        context().getTypingModule().onMessageSent(peer);
        sendMessageActor.send(new SenderActor.SendText(peer, message, markDownText, mentions,
                autoDetect));
    }

    public void sendContact(Peer peer, String name, ArrayList<String> phones, ArrayList<String> emails, String base64photo) {
        sendMessageActor.send(new SenderActor.SendContact(peer, phones, emails, name, base64photo));
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

    public void sendAudio(@NotNull Peer peer, @NotNull String fileName, int duration,
                          @NotNull String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendAudio(peer,
                descriptor,
                fileName, reference.getSize(), duration));
    }

    public void sendLoacation(@NotNull Peer peer,
                              @NotNull Double longitude, @NotNull Double latitude,
                              @Nullable String street, @Nullable String place) {
        sendMessageActor.send(new SenderActor.SendLocation(peer, longitude, latitude, street, place));
    }

    public void sendJson(@NotNull Peer peer,
                         @NotNull JsonContent content) {
        sendMessageActor.send(new SenderActor.SendJson(peer, content));
    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FastThumb fastThumb,
                             String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendDocument(peer, fileName, mimeType,
                reference.getSize(), reference.getDescriptor(), fastThumb));
    }

    public void forwardContent(Peer peer, AbsContent content) {
        sendMessageActor.send(new SenderActor.ForwardContent(peer, content));
    }

    public void sendSticker(@NotNull Peer peer,
                            @NotNull Sticker sticker) {
        sendMessageActor.send(new SenderActor.SendSticker(peer, sticker));
    }

    public void saveDraft(Peer peer, String draft) {
        context().getSettingsModule().setStringValue("drafts_" + peer.getUnuqueId(), draft);
    }

    public String loadDraft(Peer peer) {
        String res = context().getSettingsModule().getStringValue("drafts_" + peer.getUnuqueId(), null);
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
                final ApiPeer apiPeer = buildApiPeer(peer);
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
                }, RPC_TIMEOUT);
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
                }, RPC_TIMEOUT);
            }
        };
    }

    public Command<Boolean> addReaction(final Peer peer, final long rid, final String reaction) {
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

                request(new RequestMessageSetReaction(outPeer, rid, reaction), new RpcCallback<ResponseReactionsResponse>() {
                    @Override
                    public void onResult(ResponseReactionsResponse response) {
                        updates().onSeqUpdateReceived(response.getSeq(),
                                response.getState(),
                                new UpdateReactionsUpdate(apiPeer, rid, response.getReactions()));

                        updates().executeAfter(response.getSeq(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(true);
                                            }
                                        });
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

    public Command<Boolean> removeReaction(final Peer peer, final long rid, final String reaction) {
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

                request(new RequestMessageRemoveReaction(outPeer, rid, reaction), new RpcCallback<ResponseReactionsResponse>() {
                    @Override
                    public void onResult(ResponseReactionsResponse response) {
                        updates().onSeqUpdateReceived(response.getSeq(),
                                response.getState(),
                                new UpdateReactionsUpdate(apiPeer, rid, response.getReactions()));

                        updates().executeAfter(response.getSeq(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(true);
                                            }
                                        });
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

    public Command<Boolean> favoriteChat(final Peer peer) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                ApiOutPeer outPeer;
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
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestFavouriteDialog(outPeer), new RpcCallback<ResponseDialogsOrder>() {
                    @Override
                    public void onResult(ResponseDialogsOrder response) {
                        updates().onSeqUpdateReceived(response.getSeq(),
                                response.getState(),
                                new UpdateChatGroupsChanged(response.getGroups()));

                        updates().executeAfter(response.getSeq(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(true);
                                            }
                                        });
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

    public Command<Boolean> unfavoriteChat(final Peer peer) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                ApiOutPeer outPeer;
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
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestUnfavouriteDialog(outPeer), new RpcCallback<ResponseDialogsOrder>() {
                    @Override
                    public void onResult(ResponseDialogsOrder response) {
                        updates().onSeqUpdateReceived(response.getSeq(),
                                response.getState(),
                                new UpdateChatGroupsChanged(response.getGroups()));

                        updates().executeAfter(response.getSeq(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(true);
                                            }
                                        });
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


    public Command<Boolean> archiveChat(final Peer peer) {
        return new Command<Boolean>() {
            @Override
            public void start(final CommandCallback<Boolean> callback) {
                final ApiOutPeer outPeer;
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
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(new RpcInternalException());
                        }
                    });
                    return;
                }
                request(new RequestArchiveChat(outPeer), new RpcCallback<ResponseSeq>() {
                    @Override
                    public void onResult(ResponseSeq response) {

                        updates().executeAfter(response.getSeq(),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(true);
                                            }
                                        });
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
            getHistoryActor(((PeerChatOpened) event).getPeer());
        } else if (event instanceof PeerChatPreload) {
            getHistoryActor(((PeerChatPreload) event).getPeer());
        }
    }
}