package im.actor.core.modules.auth;

public class AuthenticationBackupData {

    private byte[] deviceHash;
    private int authenticatedUid;
    private byte[] ownUserData;

    public AuthenticationBackupData(byte[] deviceHash, int authenticatedUid, byte[] ownUserData) {
        this.deviceHash = deviceHash;
        this.authenticatedUid = authenticatedUid;
        this.ownUserData = ownUserData;
    }

    public byte[] getDeviceHash() {
        return deviceHash;
    }

    public int getAuthenticatedUid() {
        return authenticatedUid;
    }

    public byte[] getOwnUserData() {
        return ownUserData;
    }
}
