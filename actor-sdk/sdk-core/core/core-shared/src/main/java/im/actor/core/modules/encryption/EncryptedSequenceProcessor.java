package im.actor.core.modules.encryption;

import im.actor.core.api.ApiEncryptedContent;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public interface EncryptedSequenceProcessor {
    Promise<Void> onUpdate(int senderId, long date, ApiEncryptedContent update);
}
