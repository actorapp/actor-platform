package im.actor.model.modules;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.state.ListsStatesActor;
import im.actor.model.viewmodel.AppStateVM;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 28.03.15.
 */
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

    }

    public void onContactsUpdate(boolean isEmpty) {

    }

    public AppStateVM getAppStateVM() {
        return appStateVM;
    }
}
