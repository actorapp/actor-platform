package im.actor.core.modules.calls.entity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.core.api.ApiPeerSettings;
import im.actor.runtime.collections.ManagedList;
import im.actor.runtime.function.Function;

/**
 * Helper Class for managing current call devices
 */
public class MasterCallManager {

    private ManagedList<MasterCallMember> members;
    private CallGrid callGrid;
    private boolean isInvalidated = false;
    private boolean isAnswered = false;

    public MasterCallManager() {
        this.callGrid = new CallGrid();
        this.members = ManagedList.of();
    }

    public CallGrid getCallGrid() {
        return callGrid;
    }

    public ManagedList<MasterCallMember> getMembers() {
        return members;
    }

    /**
     * Adding Member to call
     *
     * @param uid   uid for adding
     * @param state initial state
     * @return is member was added
     */
    public boolean addMember(int uid, MasterCallMemberState state) {
        if (members.filter(MasterCallMember.PREDICATE(uid))
                .isEmpty()) {

            members.add(new MasterCallMember(uid, state));
            return true;
        }
        return false;
    }

    /**
     * Removing Member to call
     *
     * @param uid uid for removing
     * @return is member was removed
     */
    public boolean removeMember(int uid) {
        return members.removeAll(members.filter(MasterCallMember.PREDICATE(uid)));
    }

    /**
     * Called When Device is connected
     *
     * @param uid      User Id
     * @param deviceId Device Id
     */
    public void onDeviceConnected(int uid, long deviceId) throws InvalidTransactionException {

        //
        // Protection from double connected nodes
        //
        if (hasNode(uid, deviceId)) {
            throw new InvalidTransactionException();
        }

        //
        // Searching for member
        //
        MasterCallMember member = getMember(uid);

        //
        // Adding new connected device with isActive = false
        //
        callGrid.addNode(new CallNode(member, deviceId));

        //
        // Update Member State
        //
        if (member.getState() == MasterCallMemberState.RINGING) {
            member.setState(MasterCallMemberState.RINGING_REACHED);
            isInvalidated = true;
        }
    }

    /**
     * Called When device is disconnected
     *
     * @param uid      User Id
     * @param deviceId Device Id
     */
    public ArrayList<CallNode> onDeviceDisconnected(final int uid, final long deviceId) throws InvalidTransactionException {

        //
        // Searching for node
        //
        CallNode node = getNode(uid, deviceId);

        //
        // Removing Connected Device from list
        //
        ArrayList<CallGridEdge> removedEdges = callGrid.removeNode(node);

        //
        // If device was answered - update member state
        //
        if (node.isAnswered()) {
            boolean isConnected = false;
            for (CallNode m : callGrid.getNodes(uid)) {
                if (m.isAnswered()) {
                    isConnected = true;
                    break;
                }
            }
            if (!isConnected) {
                if (node.getMember().getState() != MasterCallMemberState.ENDED) {
                    node.getMember().setState(MasterCallMemberState.ENDED);
                    isInvalidated = true;
                }
            }
        }

        //
        // Converting affected edges
        //
        return ManagedList.of(removedEdges)
                .map(new Function<CallGridEdge, CallNode>() {
                    @Override
                    public CallNode apply(CallGridEdge callGridEdge) {
                        CallNode endNode = callGridEdge.getEnd();
                        if (endNode.getMember().getUid() != uid && endNode.getDeviceId() != deviceId) {
                            return callGridEdge.getEnd();
                        } else {
                            return callGridEdge.getStart();
                        }
                    }
                });
    }


    /**
     * Called When device is disconnected
     *
     * @param uid          User Id
     * @param deviceId     Device Id
     * @param peerSettings Peer Settings
     */
    public void onDeviceAdvertised(int uid, long deviceId, ApiPeerSettings peerSettings) throws InvalidTransactionException {

        CallNode node = getNode(uid, deviceId);

        //
        // If is in pending state - save settings and update state
        //
        if (node.getDeviceState() != CallNodeState.PENDING) {
            throw new InvalidTransactionException();
        }
        node.setDeviceState(CallNodeState.ADVERTISED);
        node.setPeerSettings(peerSettings);
        isInvalidated = true;
    }

//    /**
//     * Call To start silent connection
//     *
//     * @param uid      User Id
//     * @param deviceId Device Id
//     * @return is silent connection need to be started
//     */
//    public boolean startSilentConnection(int uid, long deviceId) throws InvalidTransactionException {
//        //
//        // Searching for connected device
//        //
//        CallNode device = getNode(uid, deviceId);
//        if (device == null) {
//            return false;
//        }
//
//        switch (device.getDeviceState()) {
//            case ADVERTISED:
//                if (device.getPeerSettings() != null) {
//                    if (device.getPeerSettings().canConnect() != null) {
//                        if (device.getPeerSettings().canConnect()) {
//                            device.setDeviceState(CallNodeState.CONNECTING_SILENCED);
//                            return true;
//                        }
//                    }
//                }
//        }
//
//        return false;
//    }

//    /**
//     * Checking if connection silently connected
//     *
//     * @param uid      User Id
//     * @param deviceId Device Id
//     * @return is silent connection opened
//     */
//    public boolean isStartedSilently(int uid, long deviceId) throws InvalidTransactionException {
//        //
//        // Searching for connected device
//        //
//        CallNode node = getNode(uid, deviceId);
//
//        switch (node.getDeviceState()) {
//            case SILENCED:
//            case CONNECTING_SILENCED:
//                return true;
//            default:
//                return false;
//        }
//    }

//    /**
//     * Getting Peer settings if available
//     *
//     * @param uid      User Id
//     * @param deviceId Device Id
//     * @return peer settings if available
//     */
//    public ApiPeerSettings getPeerSettings(int uid, long deviceId) throws InvalidTransactionException {
//        return getNode(uid, deviceId).getPeerSettings();
//    }

    /**
     * Called When Device is Answered
     *
     * @param uid      User Id
     * @param deviceId Device Id
     */
    public void onDeviceAnswered(int uid, long deviceId) throws InvalidTransactionException {

        CallNode device = getNode(uid, deviceId);

        //
        // Update Device States
        //
        switch (device.getDeviceState()) {
            case PENDING:
            case ADVERTISED:
            case CONNECTING_SILENCED:
                device.setDeviceState(CallNodeState.CONNECTING);
                break;
            case SILENCED:
                device.setDeviceState(CallNodeState.IN_PROGRESS);
                break;
            case IN_PROGRESS:
            case CONNECTING:
                throw new InvalidTransactionException();
        }

        //
        // Update User State
        //
        // TODO: Check State for some corner states
        if (device.getMember().getState() == MasterCallMemberState.RINGING
                || device.getMember().getState() == MasterCallMemberState.RINGING_REACHED) {

            switch (device.getDeviceState()) {
                case PENDING:
                case ADVERTISED:
                case CONNECTING:
                case CONNECTING_SILENCED:
                    device.getMember().setState(MasterCallMemberState.CONNECTING);
                    isInvalidated = true;
                    break;
                case IN_PROGRESS:
                case SILENCED:
                    device.getMember().setState(MasterCallMemberState.IN_PROGRESS);
                    isInvalidated = true;
                    break;
            }
        }
    }

    /**
     * Called when device reject call
     *
     * @param uid      User Id
     * @param deviceId Device Id
     */
    public void onDeviceRejected(int uid, long deviceId) throws InvalidTransactionException {
        CallNode device = getNode(uid, deviceId);

        //
        // If device already answered - ignore call rejection
        //
        switch (device.getDeviceState()) {
            case IN_PROGRESS:
            case CONNECTING:
                throw new InvalidTransactionException();
        }

        //
        // Mark Member as rejected member
        //
        switch (device.getMember().getState()) {
            case RINGING_REACHED:
            case RINGING:
                device.getMember().setState(MasterCallMemberState.ENDED);
                isInvalidated = true;
                break;
        }
    }

    /**
     * Called When new stream is added to device
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if this is a first stream in this call
     */
    public boolean onDeviceStreamAdded(int uid, long deviceId) throws InvalidTransactionException {

        //
        // Searching for connected device
        //
        CallNode device = getNode(uid, deviceId);

        //
        // Update Device State
        //
        switch (device.getDeviceState()) {
            case CONNECTING:
                device.setDeviceState(CallNodeState.IN_PROGRESS);
                break;
            case CONNECTING_SILENCED:
                device.setDeviceState(CallNodeState.SILENCED);
                break;
        }

        //
        // Update Member State
        //
        switch (device.getDeviceState()) {
            case IN_PROGRESS:
                device.getMember().setState(MasterCallMemberState.IN_PROGRESS);
                isInvalidated = true;
                break;
        }

        //
        // Return True only for the first method invoke
        //
        if (isAnswered) {
            return false;
        }
        isAnswered = true;
        return true;
    }

    public boolean onInvalidated() {
        if (isInvalidated) {
            isInvalidated = false;
            return true;
        }
        return false;
    }

    @NotNull
    private MasterCallMember getMember(int uid) throws InvalidTransactionException {
        MasterCallMember member = members.firstOrNull(MasterCallMember.PREDICATE(uid));
        if (member == null) {
            throw new InvalidTransactionException();
        }
        return member;
    }

    @NotNull
    private CallNode getNode(int uid, long deviceId) throws InvalidTransactionException {
        CallNode node = callGrid.getNodes().firstOrNull(CallNode.PREDICATE(uid, deviceId));
        if (node == null) {
            throw new InvalidTransactionException();
        }
        return node;
    }

    private boolean hasNode(int uid, long deviceId) throws InvalidTransactionException {
        return callGrid.getNodes().firstOrNull(CallNode.PREDICATE(uid, deviceId)) != null;
    }

    @Override
    public String toString() {
        String res = "Devices: \n";
//        for (CallNode device : nodes) {
//            res += device.getUid() + "-" + device.getDeviceId() + ": " + device.getDeviceState() + "\n";
//        }
        res += "Members: \n";
        for (MasterCallMember member : members) {
            res += member.getUid() + " - " + member.getState() + "\n";
        }
        return res;
    }
}