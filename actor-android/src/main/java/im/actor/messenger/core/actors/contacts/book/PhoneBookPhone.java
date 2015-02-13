package im.actor.messenger.core.actors.contacts.book;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class PhoneBookPhone {
    private long id;
    private long number;

    public PhoneBookPhone(long id, long number) {
        this.id = id;
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhoneBookPhone that = (PhoneBookPhone) o;

        if (id != that.id) return false;
        if (number != that.number) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (number ^ (number >>> 32));
        return result;
    }
}
