/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.search;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.engine.ListEngine;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Peer;
import im.actor.model.entity.SearchEntity;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

public class SearchActor extends ModuleActor {

    private static final long CONTACTS_PREFIX = 1L << 32;

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

    private void onContactsUpdated(int[] contactsList) {
        List<SearchEntity> updated = new ArrayList<SearchEntity>();
        for (int i = 0; i < contactsList.length; i++) {
            User user = users().getValue(contactsList[i]);
            updated.add(new SearchEntity(Peer.user(user.getUid()), CONTACTS_PREFIX + i, user.getAvatar(),
                    user.getName()));
        }
        listEngine.addOrUpdateItems(updated);
    }

    private void clear() {
        listEngine.clear();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof OnDialogsUpdated) {
            OnDialogsUpdated onDialogsUpdated = (OnDialogsUpdated) message;
            onDialogsUpdated(onDialogsUpdated.getDialogs());
        } else if (message instanceof OnContactsUpdated) {
            OnContactsUpdated contactsUpdated = (OnContactsUpdated) message;
            onContactsUpdated(contactsUpdated.getContactsList());
        } else if (message instanceof Clear) {
            clear();
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

    public static class Clear {

    }
}
