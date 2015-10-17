package im.actor.core.util;

import junit.framework.TestCase;

import java.util.List;

public class StringMatcherTest extends TestCase {

    public void testMatch() throws Exception {
        String text = "Andrew Smith";
        List<StringMatch> res = StringMatcher.findMatches(text, "An");
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).getStart());
        assertEquals(2, res.get(0).getLength());
    }
}