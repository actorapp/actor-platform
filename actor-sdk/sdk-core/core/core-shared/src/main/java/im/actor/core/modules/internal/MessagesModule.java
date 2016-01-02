/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.ApiGroup;
import im.actor.core.api.ApiJsonMessage;
import im.actor.core.api.ApiOutPeer;
import im.actor.core.api.ApiPeer;
import im.actor.core.api.ApiPeerType;
import im.actor.core.api.base.FatSeqUpdate;
import im.actor.core.api.base.SeqUpdate;
import im.actor.core.api.rpc.RequestClearChat;
import im.actor.core.api.rpc.RequestDeleteChat;
import im.actor.core.api.rpc.RequestFavouriteDialog;
import im.actor.core.api.rpc.RequestHideDialog;
import im.actor.core.api.rpc.RequestMessageRemoveReaction;
import im.actor.core.api.rpc.RequestMessageSetReaction;
import im.actor.core.api.rpc.RequestUnfavouriteDialog;
import im.actor.core.api.rpc.ResponseDialogsOrder;
import im.actor.core.api.rpc.ResponseReactionsResponse;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.api.updates.UpdateChatClear;
import im.actor.core.api.updates.UpdateChatDelete;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
import im.actor.core.api.updates.UpdateContactsAdded;
import im.actor.core.api.updates.UpdateMessageContentChanged;
import im.actor.core.api.updates.UpdateReactionsUpdate;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.DialogSpec;
import im.actor.core.entity.Group;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.internal.messages.ConversationActor;
import im.actor.core.modules.internal.messages.ConversationHistoryActor;
import im.actor.core.modules.internal.messages.CursorReaderActor;
import im.actor.core.modules.internal.messages.CursorReceiverActor;
import im.actor.core.modules.internal.messages.DialogsActor;
import im.actor.core.modules.internal.messages.DialogsHistoryActor;
import im.actor.core.modules.internal.messages.GroupedDialogsActor;
import im.actor.core.modules.internal.messages.MessageDeleteActor;
import im.actor.core.modules.internal.messages.MessageShownActor;
import im.actor.core.modules.internal.messages.MessageShownFilter;
import im.actor.core.modules.internal.messages.OwnReadActor;
import im.actor.core.modules.internal.messages.SenderActor;
import im.actor.core.modules.internal.messages.entity.MessageShownEvent;
import im.actor.core.modules.updates.internal.ChangeContent;
import im.actor.core.modules.updates.internal.InternalUpdate;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.network.RpcInternalException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.DialogGroupsVM;
import im.actor.core.viewmodel.DialogSpecVM;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.tools.BounceFilterActor;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.json.JSONObject;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.SyncKeyValue;

import static im.actor.runtime.actors.ActorSystem.system;

public class MessagesModule extends AbsModule implements BusSubscriber {

    private ListEngine<Dialog> dialogs;

    private ActorRef dialogsActor;
    private ActorRef dialogsHistoryActor;
    private ActorRef dialogsGroupedActor;
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

    private final MVVMCollection<DialogSpec, DialogSpecVM> dialogDescKeyValue;

    private final DialogGroupsVM dialogGroups = new DialogGroupsVM();

    public MessagesModule(final ModuleContext context) {
        super(context);

        this.dialogDescKeyValue = Storage.createKeyValue(STORAGE_DIALOGS_DESC, DialogSpecVM.CREATOR, DialogSpec.CREATOR);
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

        this.dialogsGroupedActor = system().actorOf(Props.create(GroupedDialogsActor.class, new ActorCreator<GroupedDialogsActor>() {
            @Override
            public GroupedDialogsActor create() {
                return new GroupedDialogsActor(context());
            }
        }), "actor/dialogs/grouped");

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

    public DialogGroupsVM getDialogGroupsVM() {
        return dialogGroups;
    }

    public MVVMCollection<DialogSpec, DialogSpecVM> getDialogDescKeyValue() {
        return dialogDescKeyValue;
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

    public ActorRef getDialogsGroupedActor() {
        return dialogsGroupedActor;
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
        // Notify typing about message sent
        context().getTypingModule().onMessageSent(peer);
        sendMessageActor.send(new SenderActor.SendText(peer, message, markDownText, mentions,
                autoDetect));
    }

    public void sendContact(Peer peer, String name, HashSet<String> phones, HashSet<String> emails, String base64photo) {
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

    public void updateJson(Peer peer, long rid, JsonContent json) {
        ApiPeer apiPeer = new ApiPeer(peer.getPeerType() == PeerType.PRIVATE ? ApiPeerType.PRIVATE : ApiPeerType.GROUP, peer.getPeerId());
        ApiJsonMessage jsonMessage = new ApiJsonMessage(json.getRawJson());
        updates().onUpdateReceived(new ChangeContent(new UpdateMessageContentChanged(apiPeer, rid, jsonMessage)));

    }

    public void sendDocument(Peer peer, String fileName, String mimeType, FastThumb fastThumb,
                             String descriptor) {
        FileSystemReference reference = Storage.fileFromDescriptor(descriptor);
        sendMessageActor.send(new SenderActor.SendDocument(peer, fileName, mimeType,
                reference.getSize(), reference.getDescriptor(), fastThumb));
    }

    public void sendSticker(@NotNull Peer peer,
                            @NotNull Sticker sticker) {
        sendMessageActor.send(new SenderActor.SendSticker(peer, sticker));
    }

    public void onMessageShown(final Peer peer, final int sender, final long sortDate) {
        if (!context().getAppStateModule().getAppStateVM().getIsAppVisible().get()) {
            return;
        }
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

    public Command<Boolean> hideChat(final Peer peer) {
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
                request(new RequestHideDialog(outPeer), new RpcCallback<ResponseDialogsOrder>() {
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