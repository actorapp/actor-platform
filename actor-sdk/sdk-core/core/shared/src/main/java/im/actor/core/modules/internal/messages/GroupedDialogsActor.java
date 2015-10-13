package im.actor.core.modules.internal.messages;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.DialogDesc;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.viewmodel.DialogGroup;

public class GroupedDialogsActor extends ModuleActor {

    private PeerGroup groups = new PeerGroup("groups", "Groups");
    private PeerGroup privates = new PeerGroup("private", "Private");

    public GroupedDialogsActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        super.preStart();
    }

    private void onPeerInfoChanged(Peer peer, String title, Avatar avatar) {

    }

    private void onNewMessage(Peer peer, long sortDate, int counter) {

        PeerGroup peerGroup;
        if (peer.getPeerType() == PeerType.GROUP) {
            peerGroup = groups;
        } else if (peer.getPeerType() == PeerType.PRIVATE) {
            peerGroup = privates;
        } else {
            return;
        }

        boolean found = false;
        for (PeerDesc d : peerGroup.getPeers()) {
            if (d.getPeer().equals(peer)) {
                d.setCounter(counter);
                found = true;
            }
        }

        if (!found) {
            peerGroup.getPeers().add(new PeerDesc(peer, counter));
        }

        ArrayList<DialogGroup> groups = new ArrayList<DialogGroup>();
        ArrayList<DialogDesc> groupDescs = new ArrayList<DialogDesc>();
        for (PeerDesc d : peerGroup.getPeers()) {
            groupDescs.add(new DialogDesc(d.getPeer(), "GRPOUP #" + d.getPeer().getPeerId(),
                    null, false, d.counter));
        }
        groups.add(new DialogGroup("Groups", "group", groupDescs));

        context().getMessagesModule().getDialogGroupsVM().getGroupsValueModel().change(groups);
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