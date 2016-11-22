package im.actor.sdk.controllers.conversation.placeholder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

public class EmptyChatPlaceholder extends BaseFragment {

    private TextView emptyTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_chat_empty, container, false);
        emptyTitle = (TextView) res.findViewById(R.id.emptyHint);
        return res;
    }

    public void setText(String text) {
        emptyTitle.setText(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyTitle = null;
    }
}
