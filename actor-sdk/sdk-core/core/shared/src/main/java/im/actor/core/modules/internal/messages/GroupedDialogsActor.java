package im.actor.core.modules.internal.messages;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.DialogSpec;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.internal.messages.entity.GroupedStorage;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.viewmodel.DialogGroup;
import im.actor.core.viewmodel.DialogSmall;
import im.actor.core.viewmodel.DialogSpecVM;
import im.actor.core.viewmodel.generics.ArrayListDialogSmall;
import im.actor.runtime.mvvm.MVVMCollection;

public class GroupedDialogsActor extends ModuleActor {

    private static final String PREFERENCE_GROUPED = "dialogs.grouped";

    private GroupedStorage storage;
    private MVVMCollection<DialogSpec, DialogSpecVM> specs;

    public GroupedDialogsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
        specs = context().getMessagesModule().getDialogDescKeyValue();
        storage = new GroupedStorage();

        byte[] data = preferences().getBytes(PREFERENCE_GROUPED);
        if (data != null) {
            try {
                storage = new GroupedStorage(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notifyVM();
    }

    private void onNewMessage(Peer peer, long sortDate, int counter) {

        // Updating dialog spec
        DialogSpec spec = new DialogSpec(peer, false, counter);
        specs.getEngine().addOrUpdateItem(spec);

        boolean found = false;
        for (Peer p : storage.getPrivatePeers()) {
            if (p.equals(peer)) {
                found = true;
                break;
            }
        }
        for (Peer p : storage.getGroupPeers()) {
            if (p.equals(peer)) {
                found = true;
                break;
            }
        }

        if (!found) {
            if (peer.getPeerType() == PeerType.PRIVATE) {
                storage.getPrivatePeers().add(peer);
            } else if (peer.getPeerType() == PeerType.GROUP) {
                storage.getGroupPeers().add(peer);
            } else {
                return;
            }
        }

        notifyVM();
        saveStorage();
    }

    private void notifyVM() {
        ArrayListDialogSmall groupSpecs = new ArrayListDialogSmall();
        for (Peer p : storage.getGroupPeers()) {
            DialogSpec spec = specs.getEngine().getValue(p.getUnuqueId());
            if (p.getPeerType() == PeerType.GROUP) {
                Group group = getGroup(p.getPeerId());
                groupSpecs.add(new DialogSmall(p,
                        group.getTitle(), group.getAvatar(),
                        spec.getCounter()));
            } else if (p.getPeerType() == PeerType.PRIVATE) {
                User user = getUser(p.getPeerId());
                groupSpecs.add(new DialogSmall(p,
                        user.getName(), user.getAvatar(),
                        spec.getCounter()));
            }
        }

        ArrayListDialogSmall privateSpecs = new ArrayListDialogSmall();
        for (Peer p : storage.getPrivatePeers()) {
            DialogSpec spec = specs.getEngine().getValue(p.getUnuqueId());
            if (p.getPeerType() == PeerType.GROUP) {
                Group group = getGroup(p.getPeerId());
                privateSpecs.add(new DialogSmall(p,
                        group.getTitle(), group.getAvatar(),
                        spec.getCounter()));
            } else if (p.getPeerType() == PeerType.PRIVATE) {
                User user = getUser(p.getPeerId());
                privateSpecs.add(new DialogSmall(p,
                        user.getName(), user.getAvatar(),
                        spec.getCounter()));
            }
        }

        ArrayList<DialogGroup> groups = new ArrayList<DialogGroup>();
        groups.add(new DialogGroup("Groups", "groups", groupSpecs));
        groups.add(new DialogGroup("Private", "private", privateSpecs));

        context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel().change(groups);
    }

    private void onPeerInfoChanged(Peer peer, String title, Avatar avatar) {
        // TODO: Implement
    }


    private void saveStorage() {
        preferences().putBytes(PREFERENCE_GROUPED, storage.toByteArray());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof PeerInformationChanged) {
            PeerInformationChanged informationChanged = (PeerInformationChanged) message;
            onPeerInfoChanged(informationChanged.getPeer(),
                    informationChanged.getTitle(),
                    informationChanged.getAvatar());
        } else if (message instanceof NewMessage) {
            NewMessage newMessage = (NewMessage) message;
            onNewMessage(newMessage.peer, newMessage.sortDate, newMessage.counter);
        } else {
            super.onReceive(message);
        }
    }

    public static class PeerInformationChanged {

        private Peer peer;
        private String title;
        private Avatar avatar;

        public PeerInformationChanged(Peer peer, String title, Avatar avatar) {
            this.peer = peer;
            this.title = title;
            this.avatar = avatar;
        }

        public Peer getPeer() {
            return peer;
        }

        public String getTitle() {
            return title;
        }

        public Avatar getAvatar() {
            return avatar;
        }
    }

    public static class CounterChanged {
        private Peer peer;
        private int counter;

        public CounterChanged(Peer peer, int counter) {
            this.peer = peer;
            this.counter = counter;
        }

        public Peer getPeer() {
            return peer;
        }

        public int getCounter() {
            return counter;
        }
    }

    public static class NewMessage {

        private Peer peer;
        private int counter;
        private long sortDate;

        public NewMessage(Peer peer, int counter, long sortDate) {
            this.peer = peer;
            this.counter = counter;
            this.sortDate = sortDate;
        }
    }

    private class PeerGroup {

        private String key;
        private String title;
        private ArrayList<PeerDesc> peers;

        public PeerGroup(String key, String title) {
            this.key = key;
            this.title = title;
            this.peers = new ArrayList<PeerDesc>();
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public ArrayList<PeerDesc> getPeers() {
            return peers;
        }
    }

    private class PeerDesc {

        private Peer peer;
        private int counter;

        public PeerDesc(Peer peer, int counter) {
            this.peer = peer;
            this.counter = counter;
        }

        public Peer getPeer() {
            return peer;
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }
    }
}