/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.misc;

import im.actor.core.api.ApiAppCounters;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.AppStateVM;
import im.actor.core.viewmodel.GlobalStateVM;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;

import static im.actor.runtime.actors.ActorSystem.system;

public class AppStateModule extends AbsModule {

    private AppStateVM appStateVM;
    private GlobalStateVM globalStateVM;
    private ActorRef listStatesActor;

    public AppStateModule(ModuleContext context) {
        super(context);

        globalStateVM = new GlobalStateVM(context);
    }

    public void run() {
        this.appStateVM = new AppStateVM(context());
        listStatesActor = system().actorOf("actor/app/state", () -> new ListsStatesActor(context()));
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

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }

    public GlobalStateVM getGlobalStateVM() {
        return globalStateVM;
    }

    public void resetModule() {
        // TODO: Implement
    }
}
