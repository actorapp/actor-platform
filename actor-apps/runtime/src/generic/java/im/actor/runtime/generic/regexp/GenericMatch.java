package im.actor.runtime.generic.regexp;

import java.util.regex.Matcher;

import im.actor.runtime.regexp.MatcherCompat;

public class GenericMatch implements MatcherCompat {

    private Matcher matcher;
    private String input;

    public GenericMatch(Matcher matcher, String input) {
        this.matcher = matcher;
        this.input = input;
    }

    @Override
    public boolean hasMatch() {
        return this.matcher.find();
    }

    @Override
    public boolean matches() {
        return matcher.matches();
    }

    @Override
    public int groupCount() {
        return matcher.groupCount();
    }

    @Override
    public String group() {
        return matcher.group();
    }

    @Override
    public String group(int index) {
        return matcher.group(index);
    }

    @Override
    public int start() {
        return matcher.start();
    }
}
