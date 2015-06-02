/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules;

import java.util.List;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.SearchEntity;
import im.actor.model.modules.search.SearchActor;

import static im.actor.model.droidkit.actors.ActorSystem.system;

public class SearchModule extends BaseModule {

    private ListEngine<SearchEntity> searchList;
    private ActorRef actorRef;

    public SearchModule(Modules modules) {
        super(modules);

        searchList = storage().createSearchList(storage().createList(STORAGE_SEARCH));
    }

    public void run() {
        actorRef = system().actorOf(Props.create(SearchActor.class, new ActorCreator<SearchActor>() {
            @Override
            public SearchActor create() {
                return new SearchActor(modules());
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
