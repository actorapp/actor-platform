package im.actor.model.viewmodel;

/**
 * Created by ex3ndr on 25.02.15.
 */
public class UserPhone {
    private long phone;
    private String title;

    public UserPhone(long phone, String title) {
        this.phone = phone;
        this.title = title;
    }

    public long getPhone() {
        return phone;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPhone userPhone = (UserPhone) o;

        if (phone != userPhone.phone) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (phone ^ (phone >>> 32));
    }
}
