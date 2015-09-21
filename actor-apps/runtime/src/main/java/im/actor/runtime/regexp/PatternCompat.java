package im.actor.runtime.regexp;

public abstract class PatternCompat {
    public PatternCompat(String pattern) {}

    public PatternCompat(String pattern, String flags) {}

    public abstract MatcherCompat matcher(String input);

    public abstract boolean test(String input);
}