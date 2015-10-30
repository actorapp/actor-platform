package im.actor.sdk.controllers.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.fragment.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ChatSettingsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_chat, container, false);

        res.findViewById(R.id.dividerTop).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        res.findViewById(R.id.dividerBot).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

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
