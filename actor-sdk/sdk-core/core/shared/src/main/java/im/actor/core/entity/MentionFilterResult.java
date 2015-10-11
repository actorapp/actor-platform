package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.util.List;

import im.actor.core.util.StringMatch;

public class MentionFilterResult {

    @Property("readonly, nonatomic")
    private int uid;
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    private String mentionString;
    @Property("readonly, nonatomic")
    private List<StringMatch> mentionMatches;
    @Property("readonly, nonatomic")
    private String originalString;
    @Property("readonly, nonatomic")
    private List<StringMatch> originalMatches;
    @Property("readonly, nonatomic")
    private boolean isNickname;

    public MentionFilterResult(int uid, Avatar avatar, String mentionString, List<StringMatch> mentionMatches,
                               String originalString, List<StringMatch> originalMatches, boolean isNickname) {
        this.uid = uid;
        this.avatar = avatar;
        this.mentionString = mentionString;
        this.originalString = originalString;
        this.isNickname = isNickname;
        this.mentionMatches = mentionMatches;
        this.originalMatches = originalMatches;
    }

    public int getUid() {
        return uid;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public List<StringMatch> getMentionMatches() {
        return mentionMatches;
    }

    public List<StringMatch> getOriginalMatches() {
        return originalMatches;
    }

    public String getMentionString() {
        return mentionString;
    }

    public String getOriginalString() {
        return originalString;
    }

    public boolean isNickname() {
        return isNickname;
    }
}
