package im.actor.model.modules.presence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import im.actor.model.Messenger;
import im.actor.model.OnlineCallback;
import im.actor.model.api.UserOutPeer;
import im.actor.model.api.rpc.RequestSubscribeToOnline;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.User;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 15.02.15.
 */
public class PresenceActor extends ModuleActor {

    public static ActorRef get(final Messenger messenger) {
        return ActorSystem.system().actorOf(Props.create(PresenceActor.class, new ActorCreator<PresenceActor>() {
            @Override
            public PresenceActor create() {
                return new PresenceActor(messenger);
            }
        }), "actor/presence/users");
    }

    private static final int ONLINE_TIMEOUT = 5 * 60 * 1000;

    private HashSet<Integer> uids = new HashSet<Integer>();
    private OnlineCallback onlineCallback;

    public PresenceActor(Messenger messenger) {
        super(messenger);

        onlineCallback = messenger.getConfiguration().getOnlineCallback();
    }

    private void onUserOnline(int uid) {
        if (onlineCallback != null) {
            onlineCallback.onUserOnline(uid);
        }
        self().sendOnce(new UserOffline(uid), ONLINE_TIMEOUT);
    }

    private void onUserOffline(int uid) {
        if (onlineCallback != null) {
            onlineCallback.onUserOffline(uid);
        }
    }

    private void onUserLastSeen(int uid, long date) {
        if (onlineCallback != null) {
            onlineCallback.onUserLastSeen(uid, date);
        }
    }

    private void onGroupOnline(int gid, int count) {
        if (onlineCallback != null) {
            onlineCallback.onGroupOnline(gid, count);
        }
    }

    private void subscribe(Peer peer) {
        if (peer.getPeerType() == PeerType.PRIVATE) {
            if (uids.contains(peer.getPeerId())) {
                return;
            }
            User user = getMessenger().getUsers().getValue(peer.getPeerId());
            if (user == null) {
                return;
            }
            uids.add(user.getUid());
            List<UserOutPeer> peers = new ArrayList<UserOutPeer>();
            peers.add(new UserOutPeer(user.getUid(), user.getAccessHash()));
            request(new RequestSubscribeToOnline(peers));
        } else if (peer.getPeerType() == PeerType.GROUP) {
            // TODO: Implement
        }
    }

    private void onNewSessionCreated() {
        List<UserOutPeer> userPeers = new ArrayList<UserOutPeer>();
        for (int uid : uids) {
            User user = getMessenger().getUsers().getValue(uid);
            if (user == null) {
                continue;
            }
            userPeers.add(new UserOutPeer(uid, user.getAccessHash()));
        }
        if (userPeers.size() > 0) {
            request(new RequestSubscribeToOnline(userPeers));
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof UserOnline) {
            UserOnline online = (UserOnline) message;
            onUserOnline(online.getUid());
        } else if (message instanceof UserOffline) {
            UserOffline offline = (UserOffline) message;
            onUserOffline(offline.getUid());
        } else if (message instanceof UserLastSeen) {
            UserLastSeen lastSeen = (UserLastSeen) message;
            onUserLastSeen(lastSeen.getUid(), lastSeen.getDate());
        } else if (message instanceof GroupOnline) {
            GroupOnline groupOnline = (GroupOnline) message;
            onGroupOnline(groupOnline.getGid(), groupOnline.getCount());
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

        public UserOnline(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class UserOffline {
        private int uid;

        public UserOffline(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class UserLastSeen {
        private int uid;
        private long date;

        public UserLastSeen(int uid, long date) {
            this.uid = uid;
            this.date = date;
        }

        public int getUid() {
            return uid;
        }

        public long getDate() {
            return date;
        }
    }

    public static class GroupOnline {
        private int gid;
        private int count;

        public GroupOnline(int gid, int count) {
            this.gid = gid;
            this.count = count;
        }

        public int getGid() {
            return gid;
        }

        public int getCount() {
            return count;
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
