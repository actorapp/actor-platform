package im.actor.core.modules.encryption.ratchet.entity;

import java.util.List;

import im.actor.core.api.ApiEncryptedBox;
import im.actor.core.api.ApiKeyGroupId;

public class EncryptedMessage {

    private ApiEncryptedBox encryptedBox;
    private List<ApiKeyGroupId> ignoredGroups;

    public EncryptedMessage(ApiEncryptedBox encryptedBox, List<ApiKeyGroupId> ignoredGroups) {
        this.encryptedBox = encryptedBox;
        this.ignoredGroups = ignoredGroups;
    }

    public ApiEncryptedBox getEncryptedBox() {
        return encryptedBox;
    }

    public List<ApiKeyGroupId> getIgnoredGroups() {
        return ignoredGroups;
    }
}
