package im.actor.sdk.controllers.conversation.messages.preprocessor;

import android.text.Spannable;

public class PreprocessedTextData extends PreprocessedData {

    private final String text;
    private final Spannable spannableString;

    public PreprocessedTextData(Spannable reactions, String text, Spannable spannableString) {
        super(reactions);
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
