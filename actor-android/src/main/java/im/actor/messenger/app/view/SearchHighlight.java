package im.actor.messenger.app.view;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

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
}
