/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules;

import im.actor.core.api.AppCounters;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.core.modules.state.ListsStatesActor;
import im.actor.core.viewmodel.AppStateVM;

import static im.actor.runtime.actors.ActorSystem.system;

public class AppStateModule extends BaseModule {
    private AppStateVM appStateVM;
    private ActorRef listStatesActor;

    public AppStateModule(Modules modules) {
        super(modules);
        this.appStateVM = new AppStateVM(modules);
    }

    public void run() {
        listStatesActor = system().actorOf(Props.create(ListsStatesActor.class, new ActorCreator<ListsStatesActor>() {
            @Override
            public ListsStatesActor create() {
                return new ListsStatesActor(modules());
            }
        }), "actor/app/state");
    }

    public void onDialogsUpdate(boolean isEmpty) {
        listStatesActor.send(new ListsStatesActor.OnDialogsChanged(isEmpty));
    }

    public void onContactsUpdate(boolean isEmpty) {
        listStatesActor.send(new ListsStatesActor.OnContactsChanged(isEmpty));
    }

    public void onBookImported() {
        listStatesActor.send(new ListsStatesActor.OnBookImported());
    }

    public void onContactsLoaded() {
        listStatesActor.send(new ListsStatesActor.OnContactsLoaded());
    }

    public void onDialogsLoaded() {
        listStatesActor.send(new ListsStatesActor.OnDialogsLoaded());
    }

    public void onCountersChanged(AppCounters counters) {
        listStatesActor.send(new ListsStatesActor.OnAppCounterChanged(counters));
    }

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }

    public void resetModule() {
        // TODO: Implement
    }
}
