package im.actor.model.modules.search;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.SearchEntity;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 05.04.15.
 */
public class SearchActor extends ModuleActor {

    private ListEngine<SearchEntity> listEngine;

    public SearchActor(Modules modules) {
        super(modules);
    }

    @Override
    public void preStart() {
        super.preStart();

        listEngine = modules().getSearch().getSearchList();
    }

    private void onDialogsUpdated(List<Dialog> dialogs) {
        List<SearchEntity> updated = new ArrayList<SearchEntity>();
        for (Dialog d : dialogs) {
            updated.add(new SearchEntity(d.getPeer(), d.getSortDate(), d.getDialogAvatar(),
                    d.getDialogTitle()));
        }
        listEngine.addOrUpdateItems(updated);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnDialogsUpdated) {
            OnDialogsUpdated onDialogsUpdated = (OnDialogsUpdated) message;
            onDialogsUpdated(onDialogsUpdated.getDialogs());
        } else {
            drop(message);
        }
    }

    public static class OnDialogsUpdated {
        private List<Dialog> dialogs;

        public OnDialogsUpdated(List<Dialog> dialogs) {
            this.dialogs = dialogs;
        }

        public List<Dialog> getDialogs() {
            return dialogs;
        }
    }

    public static class OnContactsUpdated {
        private int[] contactsList;

        public OnContactsUpdated(int[] contactsList) {
            this.contactsList = contactsList;
        }

        public int[] getContactsList() {
            return contactsList;
        }
    }
}
