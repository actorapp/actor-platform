package im.actor.model.js.angular;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.viewmodel.UserVM;

import java.util.HashMap;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularModule extends BaseModule {
    private JsMessenger messenger;
    private AngularList<JsDialog, Dialog> dialogsList;
    private HashMap<Peer, AngularList<JsMessage, Message>> messagesList = new HashMap<Peer, AngularList<JsMessage, Message>>();
    private HashMap<Integer, AngularValue<JsUser>> users;

    public AngularModule(JsMessenger messenger, Modules modules) {
        super(modules);
        this.messenger = messenger;

        users = new HashMap<Integer, AngularValue<JsUser>>();
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        if (dialogsList == null) {
            dialogsList = new AngularList<JsDialog, Dialog>((JsListEngine<Dialog>) modules().getMessagesModule().getDialogsEngine(),
                    JsDialog.CONVERTER, messenger);
        }
        return dialogsList;
    }

    public AngularList<JsMessage, Message> getMessagesList(Peer peer) {
        if (!messagesList.containsKey(peer)) {
            messagesList.put(peer, new AngularList<JsMessage, Message>(
                    (JsListEngine<Message>) modules().getMessagesModule().getConversationEngine(peer), JsMessage.CONVERTER, messenger));
        }
        return messagesList.get(peer);
    }

    public AngularValue<JsUser> getUser(int uid) {
        if (!users.containsKey(uid)) {
            UserVM userVM = modules().getUsersModule().getUsersCollection().get(uid);
            final AngularValue<JsUser> value = new AngularValue<JsUser>(JsUser.fromUserVM(userVM));
            userVM.subscribe(new ModelChangedListener<UserVM>() {
                @Override
                public void onChanged(UserVM model) {
                    value.changeValue(JsUser.fromUserVM(model));
                }
            });
            users.put(uid, value);
        }
        return users.get(uid);
    }
}
