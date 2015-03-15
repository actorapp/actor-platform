package im.actor.model.files;

/**
 * Created by ex3ndr on 26.02.15.
 */
public interface FileSystemReference {

    public String getDescriptor();

    public boolean isExist();

    public int getSize();

    public OutputFile openWrite(int size);

    public InputFile openRead();
}