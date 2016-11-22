package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchResult {

    @NotNull
    @Property("readonly, nonatomic")
    private final Peer peer;
    @Nullable
    @Property("readonly, nonatomic")
    private final Avatar avatar;
    @NotNull
    @Property("readonly, nonatomic")
    private final String title;
    @Nullable
    @Property("readonly, nonatomic")
    private final String matchString;

    public SearchResult(@NotNull Peer peer, @Nullable Avatar avatar, @NotNull String title,
                        @Nullable String matchString) {
        this.peer = peer;
        this.avatar = avatar;
        this.title = title;
        this.matchString = matchString;
    }

    @NotNull
    public Peer getPeer() {
        return peer;
    }

    @Nullable
    public Avatar getAvatar() {
        return avatar;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getMatchString() {
        return matchString;
    }
}
