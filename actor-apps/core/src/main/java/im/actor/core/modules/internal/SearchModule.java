/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import java.util.List;

import im.actor.core.entity.Dialog;
import im.actor.core.entity.SearchEntity;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.modules.internal.search.SearchActor;
import im.actor.runtime.Storage;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.storage.ListEngine;

import static im.actor.runtime.actors.ActorSystem.system;

public class SearchModule extends AbsModule {

    private ListEngine<SearchEntity> searchList;
    private ActorRef actorRef;

    public SearchModule(Modules modules) {
        super(modules);

        searchList = Storage.createList(STORAGE_SEARCH, SearchEntity.CREATOR);
    }

    public void run() {
        actorRef = system().actorOf(Props.create(SearchActor.class, new ActorCreator<SearchActor>() {
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

    public void resetModule() {
        actorRef.send(new SearchActor.Clear());
    }
}
