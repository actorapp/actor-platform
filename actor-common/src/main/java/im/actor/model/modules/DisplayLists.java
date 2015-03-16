package im.actor.model.modules;

import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.entity.Contact;
import im.actor.model.entity.Dialog;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.MVVMEngine;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class DisplayLists extends BaseModule {

    private static final int LOAD_GAP = 20;
    private static final int LOAD_PAGE = 20;

    private BindedDisplayList<Dialog> dialogGlobalList;

    private BindedDisplayList<Contact> contactsGlobalList;

    public DisplayLists(Modules modules) {
        super(modules);
    }

    public BindedDisplayList<Contact> getContactsGlobalList() {
        MVVMEngine.checkMainThread();

        if (contactsGlobalList == null) {
            contactsGlobalList = buildNewContactList(true);
        }

        return contactsGlobalList;
    }

    public BindedDisplayList<Dialog> getDialogsGlobalList() {
        MVVMEngine.checkMainThread();

        if (dialogGlobalList == null) {
            dialogGlobalList = buildNewDialogsList(true);
        }

        return dialogGlobalList;
    }

    public BindedDisplayList<Dialog> buildNewDialogsList(boolean disableDispose) {
        MVVMEngine.checkMainThread();

        ListEngine<Dialog> dialogsEngine = modules().getMessagesModule().getDialogsEngine();
        if (!(dialogsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Dialogs ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Dialog> displayList = new BindedDisplayList<Dialog>((ListEngineDisplayExt<Dialog>) dialogsEngine,
                disableDispose, LOAD_PAGE, LOAD_GAP);
        displayList.initTop(false);
        return displayList;
    }

    public BindedDisplayList<Contact> buildNewContactList(boolean disableDispose) {
        MVVMEngine.checkMainThread();

        ListEngine<Contact> contactsEngine = modules().getContactsModule().getContacts();
        if (!(contactsEngine instanceof ListEngineDisplayExt)) {
            throw new RuntimeException("Contacts ListEngine must implement ListEngineDisplayExt for using global list");
        }

        BindedDisplayList<Contact> contactList = new BindedDisplayList<Contact>((ListEngineDisplayExt<Contact>) contactsEngine,
                disableDispose, LOAD_PAGE, LOAD_GAP);
        contactList.initTop(false);
        return contactList;
    }
}
