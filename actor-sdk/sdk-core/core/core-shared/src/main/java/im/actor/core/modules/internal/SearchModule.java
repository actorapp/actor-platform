/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiMessageSearchItem;
import im.actor.core.api.ApiMessageSearchResult;
import im.actor.core.api.ApiPeerSearchResult;
import im.actor.core.api.ApiSearchAndCondition;
import im.actor.core.api.ApiSearchCondition;
import im.actor.core.api.ApiSearchContentType;
import im.actor.core.api.ApiSearchPeerCondition;
import im.actor.core.api.ApiSearchPeerContentType;
import im.actor.core.api.ApiSearchPeerType;
import im.actor.core.api.ApiSearchPeerTypeCondition;
import im.actor.core.api.ApiSearchPieceText;
import im.actor.core.api.rpc.RequestMessageSearch;
import im.actor.core.api.rpc.RequestPeerSearch;
import im.actor.core.api.rpc.ResponseMessageSearchResponse;
import im.actor.core.api.rpc.ResponsePeerSearch;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.MessageSearchEntity;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerSearchType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.internal.search.SearchActor;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.core.viewmodel.Command;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.storage.ListEngine;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;
import static im.actor.runtime.actors.ActorSystem.system;

public class SearchModule extends AbsModule {

    private ListEngine<SearchEntity> searchList;
    private ActorRef actorRef;

    public SearchModule(Modules modules) {
        super(modules);

        searchList = Storage.createList(STORAGE_SEARCH, SearchEntity.CREATOR);
    }

    public void run() {
        actorRef = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public SearchActor create() {
                return new SearchActor(context());
            }
        }), "actor/search");
    }

    public ListEngine<SearchEntity> getSearchList() {
        return searchList;
    }

    public void onDialogsChanged(List<Dialog> dialogs) {
        actorRef.send(new SearchActor.OnDialogsUpdated(dialogs));
    }

    public void onContactsChanged(Integer[] contacts) {
        int[] res = new int[contacts.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = contacts[i];
        }
        actorRef.send(new SearchActor.OnContactsUpdated(res));
    }

    public Command<List<MessageSearchEntity>> findTextMessages(Peer peer, String query) {
        ArrayList<ApiSearchCondition> conditions = new ArrayList<ApiSearchCondition>();
        conditions.add(new ApiSearchPeerCondition(buildApiOutPeer(peer)));
        conditions.add(new ApiSearchPieceText(query));
        return findMessages(new ApiSearchAndCondition(conditions));
    }

    public Command<List<MessageSearchEntity>> findAllDocs(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.DOCUMENTS);
    }

    public Command<List<MessageSearchEntity>> findAllLinks(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.LINKS);
    }

    public Command<List<MessageSearchEntity>> findAllPhotos(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.PHOTOS);
    }

    private Command<List<MessageSearchEntity>> findAllContent(Peer peer, ApiSearchContentType contentType) {
        ArrayList<ApiSearchCondition> conditions = new ArrayList<ApiSearchCondition>();
        conditions.add(new ApiSearchPeerCondition(buildApiOutPeer(peer)));
        conditions.add(new ApiSearchPeerContentType(contentType));
        return findMessages(new ApiSearchAndCondition(conditions));
    }

    private Command<List<MessageSearchEntity>> findMessages(final ApiSearchCondition condition) {
        return new Command<List<MessageSearchEntity>>() {
            @Override
            public void start(final CommandCallback<List<MessageSearchEntity>> callback) {
                request(new RequestMessageSearch(condition), new RpcCallback<ResponseMessageSearchResponse>() {
                    @Override
                    public void onResult(final ResponseMessageSearchResponse response) {
                        updates().executeRelatedResponse(response.getUsers(), response.getGroups(), new Runnable() {
                            @Override
                            public void run() {
                                final ArrayList<MessageSearchEntity> res = new ArrayList<MessageSearchEntity>();
                                for (ApiMessageSearchItem r : response.getSearchResults()) {
                                    ApiMessageSearchResult itm = r.getResult();
                                    try {
                                        res.add(new MessageSearchEntity(
                                                convert(itm.getPeer()), itm.getRid(),
                                                itm.getDate(), itm.getSenderId(),
                                                AbsContent.fromMessage(itm.getContent())));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.onResult(res);
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

    public Command<List<PeerSearchEntity>> findPeers(final PeerSearchType type) {
        final ApiSearchPeerType apiType;
        if (type == PeerSearchType.GROUPS) {
            apiType = ApiSearchPeerType.GROUPS;
        } else if (type == PeerSearchType.PUBLIC) {
            apiType = ApiSearchPeerType.PUBLIC;
        } else {
            apiType = ApiSearchPeerType.CONTACTS;
        }
        return new Command<List<PeerSearchEntity>>() {
            @Override
            public void start(final CommandCallback<List<PeerSearchEntity>> callback) {
                ArrayList<ApiSearchCondition> conditions = new ArrayList<ApiSearchCondition>();
                conditions.add(new ApiSearchPeerTypeCondition(apiType));
                request(new RequestPeerSearch(conditions), new RpcCallback<ResponsePeerSearch>() {
                    @Override
                    public void onResult(final ResponsePeerSearch response) {
                        updates().executeRelatedResponse(response.getUsers(),
                                response.getGroups(), new Runnable() {
                                    @Override
                                    public void run() {
                                        final ArrayList<PeerSearchEntity> res = new ArrayList<PeerSearchEntity>();
                                        for (ApiPeerSearchResult r : response.getSearchResults()) {
                                            res.add(new PeerSearchEntity(convert(r.getPeer()), r.getTitle(),
                                                    r.getDescription(), r.getMembersCount(), r.getDateCreated(),
                                                    r.getCreator(), r.isPublic(), r.isJoined()));
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onResult(res);
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
        actorRef.send(new SearchActor.Clear());
    }
}
