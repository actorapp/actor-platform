package im.actor.runtime.regexp;

public interface MatcherCompat {

    boolean hasMatch();

    boolean matches();

    int groupCount();

    String group();

    String group(int index);

    int start();
}
