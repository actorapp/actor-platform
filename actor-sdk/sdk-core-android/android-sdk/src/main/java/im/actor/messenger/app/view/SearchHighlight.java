package im.actor.messenger.app.view;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.List;

import im.actor.core.util.StringMatch;
import im.actor.messenger.app.util.TextUtils;

/**
 * Created by ex3ndr on 19.10.14.
 */
public class SearchHighlight {
    public static Spannable highlightQuery(String src, String query, int color) {
        String matchString = src.toLowerCase();
        SpannableStringBuilder builder = new SpannableStringBuilder(src);
        if (matchString.startsWith(query)) {
            builder.setSpan(new ForegroundColorSpan(color), 0, query.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            int index = matchString.indexOf(" " + query);
            if (index >= 0) {
                builder.setSpan(new ForegroundColorSpan(color), index + 1, index + 1 + query.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return builder;
    }

    public static Spannable highlightMentionsQuery(String src, String query, int color) {
        String matchString = src.toLowerCase();
        SpannableStringBuilder builder = new SpannableStringBuilder(src);
        int index = matchString.indexOf(" " + query);
        if (matchString.startsWith(query) || TextUtils.transliterate(matchString).startsWith(query)) {
            builder.setSpan(new ForegroundColorSpan(color), 0, query.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else if (index >= 0) {
            builder.setSpan(new ForegroundColorSpan(color), index + 1, index + 1 + query.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (query.length() == 2) {
            builder.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            int spaceIndex = matchString.indexOf(" ");
            builder.setSpan(new ForegroundColorSpan(color), spaceIndex + 1, spaceIndex + 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return builder;
    }

    public static Spannable highlightMentionsQuery(String src, List<StringMatch> matches, int color) {

        SpannableStringBuilder builder = new SpannableStringBuilder(src);


        for (StringMatch sm : matches) {
            builder.setSpan(new ForegroundColorSpan(color), sm.getStart(), sm.getStart() + sm.getLength(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }


        return builder;
    }
}
