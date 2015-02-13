package im.actor.messenger.core.actors.groups;

import com.droidkit.mvvm.ValueModel;

import java.util.HashMap;

/**
 * Created by ex3ndr on 30.11.14.
 */
public class GroupAvatarState {

    private static final HashMap<Integer, ValueModel<StateHolder>> holders =
            new HashMap<Integer, ValueModel<StateHolder>>();

    public static ValueModel<StateHolder> getGroupState(int gid) {
        synchronized (holders) {
            if (!holders.containsKey(gid)) {
                holders.put(gid, new ValueModel<StateHolder>("avatar.group_" + gid, new StateHolder()));
            }
            return holders.get(gid);
        }
    }

    public static class StateHolder {
        private final String fileName;
        private final State state;

        public StateHolder() {
            this.fileName = null;
            this.state = State.NONE;
        }

        public StateHolder(String fileName, State state) {
            this.fileName = fileName;
            this.state = state;
        }

        public StateHolder(State state) {
            this.state = state;
            this.fileName = null;
        }

        public String getFileName() {
            return fileName;
        }

        public State getState() {
            return state;
        }
    }

    public enum State {
        NONE,
        UPLOADING,
        ERROR
    }
}
