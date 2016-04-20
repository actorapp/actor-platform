package im.actor.sdk;

import im.actor.core.Configuration;
import im.actor.core.Messenger;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.runtime.Storage;
import im.actor.runtime.clc.ClcContext;
import im.actor.runtime.clc.ClcJavaPreferenceStorage;
import im.actor.runtime.clc.ClcPreferencesStorage;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ClcMessenger extends Messenger {

    private static final Logger logger = LoggerFactory.getLogger(ClcMessenger.class);
    private HashMap<Peer, BindedDisplayList<Message>> messagesLists = new HashMap<Peer, BindedDisplayList<Message>>();

    /**
     * Construct messenger
     *
     * @param configuration configuration of messenger
     */
    public ClcMessenger(@NotNull Configuration configuration, String context) {
        super(configuration);
        //context for preferences
        setContext(context);
        //context for databases(index,keyvalue,list)
        ((ClcContext)Storage.getStorageRuntime()).setContext(context);
    }

    public BindedDisplayList<Message> getMessageDisplayList(final Peer peer) {
        if (!messagesLists.containsKey(peer)) {
            BindedDisplayList<Message> list = (BindedDisplayList<Message>) modules.getDisplayListsModule().getMessagesSharedList(peer);
            messagesLists.put(peer, list);
        }

        BindedDisplayList<Message> bdl = messagesLists.get(peer);
        bdl.initEmpty();

        return bdl;
    }

    /**
     * @param context Unique id for each messenger instance. For example phone number
     */
    private void setContext(String context) {
        ((ClcPreferencesStorage) modules.getPreferences()).setContext(context);
    }

    public String getContext() {
        return ((ClcPreferencesStorage) modules.getPreferences()).getContext();
    }

    /**
     * clear java preferences
     */
    public void clearPref() {
        try {
            ((ClcJavaPreferenceStorage) modules.getPreferences()).getPref().clear();
        } catch (BackingStoreException e) {
            logger.error("Cannot clear preferences", e);
        }
    }


    public Preferences getPref(){
        return ((ClcJavaPreferenceStorage) modules.getPreferences()).getPref();
    }

}
