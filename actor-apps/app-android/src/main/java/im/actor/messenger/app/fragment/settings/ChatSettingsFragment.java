package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;

import static im.actor.messenger.app.core.Core.messenger;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class ChatSettingsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_chat, container, false);

        final CheckBox sendByEnter = (CheckBox) res.findViewById(R.id.sendByEnter);
        sendByEnter.setChecked(messenger().isSendByEnterEnabled());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeSendByEnter(!messenger().isSendByEnterEnabled());
                sendByEnter.setChecked(messenger().isSendByEnterEnabled());
            }
        };
        sendByEnter.setOnClickListener(listener);
        res.findViewById(R.id.sendByEnterCont).setOnClickListener(listener);

        final CheckBox markDown = (CheckBox) res.findViewById(R.id.markdown);
        markDown.setChecked(messenger().isMarkdownEnabled());
        View.OnClickListener markDownListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeMarkdown(!messenger().isMarkdownEnabled());
                markDown.setChecked(messenger().isMarkdownEnabled());
            }
        };
        markDown.setOnClickListener(markDownListener);
        res.findViewById(R.id.markdownCont).setOnClickListener(markDownListener);

        return res;
    }
}
