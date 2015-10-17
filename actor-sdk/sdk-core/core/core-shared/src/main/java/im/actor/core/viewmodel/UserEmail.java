package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

public class UserEmail {

    @Property("nonatomic, readonly")
    private String email;
    @Property("nonatomic, readonly")
    private String title;

    public UserEmail(String email, String title) {
        this.email = email;
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEmail userEmail = (UserEmail) o;

        if (!email.equals(userEmail.email)) return false;
        return !(title != null ? !title.equals(userEmail.title) : userEmail.title != null);

    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
