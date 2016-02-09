package im.actor.crypto.ratchet;

public class RatchetMessage {
    private long senderEphermalId;
    private long receiverEphermalId;
    private byte[] senderEphermal;
    private byte[] receiverEphermal;
    private int messageIndex;
    private byte[] cipherBox;

    public RatchetMessage(long senderEphermalId,
                          long receiverEphermalId,
                          byte[] senderEphermal,
                          byte[] receiverEphermal,
                          int messageIndex,
                          byte[] cipherBox) {
        this.senderEphermalId = senderEphermalId;
        this.receiverEphermalId = receiverEphermalId;
        this.senderEphermal = senderEphermal;
        this.receiverEphermal = receiverEphermal;
        this.messageIndex = messageIndex;
        this.cipherBox = cipherBox;
    }

    public long getSenderEphermalId() {
        return senderEphermalId;
    }

    public long getReceiverEphermalId() {
        return receiverEphermalId;
    }

    public byte[] getSenderEphermal() {
        return senderEphermal;
    }

    public byte[] getReceiverEphermal() {
        return receiverEphermal;
    }

    public byte[] getCipherBox() {
        return cipherBox;
    }

    public int getMessageIndex() {
        return messageIndex;
    }
}
