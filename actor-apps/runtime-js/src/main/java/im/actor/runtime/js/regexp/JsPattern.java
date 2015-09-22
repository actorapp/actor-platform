package im.actor.runtime.js.regexp;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import im.actor.runtime.regexp.MatcherCompat;
import im.actor.runtime.regexp.PatternCompat;

public class JsPattern extends PatternCompat {
    private RegExp compiled;

    public JsPattern(String pattern) {
        super(pattern);
        this.compiled = RegExp.compile(pattern);
    }

    public JsPattern(String pattern, String flags) {
        super(pattern, flags);
        this.compiled = RegExp.compile(pattern, flags);
    }

    @Override
    public boolean test(String input) {
        return compiled.test(input);
    }

    @Override
    public MatcherCompat matcher(String input) {
        MatchResult matchResult = compiled.exec(input);
        if (matchResult == null) {
            throw new RuntimeException("Called Matcher on a non-matching string");
        }
        return new JsMatcher(compiled.exec(input));
    }
}