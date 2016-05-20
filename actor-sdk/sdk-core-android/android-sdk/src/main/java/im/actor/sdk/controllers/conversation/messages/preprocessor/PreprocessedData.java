package im.actor.sdk.controllers.conversation.messages.preprocessor;

import android.text.Spannable;

public class PreprocessedData {
    private final Spannable reactionsSpannable;

    protected PreprocessedData(Spannable reactionsSpannable) {
        this.reactionsSpannable = reactionsSpannable;
    }

    public Spannable getReactionsSpannable() {
        return reactionsSpannable;
    }
}
