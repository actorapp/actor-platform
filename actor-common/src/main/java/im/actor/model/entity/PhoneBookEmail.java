package im.actor.model.entity;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class PhoneBookEmail {
    private long id;
    private String email;

    public PhoneBookEmail(long id, String email) {
        this.id = id;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
