package im.actor.model.files;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface InputFile {
    public boolean read(int fileOffset, byte[] data, int offset, int len);

    public boolean close();
}