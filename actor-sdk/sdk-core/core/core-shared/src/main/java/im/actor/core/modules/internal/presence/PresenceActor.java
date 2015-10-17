/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.internal.presence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestSubscribeToGroupOnline;
import im.actor.core.api.rpc.RequestSubscribeToOnline;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.events.NewSessionCreated;
import im.actor.core.modules.events.PeerChatOpened;
import im.actor.core.modules.events.PeerInfoOpened;
import im.actor.core.modules.events.UserVisible;
import im.actor.core.modules.utils.ModuleActor;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.MailboxCreator;
import im.actor.runtime.actors.Props;
import im.actor.runtime.actors.mailbox.Envelope;
import im.actor.runtime.actors.mailbox.Mailbox;
import im.actor.runtime.actors.mailbox.MailboxesQueue;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;

@Verified
public class PresenceActor extends ModuleActor implements BusSubscriber {

    public static ActorRef create(final ModuleContext messenger) {
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
                        if (a.getMessage() instanceof OnlineUserTimeout && b.getMessage() instanceof OnlineUserTimeout) {
                            if (((OnlineUserTimeout) a.getMessage()).getUid() == ((OnlineUserTimeout) b.getMessage()).getUid()) {
                                return true;
                            }
                        }
                        return super.isEqualEnvelope(a, b);
                    }
                };
            }
        }), "actor/presence/users");
    }

    private static final int ONLINE_TIMEOUT = 5 * 60 * 1000;
    private static final long FOREVER = 24 * 60 * 60 * 1000L;

    private static final String TAG = "PresenceActor";

    private HashMap<Integer, Long> lastUidState = new HashMap<Integer, Long>();
    private HashMap<Integer, Long> lastGidState = new HashMap<Integer, Long>();
    private HashSet<Integer> uids = new HashSet<Integer>();
    private HashSet<Integer> gids = new HashSet<Integer>();

    public PresenceActor(ModuleContext messenger) {
        super(messenger);
    }

    @Override
    public void preStart() {
        context().getEvents().subscribe(this, NewSessionCreated.EVENT);
        context().getEvents().subscribe(this, PeerChatOpened.EVENT);
        context().getEvents().subscribe(this, PeerInfoOpened.EVENT);
        context().getEvents().subscribe(this, UserVisible.EVENT);
    }

    @Verified
    private void onUserOnline(int uid, long updateDate) {
        Log.d(TAG, "onUserOnline  #" + uid + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            Log.d(TAG, "onUserOnline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        Log.d(TAG, "onUserOnline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.ONLINE));
        }

        // Send
        self().sendOnce(new OnlineUserTimeout(uid, (int) ((updateDate + ONLINE_TIMEOUT) / 1000L),
                updateDate + ONLINE_TIMEOUT), ONLINE_TIMEOUT);
    }

    @Verified
    private void onUserOffline(int uid, long updateDate) {
        Log.d(TAG, "onUserOffline  #" + uid + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            Log.d(TAG, "onUserOffline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        Log.d(TAG, "onUserOffline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE));
        }

        // Cancel timeout
        self().sendOnce(new OnlineUserTimeout(uid, (int) ((updateDate + ONLINE_TIMEOUT) / 1000L),
                updateDate + ONLINE_TIMEOUT), FOREVER);
    }

    @Verified
    private void onUserLastSeen(int uid, int date, long updateDate) {
        Log.d(TAG, "onUserLastSeen  #" + uid + " at " + date + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            Log.d(TAG, "onUserLastSeen:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        Log.d(TAG, "onUserLastSeen:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, date));
        }

        // Cancel timeout
        self().sendOnce(new OnlineUserTimeout(uid, 0, 0), FOREVER);
    }

    private void onUserGoesOffline(int uid, int date, long updateDate) {
        Log.d(TAG, "onUserGoesOffline  #" + uid + " at " + date + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            Log.d(TAG, "onUserGoesOffline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        Log.d(TAG, "onUserGoesOffline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, date));
        }

        // Cancel timeout
        self().sendOnce(new OnlineUserTimeout(uid, 0, 0), FOREVER);
    }

    @Verified
    private void onGroupOnline(int gid, int count, long updateDate) {
        Log.d(TAG, "onGroupOnline  #" + gid + " " + count + " at " + updateDate);
        if (lastGidState.containsKey(gid) && lastGidState.get(gid) >= updateDate) {
            Log.d(TAG, "onGroupOnline:ignored - too old");
            return;
        }
        lastGidState.put(gid, updateDate);
        Log.d(TAG, "onGroupOnline:updated");

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
            List<ApiUserOutPeer> peers = new ArrayList<ApiUserOutPeer>();
            peers.add(new ApiUserOutPeer(user.getUid(), user.getAccessHash()));
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
            List<ApiGroupOutPeer> peers = new ArrayList<ApiGroupOutPeer>();
            peers.add(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()));
            request(new RequestSubscribeToGroupOnline(peers));
        }
    }

    @Verified
    private void onNewSessionCreated() {

        // Resubscribing for online states of users
        List<ApiUserOutPeer> userPeers = new ArrayList<ApiUserOutPeer>();
        for (int uid : uids) {
            User user = getUser(uid);
            if (user == null) {
                continue;
            }
            userPeers.add(new ApiUserOutPeer(uid, user.getAccessHash()));
        }
        if (userPeers.size() > 0) {
            request(new RequestSubscribeToOnline(userPeers));
        }

        // Resubscribing for online states of groups
        List<ApiGroupOutPeer> groupPeers = new ArrayList<ApiGroupOutPeer>();
        for (int gid : gids) {
            Group group = getGroup(gid);
            if (group == null) {
                continue;
            }
            groupPeers.add(new ApiGroupOutPeer(group.getGroupId(), group.getAccessHash()));
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
        } else if (message instanceof OnlineUserTimeout) {
            OnlineUserTimeout timeout = (OnlineUserTimeout) message;
            onUserGoesOffline(timeout.getUid(), timeout.getDate(), timeout.getUpdateDate());
        } else {
            drop(message);
        }
    }

    @Override
    public void onBusEvent(Event event) {
        if (event instanceof NewSessionCreated) {
            self().send(new SessionCreated());
        } else if (event instanceof PeerChatOpened) {
            self().send(new Subscribe(((PeerChatOpened) event).getPeer()));
        } else if (event instanceof PeerInfoOpened) {
            self().send(new Subscribe(((PeerInfoOpened) event).getPeer()));
        } else if (event instanceof UserVisible) {
            self().send(new Subscribe(Peer.user(((UserVisible) event).getUid())));
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

    }

    public static class UserLastSeen {
        private int uid;
        private int date;
        private long updateDate;

        public UserLastSeen(int uid, int date, long updateDate) {
            this.uid = uid;
            this.date = date;
            this.updateDate = updateDate;
        }

        public int getUid() {
            return uid;
        }

        public int getDate() {
            return date;
        }

        public long getUpdateDate() {
            return updateDate;
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
    }

    private static class OnlineUserTimeout {

        private int uid;
        private int date;
        private long updateDate;

        public OnlineUserTimeout(int uid, int date, long updateDate) {
            this.uid = uid;
            this.date = date;
            this.updateDate = updateDate;
        }

        public int getUid() {
            return uid;
        }

        public int getDate() {
            return date;
        }

        public long getUpdateDate() {
            return updateDate;
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
