package im.actor.core.modules.encryption;

import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.encryption.entity.OwnPrivateKey;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Future;
import im.actor.runtime.crypto.IntegrityException;
import im.actor.runtime.crypto.box.ActorBox;
import im.actor.runtime.crypto.box.ActorBoxKey;
import im.actor.runtime.crypto.ratchet.RatchetMessageKey;
import im.actor.runtime.crypto.ratchet.RatchetPrivateKey;
import im.actor.runtime.crypto.ratchet.RatchetPublicKey;
import im.actor.runtime.crypto.ratchet.RatchetRootChainKey;

public class EncryptedSessionChainActor extends ModuleActor {

    private static final String TAG = "EncryptedSessionChainActor";

    private byte[] masterKey;
    private OwnPrivateKey chainOwnPrivateKey;

    public EncryptedSessionChainActor(byte[] masterKey,
                                      OwnPrivateKey chainOwnPrivateKey,
                                      ModuleContext context) {
        super(context);

        this.masterKey = masterKey;
        this.chainOwnPrivateKey = chainOwnPrivateKey;
    }

    private void onDecrypt(byte[] header, byte[] cipherText, byte[] senderPublicKey, int messageIndex,
                           Future future) {

        byte[] rc = RatchetRootChainKey.makeRootChainKey(
                new RatchetPrivateKey(chainOwnPrivateKey.getKey()),
                new RatchetPublicKey(senderPublicKey),
                masterKey);

        ActorBoxKey ratchetMessageKey = RatchetMessageKey.buildKey(rc, messageIndex);

        byte[] plainText;
        try {
            plainText = ActorBox.openBox(header, cipherText, ratchetMessageKey);
            Log.d(TAG, "Plain Text");
        } catch (IntegrityException e) {
            Log.d(TAG, "Plain Text error");
            e.printStackTrace();
            future.onError(e);
            return;
        }
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof DecryptMessage) {
            DecryptMessage decryptMessage = (DecryptMessage) message;
            onDecrypt(decryptMessage.getHeader(), decryptMessage.getCipherText(),
                    decryptMessage.getSenderPublicKey(), decryptMessage.getMessageIndex(), future);
            return false;
        }
        return super.onAsk(message, future);
    }

    public static class DecryptMessage {

        private byte[] header;
        private byte[] cipherText;
        private byte[] senderPublicKey;
        private int messageIndex;

        public DecryptMessage(byte[] header, byte[] cipherText, byte[] senderPublicKey, int messageIndex) {
            this.header = header;
            this.cipherText = cipherText;
            this.senderPublicKey = senderPublicKey;
            this.messageIndex = messageIndex;
        }

        public byte[] getHeader() {
            return header;
        }

        public byte[] getCipherText() {
            return cipherText;
        }

        public byte[] getSenderPublicKey() {
            return senderPublicKey;
        }

        public int getMessageIndex() {
            return messageIndex;
        }
    }
}