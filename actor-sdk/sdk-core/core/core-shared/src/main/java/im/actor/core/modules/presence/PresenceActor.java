/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.presence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.core.api.ApiGroupOutPeer;
import im.actor.core.api.ApiUserOutPeer;
import im.actor.core.api.rpc.RequestSubscribeToGroupOnline;
import im.actor.core.api.rpc.RequestSubscribeToOnline;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.entity.Group;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.User;
import im.actor.core.modules.ModuleContext;
import im.actor.core.events.NewSessionCreated;
import im.actor.core.events.PeerChatOpened;
import im.actor.core.events.PeerInfoOpened;
import im.actor.core.events.UserVisible;
import im.actor.core.modules.ModuleActor;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.Log;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.Props;
import im.actor.runtime.annotations.Verified;
import im.actor.runtime.eventbus.BusSubscriber;
import im.actor.runtime.eventbus.Event;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromisesArray;

@Verified
public class PresenceActor extends ModuleActor implements BusSubscriber {

    public static ActorRef create(final ModuleContext messenger) {
        return ActorSystem.system().actorOf("actor/presence", () -> new PresenceActor(messenger));
    }

    private static final int ONLINE_TIMEOUT = 5 * 60 * 1000;

    // private static final String TAG = "PresenceActor";

    private HashMap<Integer, Long> lastUidState = new HashMap<>();
    private HashMap<Integer, Long> lastGidState = new HashMap<>();
    private HashMap<Integer, Cancellable> uidCancellables = new HashMap<>();
    private HashSet<Integer> uids = new HashSet<>();
    private HashSet<Integer> gids = new HashSet<>();

    private boolean isRequesting = false;
    private ArrayList<Peer> pendingPeers = new ArrayList<>();

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
        // Log.d(TAG, "onUserOnline  #" + uid + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            // Log.d(TAG, "onUserOnline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        // Log.d(TAG, "onUserOnline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.ONLINE));
        }

        // Updating timeout
        if (uidCancellables.containsKey(uid)) {
            uidCancellables.remove(uid).cancel();
        }
        uidCancellables.put(uid, schedule(new OnlineUserTimeout(uid, (int) ((updateDate + ONLINE_TIMEOUT) / 1000L),
                updateDate + ONLINE_TIMEOUT), ONLINE_TIMEOUT));
    }

    @Verified
    private void onUserOffline(int uid, long updateDate) {
        // Log.d(TAG, "onUserOffline  #" + uid + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            // Log.d(TAG, "onUserOffline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        // Log.d(TAG, "onUserOffline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE));
        }

        // Cancel timeout
        if (uidCancellables.containsKey(uid)) {
            uidCancellables.remove(uid).cancel();
        }
    }

    @Verified
    private void onUserLastSeen(int uid, int date, long updateDate) {
        // Log.d(TAG, "onUserLastSeen  #" + uid + " at " + date + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            // Log.d(TAG, "onUserLastSeen:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        // Log.d(TAG, "onUserLastSeen:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, date));
        }

        // Cancel timeout
        if (uidCancellables.containsKey(uid)) {
            uidCancellables.remove(uid).cancel();
        }
    }

    private void onUserGoesOffline(int uid, int date, long updateDate) {
        // Log.d(TAG, "onUserGoesOffline  #" + uid + " at " + date + " at " + updateDate);
        if (lastUidState.containsKey(uid) && lastUidState.get(uid) >= updateDate) {
            // Log.d(TAG, "onUserGoesOffline:ignored - too old");
            return;
        }
        lastUidState.put(uid, updateDate);
        // Log.d(TAG, "onUserGoesOffline:updated");

        UserVM vm = getUserVM(uid);
        if (vm != null) {
            vm.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, date));
        }

        // Cancel timeout
        if (uidCancellables.containsKey(uid)) {
            uidCancellables.remove(uid).cancel();
        }
    }

    @Verified
    private void onGroupOnline(int gid, int count, long updateDate) {
        // Log.d(TAG, "onGroupOnline  #" + gid + " " + count + " at " + updateDate);
        if (lastGidState.containsKey(gid) && lastGidState.get(gid) >= updateDate) {
            // Log.d(TAG, "onGroupOnline:ignored - too old");
            return;
        }
        lastGidState.put(gid, updateDate);
        // Log.d(TAG, "onGroupOnline:updated");

        GroupVM vm = getGroupVM(gid);
        if (vm != null) {
            vm.getPresence().change(count);
        }
    }

    @Verified
    private void subscribe(Peer peer) {

        // Log.d(TAG, "subscribe:" + peer);

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

        } else {
            return;
        }

        // Adding Pending Peer
        if (pendingPeers.contains(peer)) {
            return;
        }
        pendingPeers.add(peer);

        onCheckQueue();
    }

    @Verified
    private void onNewSessionCreated() {

        // Resubscribing for online states of users
        for (int uid : uids) {
            Peer p = Peer.user(uid);
            if (!pendingPeers.contains(p)) {
                pendingPeers.add(p);
            }
        }

        // Resubscribing for online states of groups
        for (int gid : gids) {
            Peer p = Peer.group(gid);
            if (!pendingPeers.contains(p)) {
                pendingPeers.add(p);
            }
        }

        onCheckQueue();
    }

    private void onCheckQueue() {

        if (isRequesting) {
            return;
        }

        if (pendingPeers.size() == 0) {
            return;
        }

        ArrayList<Peer> destPeers = new ArrayList<>(pendingPeers);
        pendingPeers.clear();
        ArrayList<ApiUserOutPeer> outUserPeers = new ArrayList<>();
        ArrayList<ApiGroupOutPeer> outGroupPeers = new ArrayList<>();

        for (Peer p : destPeers) {
            if (p.getPeerType() == PeerType.GROUP) {
                Group g = getGroup(p.getPeerId());
                if (g != null) {
                    outGroupPeers.add(new ApiGroupOutPeer(p.getPeerId(), g.getAccessHash()));
                }
            } else if (p.getPeerType() == PeerType.PRIVATE) {
                User u = getUser(p.getPeerId());
                if (u != null) {
                    outUserPeers.add(new ApiUserOutPeer(p.getPeerId(), u.getAccessHash()));
                }
            }
        }

        ArrayList<Promise<ResponseVoid>> requests = new ArrayList<>();
        if (outUserPeers.size() > 0) {
            requests.add(api(new RequestSubscribeToOnline(outUserPeers)));
        }
        if (outGroupPeers.size() > 0) {
            requests.add(api(new RequestSubscribeToGroupOnline(outGroupPeers)));
        }

        if (requests.size() > 0) {
            isRequesting = true;
            PromisesArray.ofPromises(requests).zip().then(responseVoids -> {
                isRequesting = false;
                onCheckQueue();
            }).failure(e -> {
                isRequesting = false;
                onCheckQueue();
            });
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
            super.onReceive(message);
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
