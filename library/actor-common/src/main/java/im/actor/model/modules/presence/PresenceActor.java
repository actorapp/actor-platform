/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.modules.presence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.model.annotation.Verified;
import im.actor.model.api.GroupOutPeer;
import im.actor.model.api.UserOutPeer;
import im.actor.model.api.rpc.RequestSubscribeToGroupOnline;
import im.actor.model.api.rpc.RequestSubscribeToOnline;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.MailboxCreator;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.droidkit.actors.mailbox.Mailbox;
import im.actor.model.droidkit.actors.mailbox.MailboxesQueue;
import im.actor.model.entity.Group;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserVM;

@Verified
public class PresenceActor extends ModuleActor {

    public static ActorRef get(final Modules messenger) {
        return ActorSystem.system().actorOf(Props.create(PresenceActor.class, new ActorCreator<PresenceActor>() {
            @Override
            public PresenceActor create() {
                return new PresenceActor(messenger);
            }
        }, new MailboxCreator() {
            @Override
            public Mailbox createMailbox(MailboxesQueue queue) {
                return new Mailbox(queue) {
                    @Override
                    protected boolean isEqualEnvelope(Envelope a, Envelope b) {
                        if (a.getMessage().equals(b.getMessage())) {
                            return true;
                        }
                        return super.isEqualEnvelope(a, b);
                    }
                };
            }
        }), "actor/presence/users");
    }

    private static final int ONLINE_TIMEOUT = 5 * 60 * 1000;

    private HashMap<Integer, Long> lastUidState = new HashMap<Integer, Long>();
    private HashMap<Integer, Long> lastGidState = new HashMap<Integer, Long>();
    private HashSet<Integer> uids = new HashSet<Integer>();
    private HashSet<Integer> gids = new HashSet<Integer>();

    public PresenceActor(Modules messenger) {
        super(messenger);
    }

    @Verified
    private void onUserOnline(int uid, long updateDate) {
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            return;
        }
        lastUidState.put(uid, updateDate);

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.ONLINE));
        }
        self().sendOnce(new UserOffline(uid, updateDate + ONLINE_TIMEOUT),
                ONLINE_TIMEOUT);
    }

    @Verified
    private void onUserOffline(int uid, long updateDate) {
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            return;
        }
        lastUidState.put(uid, updateDate);

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE));
        }
    }

    @Verified
    private void onUserLastSeen(int uid, long date, long updateDate) {
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            return;
        }
        lastUidState.put(uid, updateDate);

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, date));
        }
    }

    @Verified
    private void onGroupOnline(int gid, int count, long updateDate) {
        if (lastGidState.containsKey(gid) && lastGidState.get(gid) >= updateDate) {
            return;
        }
        lastGidState.put(gid, updateDate);

        GroupVM vm = getGroupVM(gid);
        if (vm != null) {
            vm.getPresence().change(count);
        }
    }

    @Verified
    private void subscribe(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            // Already subscribed
            if (uids.contains(peer.getPeerId())) {
                return;
            }

            User user = getUser(peer.getPeerId());
            if (user == null) {
                return;
            }

            // Subscribing to user online sates
            uids.add(user.getUid());
            List<UserOutPeer> peers = new ArrayList<UserOutPeer>();
            peers.add(new UserOutPeer(user.getUid(), user.getAccessHash()));
            request(new RequestSubscribeToOnline(peers));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            // Already subscribed
            if (gids.contains(peer.getPeerId())) {
                return;
            }

            Group group = getGroup(peer.getPeerId());
            if (group == null) {
                return;
            }

            // Subscribing to group online sates
            gids.add(peer.getPeerId());
            List<GroupOutPeer> peers = new ArrayList<GroupOutPeer>();
            peers.add(new GroupOutPeer(group.getGroupId(), group.getAccessHash()));
            request(new RequestSubscribeToGroupOnline(peers));
        }
    }

    @Verified
    private void onNewSessionCreated() {

        // Resubscribing for online states of users
        List<UserOutPeer> userPeers = new ArrayList<UserOutPeer>();
        for (int uid : uids) {
            User user = getUser(uid);
            if (user == null) {
                continue;
            }
            userPeers.add(new UserOutPeer(uid, user.getAccessHash()));
        }
        if (userPeers.size() > 0) {
            request(new RequestSubscribeToOnline(userPeers));
        }

        // Resubscribing for online states of groups
        List<GroupOutPeer> groupPeers = new ArrayList<GroupOutPeer>();
        for (int gid : gids) {
            Group group = getGroup(gid);
            if (group == null) {
                continue;
            }
            groupPeers.add(new GroupOutPeer(group.getGroupId(), group.getAccessHash()));
        }
        if (groupPeers.size() > 0) {
            request(new RequestSubscribeToGroupOnline(groupPeers));
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof UserOnline) {
            UserOnline online = (UserOnline) message;
            onUserOnline(online.getUid(), online.getUpdateDate());
        } else if (message instanceof UserOffline) {
            UserOffline offline = (UserOffline) message;
            onUserOffline(offline.getUid(), offline.getUpdateDate());
        } else if (message instanceof UserLastSeen) {
            UserLastSeen lastSeen = (UserLastSeen) message;
            onUserLastSeen(lastSeen.getUid(), lastSeen.getDate(), lastSeen.getUpdateDate());
        } else if (message instanceof GroupOnline) {
            GroupOnline groupOnline = (GroupOnline) message;
            onGroupOnline(groupOnline.getGid(), groupOnline.getCount(), groupOnline.getUpdateDate());
        } else if (message instanceof Subscribe) {
            subscribe(((Subscribe) message).getPeer());
        } else if (message instanceof SessionCreated) {
            onNewSessionCreated();
        } else {
            drop(message);
        }
    }

    public static class UserOnline {
        private int uid;
        private long updateDate;

        public UserOnline(int uid, long updateDate) {
            this.uid = uid;
            this.updateDate = updateDate;
        }

        public int getUid() {
            return uid;
        }

        public long getUpdateDate() {
            return updateDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserOnline that = (UserOnline) o;

            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uid;
        }
    }

    public static class UserOffline {
        private int uid;
        private long updateDate;

        public UserOffline(int uid, long updateDate) {
            this.uid = uid;
            this.updateDate = updateDate;
        }

        public int getUid() {
            return uid;
        }

        public long getUpdateDate() {
            return updateDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserOffline that = (UserOffline) o;

            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uid;
        }
    }

    public static class UserLastSeen {
        private int uid;
        private long date;
        private long updateDate;

        public UserLastSeen(int uid, long date, long updateDate) {
            this.uid = uid;
            this.date = date;
            this.updateDate = updateDate;
        }

        public int getUid() {
            return uid;
        }

        public long getDate() {
            return date;
        }

        public long getUpdateDate() {
            return updateDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserLastSeen that = (UserLastSeen) o;

            if (date != that.date) return false;
            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uid;
            result = 31 * result + (int) (date ^ (date >>> 32));
            return result;
        }
    }

    public static class GroupOnline {
        private int gid;
        private int count;
        private long updateDate;

        public GroupOnline(int gid, int count, long updateDate) {
            this.gid = gid;
            this.count = count;
            this.updateDate = updateDate;
        }

        public int getGid() {
            return gid;
        }

        public int getCount() {
            return count;
        }

        public long getUpdateDate() {
            return updateDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupOnline that = (GroupOnline) o;

            if (count != that.count) return false;
            if (gid != that.gid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = gid;
            result = 31 * result + count;
            return result;
        }
    }

    public static class Subscribe {
        private Peer peer;

        public Subscribe(Peer peer) {
            this.peer = peer;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class SessionCreated {

    }
}
