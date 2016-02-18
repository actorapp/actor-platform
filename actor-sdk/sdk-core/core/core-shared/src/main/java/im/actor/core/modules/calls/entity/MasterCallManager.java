package im.actor.core.modules.calls.entity;

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
        connectedDevices.add(new MasterCallDevice(uid, deviceId, false));

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
        // If Device Was Active - update member state
        //
        if (device.isActive()) {
            boolean isConnected = false;
            for (MasterCallDevice m : connectedDevices
                    .filter(MasterCallDevice.PREDICATE(uid))) {
                if (m.isActive()) {
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
     * Called When Device is Answered
     *
     * @param uid      User Id
     * @param deviceId Device Id
     * @return if device was answered
     */
    public boolean onDeviceAnswered(int uid, long deviceId) {

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
        // Check if already answered
        //
        if (device.isActive()) {
            return false;
        }

        //
        // Mark Device as Active
        //
        device.setIsActive(true);

        //
        // Update User State
        //
        switch (member.getState()) {
            case RINGING:
            case RINGING_REACHED:
                member.setState(MasterCallMemberState.CONNECTING);
                break;
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
        // If device wasn't answered - ignore call rejection
        //
        if (device.isActive()) {
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
            res += device.getUid() + "-" + device.getDeviceId() + ": " + device.isActive() + "\n";
        }
        res += "Members: \n";
        for (MasterCallMember member : connectedMembers) {
            res += member.getUid() + " - " + member.getState();
        }
        return res;
    }
}