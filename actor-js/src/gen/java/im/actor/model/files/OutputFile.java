package im.actor.model.files;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface OutputFile {
    public boolean write(int fileOffset, byte[] data, int dataOffset, int dataLen);

    public boolean close();
}
