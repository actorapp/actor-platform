package im.actor.core.modules.conductor;

import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.ActorInterface;

import static im.actor.runtime.actors.ActorSystem.system;

public class Conductor extends ActorInterface {

    public Conductor(ModuleContext context) {
        super(system().actorOf("conductor", () -> new ConductorActor(context)));
    }

    public void finishLaunching() {
        send(new ConductorActor.FinishLaunching());
    }

    public void onContactsLoaded() {
        send(new ConductorActor.ContactsLoaded());
    }

    public void onDialogsLoaded() {
        send(new ConductorActor.DialogsLoaded());
    }

    public void onSettingsLoaded() {
        send(new ConductorActor.SettingsLoaded());
    }

    public void onDialogsChanged(boolean isEmpty) {
        send(new ConductorActor.DialogsChanged(isEmpty));
    }

    public void onContactsChanged(boolean isEmpty) {
        send(new ConductorActor.ContactsChanged(isEmpty));
    }

    public void onPhoneBookImported() {
        send(new ConductorActor.BookImported());
    }
}
