package im.actor.runtime.generic.regexp;

import java.util.regex.Pattern;

import im.actor.runtime.regexp.MatcherCompat;
import im.actor.runtime.regexp.PatternCompat;

public class GenericPattern extends PatternCompat {

    private Pattern pattern;

    public GenericPattern(String pattern) {
        super(pattern);
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public MatcherCompat matcher(String input) {
        return new GenericMatch(pattern.matcher(input), input);
    }
}
