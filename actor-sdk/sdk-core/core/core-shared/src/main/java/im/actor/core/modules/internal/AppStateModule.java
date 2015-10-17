/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal;

import im.actor.core.api.ApiAppCounters;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.state.ListsStatesActor;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

import static im.actor.runtime.actors.ActorSystem.system;

public class AppStateModule extends AbsModule {
    private AppStateVM appStateVM;
    private ActorRef listStatesActor;

    public AppStateModule(ModuleContext context) {
        super(context);
        this.appStateVM = new AppStateVM(context);
    }

    public void run() {
        listStatesActor = system().actorOf(Props.create(ListsStatesActor.class, new ActorCreator<ListsStatesActor>() {
            @Override
            public ListsStatesActor create() {
                return new ListsStatesActor(context());
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

    public void onCountersChanged(ApiAppCounters counters) {
        listStatesActor.send(new ListsStatesActor.OnAppCounterChanged(counters));
    }

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }

    public void resetModule() {
        // TODO: Implement
    }
}
