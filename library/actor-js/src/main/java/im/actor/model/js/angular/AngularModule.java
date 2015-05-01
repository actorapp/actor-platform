package im.actor.model.js.angular;

import java.util.HashMap;

import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.entity.JsDialog;
import im.actor.model.js.entity.JsGroup;
import im.actor.model.js.entity.JsMessage;
import im.actor.model.js.entity.JsUser;
import im.actor.model.js.providers.storage.JsListEngine;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.ModelChangedListener;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 27.03.15.
 */
public class AngularModule extends BaseModule {
    private JsMessenger messenger;
    private AngularList<JsDialog, Dialog> dialogsList;
    private HashMap<Peer, AngularList<JsMessage, Message>> messagesList = new HashMap<Peer, AngularList<JsMessage, Message>>();
    private HashMap<Integer, AngularValue<JsUser>> users;
    private HashMap<Integer, AngularValue<JsGroup>> groups;

    public AngularModule(JsMessenger messenger, Modules modules) {
        super(modules);
        this.messenger = messenger;

        users = new HashMap<Integer, AngularValue<JsUser>>();
        groups = new HashMap<Integer, AngularValue<JsGroup>>();
    }

    public AngularList<JsDialog, Dialog> getDialogsList() {
        if (dialogsList == null) {
            dialogsList = new AngularList<JsDialog, Dialog>((JsListEngine<Dialog>) modules().getMessagesModule().getDialogsEngine(),
                    false, JsDialog.CONVERTER, messenger);
        }
        return dialogsList;
    }

    public AngularList<JsMessage, Message> getMessagesList(Peer peer) {
        if (!messagesList.containsKey(peer)) {
            messagesList.put(peer, new AngularList<JsMessage, Message>(
                    (JsListEngine<Message>) modules().getMessagesModule().getConversationEngine(peer),
                    true, JsMessage.CONVERTER, messenger));
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

    public AngularValue<JsGroup> getGroup(int gid) {
        if (!groups.containsKey(gid)) {
            GroupVM groupVM = modules().getGroupsModule().getGroupsCollection().get(gid);
            final AngularValue<JsGroup> value = new AngularValue<>(JsGroup.fromGroupVM(groupVM));
            groupVM.subscribe(new ModelChangedListener<GroupVM>() {
                @Override
                public void onChanged(GroupVM model) {
                    value.changeValue(JsGroup.fromGroupVM(model));
                }
            });
            groups.put(gid, value);
        }
        return groups.get(gid);
    }
}
