package im.actor.core.util;

import java.util.ArrayList;
import java.util.List;

public class StringMatcher {

    public static List<StringMatch> findMatches(String text, String query) {
        text = text.toLowerCase();
        query = query.toLowerCase();

        ArrayList<StringMatch> matches = new ArrayList<StringMatch>();

        if (text.startsWith(query)) {
            matches.add(new StringMatch(0, query.length()));
        }

        int index = text.indexOf(" " + query);
        if (index >= 0) {
            matches.add(new StringMatch(index + 1, query.length()));
        }

        return matches;
    }
}
