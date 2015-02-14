package im.actor.model.entity;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ContactRecord {
    private int id;
    private long accessHash;
    private int type;
    private String title;
    private String value;

    public ContactRecord(int id, long accessHash, int type, String title, String value) {
        this.id = id;
        this.accessHash = accessHash;
        this.type = type;
        this.title = title;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public long getAccessHash() {
        return accessHash;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }
}