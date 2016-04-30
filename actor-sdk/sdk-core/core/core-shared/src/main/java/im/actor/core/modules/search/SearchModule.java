/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.search;

import java.util.ArrayList;
import java.util.List;

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
import im.actor.core.entity.Dialog;
import im.actor.core.entity.MessageSearchEntity;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerSearchType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.api.ApiSupportConfiguration;
import im.actor.core.modules.Modules;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.storage.ListEngine;

import static im.actor.core.entity.EntityConverter.convert;
import static im.actor.runtime.actors.ActorSystem.system;

public class SearchModule extends AbsModule {

    private ListEngine<SearchEntity> searchList;
    private ActorRef actorRef;

    public SearchModule(Modules modules) {
        super(modules);

        searchList = Storage.createList(STORAGE_SEARCH, SearchEntity.CREATOR);
    }

    public void run() {
        actorRef = system().actorOf("actor/search", () -> new SearchActor(context()));
    }

    public ListEngine<SearchEntity> getSearchList() {
        return searchList;
    }


    //
    // Message Search
    //

    public Promise<List<MessageSearchEntity>> findTextMessages(Peer peer, String query) {
        ArrayList<ApiSearchCondition> conditions = new ArrayList<>();
        conditions.add(new ApiSearchPeerCondition(getApiOutPeer(peer)));
        conditions.add(new ApiSearchPieceText(query));
        return findMessages(new ApiSearchAndCondition(conditions));
    }

    public Promise<List<MessageSearchEntity>> findAllDocs(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.DOCUMENTS);
    }

    public Promise<List<MessageSearchEntity>> findAllLinks(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.LINKS);
    }

    public Promise<List<MessageSearchEntity>> findAllPhotos(Peer peer) {
        return findAllContent(peer, ApiSearchContentType.PHOTOS);
    }

    private Promise<List<MessageSearchEntity>> findAllContent(Peer peer, ApiSearchContentType contentType) {
        ArrayList<ApiSearchCondition> conditions = new ArrayList<>();
        conditions.add(new ApiSearchPeerCondition(getApiOutPeer(peer)));
        conditions.add(new ApiSearchPeerContentType(contentType));
        return findMessages(new ApiSearchAndCondition(conditions));
    }

    private Promise<List<MessageSearchEntity>> findMessages(final ApiSearchCondition condition) {
        return api(new RequestMessageSearch(condition, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(responseMessageSearchResponse ->
                        updates().applyRelatedData(
                                responseMessageSearchResponse.getUsers(),
                                responseMessageSearchResponse.getGroups()))
                .map(responseMessageSearchResponse1 ->
                        ManagedList.of(responseMessageSearchResponse1.getSearchResults())
                                .map(itm -> new MessageSearchEntity(
                                        convert(itm.getResult().getPeer()), itm.getResult().getRid(),
                                        itm.getResult().getDate(), itm.getResult().getSenderId(),
                                        AbsContent.fromMessage(itm.getResult().getContent()))));
    }

    public Promise<List<PeerSearchEntity>> findPeers(final PeerSearchType type) {
        final ApiSearchPeerType apiType;
        if (type == PeerSearchType.GROUPS) {
            apiType = ApiSearchPeerType.GROUPS;
        } else if (type == PeerSearchType.PUBLIC) {
            apiType = ApiSearchPeerType.PUBLIC;
        } else {
            apiType = ApiSearchPeerType.CONTACTS;
        }
        ArrayList<ApiSearchCondition> conditions = new ArrayList<>();
        conditions.add(new ApiSearchPeerTypeCondition(apiType));

        return api(new RequestPeerSearch(conditions, ApiSupportConfiguration.OPTIMIZATIONS))
                .chain(responsePeerSearch ->
                        updates().applyRelatedData(
                                responsePeerSearch.getUsers(),
                                responsePeerSearch.getGroups()))
                .map(responsePeerSearch1 ->
                        ManagedList.of(responsePeerSearch1.getSearchResults())
                                .map(r -> new PeerSearchEntity(convert(r.getPeer()), r.getTitle(),
                                        r.getDescription(), r.getMembersCount(), r.getDateCreated(),
                                        r.getCreator(), r.isPublic(), r.isJoined())));
    }


    //
    // Local Search
    //

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

    public void resetModule() {
        actorRef.send(new SearchActor.Clear());
    }
}
