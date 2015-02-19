package im.actor.model.modules.typing;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.MailboxCreator;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.droidkit.actors.mailbox.Envelope;
import im.actor.model.droidkit.actors.mailbox.Mailbox;
import im.actor.model.droidkit.actors.mailbox.MailboxesQueue;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class TypingActor extends ModuleActor {

    public static ActorRef get(final Modules messenger) {
        return ActorSystem.system().actorOf(Props.create(TypingActor.class, new ActorCreator<TypingActor>() {
            @Override
            public TypingActor create() {
                return new TypingActor(messenger);
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
        }), "actor/typing");
    }

    private static final int TYPING_TEXT_TIMEOUT = 3000;

    private HashSet<Integer> typings = new HashSet<Integer>();
    private HashMap<Integer, HashSet<Integer>> groupTypings = new HashMap<Integer, HashSet<Integer>>();

    public TypingActor(Modules messenger) {
        super(messenger);
    }

    private void privateTyping(int uid, int type) {
        // Support only simple typings
        if (type != 0) {
            return;
        }

        if (getUser(uid) == null) {
            return;
        }

        if (!typings.contains(uid)) {
            typings.add(uid);

            modules().getTypingModule().getTyping(uid).getTyping().change(true);
        }
        self().sendOnce(new StopTyping(uid), TYPING_TEXT_TIMEOUT);
    }

    private void stopPrivateTyping(int uid) {
        if (typings.contains(uid)) {
            typings.remove(uid);

            modules().getTypingModule().getTyping(uid).getTyping().change(false);
        }
    }

    private void groupTyping(int gid, int uid, int type) {
        // Support only simple typings
        if (type != 0) {
            return;
        }

        // TODO: add group check

        if (getUser(uid) == null) {
            return;
        }

        if (!groupTypings.containsKey(gid)) {
            HashSet<Integer> set = new HashSet<Integer>();
            set.add(uid);
            groupTypings.put(gid, set);

            modules().getTypingModule()
                    .getGroupTyping(gid)
                    .getActive()
                    .change(new int[uid]);
        } else {
            HashSet<Integer> src = groupTypings.get(gid);
            if (!src.contains(uid)) {
                src.add(uid);
                Integer[] ids = src.toArray(new Integer[src.size()]);
                int[] ids2 = new int[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    ids2[i] = ids[i];
                }

                modules().getTypingModule()
                        .getGroupTyping(gid)
                        .getActive()
                        .change(ids2);
            }
        }

        self().sendOnce(new StopGroupTyping(gid, uid), TYPING_TEXT_TIMEOUT);
    }

    private void stopGroupTyping(int gid, int uid) {
        if (!groupTypings.containsKey(gid)) {
            return;
        }
        HashSet<Integer> set = groupTypings.get(gid);
        if (set.contains(uid)) {
            set.remove(uid);
            Integer[] ids = set.toArray(new Integer[set.size()]);
            int[] ids2 = new int[ids.length];
            for (int i = 0; i < ids.length; i++) {
                ids2[i] = ids[i];
            }

            modules().getTypingModule()
                    .getGroupTyping(gid)
                    .getActive()
                    .change(ids2);
        }
    }

    // Messages

    @Override
    public void onReceive(Object message) {
        if (message instanceof PrivateTyping) {
            PrivateTyping typing = (PrivateTyping) message;
            privateTyping(typing.getUid(), typing.getType());
        } else if (message instanceof GroupTyping) {
            GroupTyping typing = (GroupTyping) message;
            groupTyping(typing.getGid(), typing.getUid(), typing.getType());
        } else if (message instanceof StopTyping) {
            StopTyping typing = (StopTyping) message;
            stopPrivateTyping(typing.getUid());
        } else if (message instanceof StopGroupTyping) {
            StopGroupTyping typing = (StopGroupTyping) message;
            stopGroupTyping(typing.getGid(), typing.getUid());
        } else {
            drop(message);
        }
    }

    public static class StopTyping {
        private int uid;

        public StopTyping(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StopTyping that = (StopTyping) o;

            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return uid;
        }
    }

    public static class StopGroupTyping {
        private int gid;
        private int uid;

        public StopGroupTyping(int gid, int uid) {
            this.gid = gid;
            this.uid = uid;
        }

        public int getGid() {
            return gid;
        }

        public int getUid() {
            return uid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StopGroupTyping that = (StopGroupTyping) o;

            if (gid != that.gid) return false;
            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = gid;
            result = 31 * result + uid;
            return result;
        }
    }

    public static class PrivateTyping {
        private int uid;
        private int type;

        public PrivateTyping(int uid, int type) {
            this.uid = uid;
            this.type = type;
        }

        public int getUid() {
            return uid;
        }

        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PrivateTyping that = (PrivateTyping) o;

            if (type != that.type) return false;
            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uid;
            result = 31 * result + type;
            return result;
        }
    }

    public static class GroupTyping {
        private int gid;
        private int uid;
        private int type;

        public GroupTyping(int gid, int uid, int type) {
            this.gid = gid;
            this.uid = uid;
            this.type = type;
        }

        public int getGid() {
            return gid;
        }

        public int getUid() {
            return uid;
        }

        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupTyping that = (GroupTyping) o;

            if (gid != that.gid) return false;
            if (type != that.type) return false;
            if (uid != that.uid) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = gid;
            result = 31 * result + uid;
            result = 31 * result + type;
            return result;
        }
    }
}