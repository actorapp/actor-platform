package im.actor.model.modules;

import com.droidkit.actors.ActorCreator;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.Props;
import im.actor.model.Messenger;
import im.actor.model.entity.Dialog;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.mvvm.ListEngine;

import static com.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class Messages {
    private Messenger messenger;
    private ListEngine<Dialog> dialogs;
    private ActorRef dialogsActor;

    public Messages(final Messenger messenger) {
        this.messenger = messenger;
        this.dialogs = messenger.getConfiguration().getEnginesFactory().createDialogsEngine();
        this.dialogsActor = system().actorOf(Props.create(DialogsActor.class, new ActorCreator<DialogsActor>() {
            @Override
            public DialogsActor create() {
                return new DialogsActor(messenger);
            }
        }), "actor/messages");
    }

    public ActorRef getDialogsActor() {
        return dialogsActor;
    }

    public ListEngine<Dialog> getDialogsEngine() {
        return dialogs;
    }
}
