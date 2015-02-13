package im.actor.messenger.core.actors.api;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;

import im.actor.api.parser.Update;
import im.actor.api.scheme.DifferenceUpdate;
import im.actor.api.scheme.Group;
import im.actor.api.scheme.PeerType;
import im.actor.api.scheme.User;
import im.actor.api.scheme.parser.UpdatesParser;
import im.actor.api.scheme.rpc.ResponseGetDifference;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.updates.UpdateContactRegistered;
import im.actor.api.scheme.updates.UpdateContactsAdded;
import im.actor.api.scheme.updates.UpdateEncryptedMessage;
import im.actor.api.scheme.updates.UpdateGroupInvite;
import im.actor.api.scheme.updates.UpdateGroupUserAdded;
import im.actor.api.scheme.updates.UpdateGroupUserKick;
import im.actor.api.scheme.updates.UpdateGroupUserLeave;
import im.actor.api.scheme.updates.UpdateMessage;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.LogTag;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.core.actors.updates.UpdateBroker;
import im.actor.messenger.core.actors.users.UserActor;
import im.actor.messenger.storage.SequenceStorage;
import im.actor.messenger.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static im.actor.messenger.core.Core.auth;
import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 01.09.14.
 */
public class SequenceActor extends Actor {
    private static final String TAG = LogTag.SEQUENCE;
    private static final String TAG_PREFIX = "sequencer# ";
    private static final boolean LOG_ENABLED = false;

    public static ActorSelection sequence() {
        return new ActorSelection(Props.create(SequenceActor.class), "sequence");
    }

    private boolean isInvalidated;
    private HashMap<Integer, Object> further = new HashMap<Integer, Object>();
    private SequenceStorage updateState;
    private UpdatesParser updatesParser;

    @Override
    public void preStart() {
        updateState = new SequenceStorage(AppContext.getContext());
        updatesParser = new UpdatesParser();
    }

    @Override
    public void onReceive(Object message) {
        if (!auth().isAuthorized()) {
            return;
        }

        if (message instanceof Invalidate) {
            invalidate();
        } else if (message instanceof SeqUpdate) {
            onReceiveUpdate((SeqUpdate) message);
        } else if (message instanceof ExternalSequence) {
            onExternalSeq(((ExternalSequence) message).getSeq());
        } else if (message instanceof Reset) {
            updateState.dropState();
            isInvalidated = false;
            further.clear();
        }
    }

    public void onExternalSeq(int seq) {
        if (isInvalidated) {
            return;
        }


        if (seq <= updateState.getState().getSeq()) {
            Logger.d(TAG, TAG_PREFIX + "Received external seq is too small");
            return;
        }


        if (seq > updateState.getState().getSeq() + 1) {
            self().sendOnce(new InvalidateForced(), 2 * 1000L);
            Logger.d(TAG, TAG_PREFIX + "Forcing sequence invalidation by external {seq:" + seq + "}");
        } else {
            if (LOG_ENABLED) {
                Logger.d(TAG, TAG_PREFIX + "Received external {seq:" + seq + "}");

            }
        }
    }


    public void onReceiveUpdate(SeqUpdate message) {
        int seq = message.seq;
        byte[] state = message.state;


        if (seq <= updateState.getState().getSeq()) {
            if (LOG_ENABLED) {
                Logger.d(TAG, TAG_PREFIX + "Ignored SeqUpdate {seq:" + seq + "}");
            }
            return;
        }
        if (LOG_ENABLED) {
            Logger.d(TAG, TAG_PREFIX + "SeqUpdate {seq:" + seq + "}");
        }

        if (isInvalidated) {
            if (LOG_ENABLED) {
                Logger.d(TAG, TAG_PREFIX + "caching in further map");
            }
            further.put(seq, message);
            return;
        }

        if (!isValidSeq(seq)) {
            Logger.w(TAG, TAG_PREFIX + "Out of sequence: starting timer for invalidation");
            further.put(seq, message);
            self().sendOnce(new InvalidateForced(), 2 * 1000L);
            return;
        }

        if (causesInvalidation(message.update)) {
            Logger.w(TAG, TAG_PREFIX + "Message causes invalidation");
            invalidate();
            return;
        }

        if (LOG_ENABLED) {
            Logger.d(TAG, "Processing message");
        }

        if (message instanceof SeqFatUpdate) {
            SeqFatUpdate fatSeqUpdate = (SeqFatUpdate) message;
            if (fatSeqUpdate.users.size() > 0) {
                UserActor.userActor().onUpdateUsers(fatSeqUpdate.users);
            } else if (fatSeqUpdate.groups.size() > 0) {
                GroupsActor.groupUpdates().onUpdateGroups(fatSeqUpdate.groups);
            }
        }

        system().actorOf(UpdateBroker.sequenceBroker()).send(message.update);

        updateState.setState(seq, state);
        if (LOG_ENABLED) {
            Logger.d(TAG, TAG_PREFIX + "Saved state {seq:" + seq + "}");
        }

        validated();

        self().sendOnce(new InvalidateForced(), 24 * 60 * 60 * 1000L);
    }

    public void onDifference(ResponseGetDifference diff) {
        Logger.d(TAG, TAG_PREFIX + "Received diff with " + diff.getUsers().size() + " users and " + diff.getUpdates().size() + " updates");

        if (diff.getUsers().size() > 0) {
            UserActor.userActor().onUpdateUsers(diff.getUsers());
        }
        if (diff.getGroups().size() > 0) {
            GroupsActor.groupUpdates().onUpdateGroups(diff.getGroups());
        }
        if (diff.getUpdates().size() > 0) {
            ArrayList<Update> updates = new ArrayList<Update>();
            for (DifferenceUpdate u : diff.getUpdates()) {
                try {
                    updates.add(updatesParser.read(u.getUpdateHeader(), u.getUpdate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (updates.size() > 0) {
                system().actorOf(UpdateBroker.sequenceBroker()).send(updates);
            }
        }

        updateState.setState(diff.getSeq(), diff.getState());
        if (LOG_ENABLED) {
            Logger.d(TAG, TAG_PREFIX + "Saved state {seq:" + diff.getSeq() + "}");
        }
        self().sendOnce(new InvalidateForced(), 24 * 60 * 60 * 1000L);
    }

    public void reinvalidate() {
        isInvalidated = false;
        invalidate();
    }

    public void invalidate() {
        if (isInvalidated) {
            return;
        }
        isInvalidated = true;

        if (updateState.getState().getSeq() < 0) {
            Logger.w(TAG, TAG_PREFIX + "Loading initial sequence state");
            ask(requests().getState(), new FutureCallback<ResponseSeq>() {
                @Override
                public void onResult(ResponseSeq result) {
                    if (!isInvalidated) {
                        return;
                    }
                    Logger.w(TAG, TAG_PREFIX + "State received {seq:" + result.getSeq() + "}");
                    updateState.setState(result.getSeq(), result.getState());
                    validated();
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!isInvalidated) {
                        return;
                    }
                    Logger.w(TAG, TAG_PREFIX + "State load error: trying again");
                    // Just try again
                    reinvalidate();
                }
            });
        } else {
            Logger.w(TAG, TAG_PREFIX + "Performing get difference {seq:" + updateState.getState().getSeq() + "}");
            ask(requests().getDifference(updateState.getState().getSeq(),
                    updateState.getState().getState()), new FutureCallback<ResponseGetDifference>() {
                @Override
                public void onResult(ResponseGetDifference result) {
                    if (!isInvalidated) {
                        return;
                    }
                    Logger.w(TAG, TAG_PREFIX + "Diff received");
                    onDifference(result);
                    if (result.needMore()) {
                        reinvalidate();
                    } else {
                        validated();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (!isInvalidated) {
                        return;
                    }
                    Logger.w(TAG, TAG_PREFIX + "Diff load error: trying again");
                    // Just try again
                    reinvalidate();
                }
            });
        }
    }

    public void validated() {
        isInvalidated = false;
        for (int i = updateState.getState().getSeq() + 1; ; i++) {
            if (further.containsKey(i)) {
                onReceive(further.remove(i));
            } else {
                break;
            }
        }
        further.clear();
    }

    private boolean isValidSeq(final int seq) {
        return updateState.getState().getSeq() <= 0 || seq == updateState.getState().getSeq() + 1;
    }

    private boolean causesInvalidation(Update update) {

        if (update instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) update;

            if (users().get(updateMessage.getSenderUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateMessage unknown sender {uid:" + updateMessage.getSenderUid() + "}");
                return true;
            }

            if (updateMessage.getPeer().getType() == PeerType.GROUP) {
                if (groups().get(updateMessage.getPeer().getId()) == null) {
                    Logger.d(TAG, TAG_PREFIX + "UpdateMessage unknown peer {group:" + updateMessage.getPeer().getId() + "}");
                    return true;
                }
            }
            if (updateMessage.getPeer().getType() == PeerType.PRIVATE) {
                if (users().get(updateMessage.getPeer().getId()) == null) {
                    Logger.d(TAG, TAG_PREFIX + "UpdateMessage unknown peer {uid:" + updateMessage.getPeer().getId() + "}");
                    return true;
                }
            }
        } else if (update instanceof UpdateEncryptedMessage) {
            UpdateEncryptedMessage updateMessage = (UpdateEncryptedMessage) update;

            if (users().get(updateMessage.getSenderUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateEncryptedMessage unknown sender {uid:" + updateMessage.getSenderUid() + "}");
                return true;
            }

            if (updateMessage.getPeer().getType() == PeerType.GROUP) {
                if (groups().get(updateMessage.getPeer().getId()) == null) {
                    Logger.d(TAG, TAG_PREFIX + "UpdateEncryptedMessage unknown peer {group:" + updateMessage.getPeer().getId() + "}");
                    return true;
                }
            }
            if (updateMessage.getPeer().getType() == PeerType.PRIVATE) {
                if (users().get(updateMessage.getPeer().getId()) == null) {
                    Logger.d(TAG, TAG_PREFIX + "UpdateEncryptedMessage unknown peer {uid:" + updateMessage.getPeer().getId() + "}");
                    return true;
                }
            }
        } else if (update instanceof UpdateContactRegistered) {
            UpdateContactRegistered contactRegistered = (UpdateContactRegistered) update;
            if (users().get(contactRegistered.getUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateContactRegistered unknown user {uid:" + contactRegistered.getUid() + "}");
                return true;
            }
        } else if (update instanceof UpdateGroupInvite) {
            UpdateGroupInvite groupInvite = (UpdateGroupInvite) update;
            if (users().get(groupInvite.getInviteUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupInvite unknown inviter {uid:" + groupInvite.getInviteUid() + "}");
                return true;
            }
            if (groups().get(groupInvite.getGroupId()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupInvite unknown group {group:" + groupInvite.getGroupId() + "}");
                return true;
            }
        } else if (update instanceof UpdateGroupUserAdded) {
            UpdateGroupUserAdded added = (UpdateGroupUserAdded) update;
            if (users().get(added.getInviterUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown inviter {group:" + added.getInviterUid() + "}");
                return true;
            }
            if (users().get(added.getUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown added {uid:" + added.getUid() + "}");
                return true;
            }
            if (groups().get(added.getGroupId()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown group {uid:" + added.getGroupId() + "}");
                return true;
            }
        } else if (update instanceof UpdateGroupUserKick) {
            UpdateGroupUserKick added = (UpdateGroupUserKick) update;
            if (users().get(added.getKickerUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown kicker {uid:" + added.getKickerUid() + "}");
                return true;
            }
            if (users().get(added.getUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown kicked {uid:" + added.getUid() + "}");
                return true;
            }
            if (groups().get(added.getGroupId()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserKick unknown group {uid:" + added.getGroupId() + "}");
                return true;
            }
        } else if (update instanceof UpdateGroupUserLeave) {
            UpdateGroupUserLeave added = (UpdateGroupUserLeave) update;
            if (users().get(added.getUid()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserAdded unknown user {uid:" + added.getUid() + "}");
                return true;
            }
            if (groups().get(added.getGroupId()) == null) {
                Logger.d(TAG, TAG_PREFIX + "UpdateGroupUserLeave unknown group {uid:" + added.getGroupId() + "}");
                return true;
            }
        } else if (update instanceof UpdateContactsAdded) {
            for (int u : ((UpdateContactsAdded) update).getUids()) {
                if (users().get(u) == null) {
                    Logger.d(TAG, TAG_PREFIX + "UpdateContactsAdded unknown user {uid:" + u + "}");
                    return true;
                }
            }
        }
        return false;
    }

    public static class Invalidate {

    }

    public static class InvalidateForced extends Invalidate {

    }

    public static class Reset {

    }

    public static class SeqUpdate {
        private int seq;
        private byte[] state;
        private Update update;

        public SeqUpdate(int seq, byte[] state, Update update) {
            this.seq = seq;
            this.state = state;
            this.update = update;
        }
    }

    public static class SeqFatUpdate extends SeqUpdate {
        private List<User> users;
        private List<Group> groups;

        public SeqFatUpdate(int seq, byte[] state, Update update, List<User> users, List<Group> groups) {
            super(seq, state, update);
            this.users = users;
            this.groups = groups;
        }
    }

    public static class ExternalSequence {
        private int seq;

        public ExternalSequence(int seq) {
            this.seq = seq;
        }

        public int getSeq() {
            return seq;
        }
    }
}
