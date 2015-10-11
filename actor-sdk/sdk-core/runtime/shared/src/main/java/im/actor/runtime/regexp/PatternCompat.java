package im.actor.runtime.regexp;

public abstract class PatternCompat {

    public PatternCompat(String pattern) {
    }

    public abstract MatcherCompat matcher(String input);
}