package im.actor.messenger.core.actors.contacts.book;

/**
 * Created by ex3ndr on 11.09.14.
 */
public class PhoneBookEmail {
    private long id;
    private String email;

    public PhoneBookEmail(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhoneBookEmail that = (PhoneBookEmail) o;

        if (id != that.id) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}
