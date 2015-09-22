package im.actor.runtime.regexp;

public interface MatcherCompat {
    public abstract boolean matches();

    public abstract int groupCount();

    public abstract String group();

    public abstract String group(int index);

    public abstract int start();
}
