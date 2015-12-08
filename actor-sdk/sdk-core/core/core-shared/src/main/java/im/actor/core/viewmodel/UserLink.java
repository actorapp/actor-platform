package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

public class UserLink {

    @Property("nonatomic, readonly")
    private String title;
    @Property("nonatomic, readonly")
    private String url;

    public UserLink(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserLink userWeb = (UserLink) o;

        if (title != null ? !title.equals(userWeb.title) : userWeb.title != null) return false;
        return !(url != null ? !url.equals(userWeb.url) : userWeb.url != null);

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
