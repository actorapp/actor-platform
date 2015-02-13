package im.actor.messenger.core.actors.presence;

import com.droidkit.actors.*;
import com.droidkit.actors.mailbox.Envelope;
import com.droidkit.actors.mailbox.Mailbox;
import com.droidkit.actors.mailbox.MailboxesQueue;

import im.actor.api.scheme.UserOutPeer;
import im.actor.messenger.core.actors.api.NewSessionCreated;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.model.UserPresence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 07.09.14.
 */
public class UsersPresence extends Actor {

    public static ActorRef presence() {
        return ActorSystem.system().actorOf(props(), "users_presence");
    }

    public static Props<UsersPresence> props() {
        return Props.create(UsersPresence.class, new MailboxCreator() {
            @Override
            public Mailbox createMailbox(MailboxesQueue queue) {
                return new PresenceMailbox(queue);
            }
        });
    }

    private static final HashSet<Integer> presence = new HashSet<Integer>();

    private static final int ONLINE_TIMEOUT = 5 * 60 * 1000;

    @Override
    public void onReceive(Object message) {
        if (message instanceof UserGoesOnline) {
            UserGoesOnline online = (UserGoesOnline) message;
            UserModel model = users().get(online.uid);
            if (model != null) {
                model.getPresence().change(new UserPresence(UserPresence.State.ONLINE));
            }
            self().sendOnce(new UserGoesOffline(online.uid), ONLINE_TIMEOUT);
        } else if (message instanceof UserGoesOffline) {
            UserGoesOffline online = (UserGoesOffline) message;
            UserModel model = users().get(online.uid);
            if (model != null) {
                switch (model.getPresence().getValue().getState()) {
                    case OFFLINE:
                    case UNKNOWN:
                        return;
                }
                model.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, 0));
            }
        } else if (message instanceof UserLastSeen) {
            UserLastSeen lastSeen = (UserLastSeen) message;
            UserModel model = users().get(lastSeen.uid);
            if (model != null) {
                model.getPresence().change(new UserPresence(UserPresence.State.OFFLINE, lastSeen.lastSeen));
            }
        } else if (message instanceof ConversationOpen) {
            ConversationOpen conv = (ConversationOpen) message;
            if (conv.getType() != DialogType.TYPE_USER) {
                return;
            }
            if (presence.contains(conv.getId())) {
                return;
            }
            UserModel user = users().get(conv.getId());
            if (user == null) {
                return;
            }
            presence.add(conv.getId());

            List<UserOutPeer> peers = new ArrayList<UserOutPeer>();
            peers.add(new UserOutPeer(user.getId(), user.getAccessHash()));
            requests().subscribeToOnline(peers);
        } else if (message instanceof NewSessionCreated) {
            List<UserOutPeer> peers = new ArrayList<UserOutPeer>();
            for (Integer uid : presence) {
                UserModel user = users().get(uid);
                if (user == null) {
                    continue;
                }
                peers.add(new UserOutPeer(user.getId(), user.getAccessHash()));
            }
            if (peers.size() > 0) {
                requests().subscribeToOnline(peers);
            }
        }
    }

    private static class PresenceMailbox extends Mailbox {

        /**
         * Creating mailbox
         *
         * @param queue MailboxesQueue
         */
        public PresenceMailbox(MailboxesQueue queue) {
            super(queue);
        }

        @Override
        protected boolean isEqualEnvelope(Envelope a, Envelope b) {
            if (a.getMessage() instanceof UserGoesOnline && b.getMessage() instanceof UserGoesOnline) {
                UserGoesOnline aOn = (UserGoesOnline) a.getMessage();
                UserGoesOnline bOn = (UserGoesOnline) b.getMessage();
                return aOn.uid == bOn.uid;
            }
            if (a.getMessage() instanceof UserGoesOffline && b.getMessage() instanceof UserGoesOffline) {
                UserGoesOffline aOn = (UserGoesOffline) a.getMessage();
                UserGoesOffline bOn = (UserGoesOffline) b.getMessage();
                return aOn.uid == bOn.uid;
            }
            if (a.getMessage() instanceof UserLastSeen && b.getMessage() instanceof UserLastSeen) {
                UserLastSeen aOn = (UserLastSeen) a.getMessage();
                UserLastSeen bOn = (UserLastSeen) b.getMessage();
                return aOn.uid == bOn.uid;
            }
            return super.isEqualEnvelope(a, b);
        }
    }

    public static class UserGoesOnline {
        private int uid;

        public UserGoesOnline(int uid) {
            this.uid = uid;
        }
    }

    public static class UserGoesOffline {
        private int uid;

        public UserGoesOffline(int uid) {
            this.uid = uid;
        }
    }

    public static class UserLastSeen {
        private int uid;
        private long lastSeen;

        public UserLastSeen(int uid, long lastSeen) {
            this.uid = uid;
            this.lastSeen = lastSeen;
        }
    }

    public static class ConversationOpen {
        private int type;
        private int id;

        public ConversationOpen(int type, int id) {
            this.type = type;
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }
    }
}