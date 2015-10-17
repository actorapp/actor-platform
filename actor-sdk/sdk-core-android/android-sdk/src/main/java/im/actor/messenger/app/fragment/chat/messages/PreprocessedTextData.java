package im.actor.messenger.app.fragment.chat.messages;

import android.text.Spannable;

public class PreprocessedTextData extends PreprocessedData {

    private final String text;
    private final Spannable spannableString;

    public PreprocessedTextData(String text, Spannable spannableString) {
        this.text = text;
        this.spannableString = spannableString;
    }

    public String getText() {
        return text;
    }

    public Spannable getSpannableString() {
        return spannableString;
    }
}
