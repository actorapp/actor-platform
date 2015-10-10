package im.actor.runtime.js.regexp;

import com.google.gwt.regexp.shared.MatchResult;

import im.actor.runtime.regexp.MatcherCompat;

public class JsMatcher implements MatcherCompat {
    private MatchResult matchResult;

    public JsMatcher(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

    @Override
    public boolean hasMatch() {
        return matchResult != null;
    }

    @Override
    public boolean matches() {
        return matchResult.getGroupCount() > 0;
    }

    @Override
    public int start() {
        return matchResult.getIndex();
    }

    @Override
    public int groupCount() {
        return matchResult.getGroupCount();
    }

    @Override
    public String group() {
        return group(0);
    }

    @Override
    public String group(int index) {
        return matchResult.getGroup(index);
    }
}