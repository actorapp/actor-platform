package im.actor.model.entity;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class PhoneBookPhone {
    private long id;
    private long number;

    public PhoneBookPhone(long id, long number) {
        this.id = id;
        this.number = number;
    }

    public long getId() {
        return id;
    }

    public long getNumber() {
        return number;
    }
}
