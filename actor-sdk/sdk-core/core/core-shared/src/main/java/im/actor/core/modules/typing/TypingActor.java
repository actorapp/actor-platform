/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.typing;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.core.api.ApiTypingType;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.ModuleActor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Cancellable;
import im.actor.runtime.actors.Props;
import im.actor.runtime.annotations.Verified;

@Verified
public class TypingActor extends ModuleActor {

    public static ActorRef get(final ModuleContext messenger) {
        return ActorSystem.system().actorOf("actor/typing", () -> new TypingActor(messenger));
    }

    private static final int TYPING_TEXT_TIMEOUT = 7000;

    private HashMap<Integer, Cancellable> typingsCancellables = new HashMap<>();
    private HashSet<Integer> typings = new HashSet<>();
    private HashMap<Integer, HashSet<Integer>> groupTypings = new HashMap<>();
    private HashMap<Integer, HashMap<Integer, Cancellable>> groupCancellables = new HashMap<>();

    public TypingActor(ModuleContext messenger) {
        super(messenger);
    }

    @Verified
    private void privateTyping(int uid, ApiTypingType type) {
        // Support only text typings
        if (type != ApiTypingType.TEXT) {
            return;
        }

        if (getUser(uid) == null) {
            return;
        }

        if (!typings.contains(uid)) {
            typings.add(uid);

            context().getTypingModule().getTyping(uid).getTyping().change(true);
        }

        if (typingsCancellables.containsKey(uid)) {
            typingsCancellables.remove(uid).cancel();
        }
        typingsCancellables.put(uid, schedule(new StopTyping(uid), TYPING_TEXT_TIMEOUT));
    }

    @Verified
    private void stopPrivateTyping(int uid) {
        if (typings.contains(uid)) {
            typings.remove(uid);

            if (typingsCancellables.containsKey(uid)) {
                typingsCancellables.remove(uid).cancel();
            }

            context().getTypingModule().getTyping(uid).getTyping().change(false);
        }
    }

    @Verified
    private void groupTyping(int gid, int uid, ApiTypingType type) {
        // Support only text typings
        if (type != ApiTypingType.TEXT) {
            return;
        }

        if (getGroup(gid) == null) {
            return;
        }

        if (getUser(uid) == null) {
            return;
        }

        if (!groupTypings.containsKey(gid)) {
            HashSet<Integer> set = new HashSet<Integer>();
            set.add(uid);
            groupTypings.put(gid, set);

            context().getTypingModule()
                    .getGroupTyping(gid)
                    .getActive()
                    .change(new int[]{uid});
        } else {
            HashSet<Integer> src = groupTypings.get(gid);
            if (!src.contains(uid)) {
                src.add(uid);
                Integer[] ids = src.toArray(new Integer[src.size()]);
                int[] ids2 = new int[ids.length];
                for (int i = 0; i < ids.length; i++) {
                    ids2[i] = ids[i];
                }

                context().getTypingModule()
                        .getGroupTyping(gid)
                        .getActive()
                        .change(ids2);
            }
        }

        if (!groupCancellables.containsKey(gid)) {
            groupCancellables.put(gid, new HashMap<>());
        }

        HashMap<Integer, Cancellable> cancellables = groupCancellables.get(gid);
        if (cancellables.containsKey(uid)) {
            cancellables.remove(uid).cancel();
        }
        cancellables.put(uid, schedule(new StopGroupTyping(gid, uid), TYPING_TEXT_TIMEOUT));
    }

    @Verified
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

            context().getTypingModule()
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
            super.onReceive(message);
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
        private ApiTypingType type;

        public PrivateTyping(int uid, ApiTypingType type) {
            this.uid = uid;
            this.type = type;
        }

        public int getUid() {
            return uid;
        }

        public ApiTypingType getType() {
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
            result = 31 * result + type.getValue();
            return result;
        }
    }

    public static class GroupTyping {
        private int gid;
        private int uid;
        private ApiTypingType type;

        public GroupTyping(int gid, int uid, ApiTypingType type) {
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

        public ApiTypingType getType() {
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
            result = 31 * result + type.getValue();
            return result;
        }
    }
}