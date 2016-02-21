package im.actor.core.modules.calls.entity;

import im.actor.core.api.ApiPeerSettings;
import im.actor.runtime.collections.ManagedList;

/**
 * Helper Class for managing current call devices
 */
public class MasterCallManager {

    private ManagedList<MasterCallDevice> connectedDevices;
    private ManagedList<MasterCallMember> connectedMembers;
    private boolean isAnswered = false;

    public MasterCallManager() {
        this.connectedDevices = ManagedList.of();
        this.connectedMembers = ManagedList.of();
    }

    public ManagedList<MasterCallDevice> getConnectedDevices() {
        return connectedDevices;
    }

    public ManagedList<MasterCallMember> getConnectedMembers() {
        return connectedMembers;
    }

    /**
     * Adding Member to call
     *
     * @param uid   uid for adding
     * @param state initial state
     * @return is member was added
     */
    public boolean addMember(int uid, MasterCallMemberState state) {
        if (connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .isEmpty()) {

            connectedMembers.add(new MasterCallMember(uid, state));
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
        return connectedMembers.removeAll(connectedMembers.filter(MasterCallMember.PREDICATE(uid)));
    }

    /**
     * Called When Device is connected
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if device was connected
     */
    public boolean onDeviceConnected(int uid, long deviceId) {

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }

        //
        // Protection from double connected events
        //
        if (!connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .isEmpty()) {
            return false;
        }

        //
        // Adding new connected device with isActive = false
        //
        connectedDevices.add(new MasterCallDevice(uid, deviceId));

        //
        // Update Member State
        //
        if (member.getState() == MasterCallMemberState.RINGING) {
            member.setState(MasterCallMemberState.RINGING_REACHED);
        }

        return true;
    }

    /**
     * Called When device is disconnected
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if device was disconnected
     */
    public boolean onDeviceDisconnected(int uid, long deviceId) {

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }

        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        //
        // Removing Connected Device from list
        //
        connectedDevices.remove(device);

        //
        // If device was answered - update member state
        //
        if (device.isAnswered()) {
            boolean isConnected = false;
            for (MasterCallDevice m : connectedDevices
                    .filter(MasterCallDevice.PREDICATE(uid))) {
                if (m.isAnswered()) {
                    isConnected = true;
                    break;
                }
            }
            if (!isConnected) {
                member.setState(MasterCallMemberState.ENDED);
            }
        }

        return true;
    }


    /**
     * Called When device is disconnected
     *
     * @param uid          User Id
     * @param deviceId     Device Id
     * @param peerSettings Peer Settings
     * @return if device was disconnected
     */
    public boolean onDeviceAdvertised(int uid, long deviceId, ApiPeerSettings peerSettings) {

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }

        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        if (device.getDeviceState() != MasterCallDeviceState.PENDING) {
            return false;
        }

        device.setDeviceState(MasterCallDeviceState.ADVERTISED);
        device.setPeerSettings(peerSettings);

        return true;
    }

    /**
     * Call To start silent connection
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return is silent connection need to be started
     */
    public boolean startSilentConnection(int uid, long deviceId) {
        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        switch (device.getDeviceState()) {
            case ADVERTISED:
                if (device.getPeerSettings() != null) {
                    if (device.getPeerSettings().canConnect() != null) {
                        if (device.getPeerSettings().canConnect()) {
                            device.setDeviceState(MasterCallDeviceState.CONNECTING_SILENCED);
                            return true;
                        }
                    }
                }
        }

        return false;
    }

    /**
     * Checking if connection silently connected
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return is silent connection opened
     */
    public boolean isStartedSilently(int uid, long deviceId) {
        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        switch (device.getDeviceState()) {
            case SILENCED:
            case CONNECTING_SILENCED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Getting Peer settings if available
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return peer settings if available
     */
    public ApiPeerSettings getPeerSettings(int uid, long deviceId) {
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return null;
        }
        return device.getPeerSettings();
    }

    /**
     * Called When Device is Answered
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if device was answered
     */
    public boolean onDeviceAnswered(int uid, long deviceId, boolean supportSilence) {

        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }

        //
        // Update Device States
        //
        switch (device.getDeviceState()) {
            case PENDING:
            case ADVERTISED:
            case CONNECTING_SILENCED:
                device.setDeviceState(MasterCallDeviceState.CONNECTING);
                break;
            case SILENCED:
                device.setDeviceState(MasterCallDeviceState.IN_PROGRESS);
                break;
            case IN_PROGRESS:
            case CONNECTING:
                return false;
        }

        //
        // Update User State
        //
        // TODO: Check State for some corner states
        if (member.getState() == MasterCallMemberState.RINGING
                || member.getState() == MasterCallMemberState.RINGING_REACHED) {

            switch (device.getDeviceState()) {
                case PENDING:
                case ADVERTISED:
                case CONNECTING:
                case CONNECTING_SILENCED:
                    member.setState(MasterCallMemberState.CONNECTING);
                    break;
                case IN_PROGRESS:
                case SILENCED:
                    member.setState(MasterCallMemberState.IN_PROGRESS);
                    break;
            }
        }

        return true;
    }

    /**
     * Called when device reject call
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if device was rejected
     */
    public boolean onDeviceRejected(int uid, long deviceId) {

        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }

        //
        // If device already answered - ignore call rejection
        //
        switch (device.getDeviceState()) {
            case IN_PROGRESS:
            case CONNECTING:
                return false;
        }

        //
        // Mark Member as rejected member
        //
        switch (member.getState()) {
            case RINGING_REACHED:
            case RINGING:
                member.setState(MasterCallMemberState.ENDED);
                break;
        }

        return true;
    }

    /**
     * Called When new stream is added to device
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if this is a first stream in this call
     */
    public boolean onDeviceStreamAdded(int uid, long deviceId) {

        //
        // Searching for connected device
        //
        MasterCallDevice device = connectedDevices
                .filter(MasterCallDevice.PREDICATE(uid, deviceId))
                .firstOrNull();
        if (device == null) {
            return false;
        }

        //
        // Searching for member
        //
        MasterCallMember member = connectedMembers
                .filter(MasterCallMember.PREDICATE(uid))
                .firstOrNull();
        if (member == null) {
            return false;
        }


        //
        // Update Member State
        //
        switch (member.getState()) {
            case CONNECTING:
                member.setState(MasterCallMemberState.IN_PROGRESS);
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

    @Override
    public String toString() {
        String res = "Devices: \n";
        for (MasterCallDevice device : connectedDevices) {
            res += device.getUid() + "-" + device.getDeviceId() + ": " + device.getDeviceState() + "\n";
        }
        res += "Members: \n";
        for (MasterCallMember member : connectedMembers) {
            res += member.getUid() + " - " + member.getState() + "\n";
        }
        return res;
    }
}