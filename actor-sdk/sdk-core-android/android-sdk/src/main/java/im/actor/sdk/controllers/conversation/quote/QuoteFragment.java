package im.actor.sdk.controllers.conversation.quote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

public class QuoteFragment extends BaseFragment {

    private TextView quoteText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_quote, container, false);
        res.setBackgroundColor(style.getMainBackgroundColor());
        quoteText = (TextView) res.findViewById(R.id.quote_text);
        res.findViewById(R.id.ib_close_quote).setOnClickListener(view -> onClosed());
        return res;
    }

    public void setText(String text, boolean isQuoted) {
        quoteText.setText(text);
        if (isQuoted) {
            quoteText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_editor_format_quote_gray), null, null, null);
        } else {
            quoteText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_content_create), null, null, null);
        }
    }

    private void onClosed() {
        Fragment parent = getParentFragment();
        if (parent instanceof QuoteCallback) {
            ((QuoteCallback) parent).onQuoteCancelled();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        quoteText = null;
    }
}
