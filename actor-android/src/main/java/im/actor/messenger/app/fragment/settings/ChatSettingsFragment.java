package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.settings.ChatSettings;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class ChatSettingsFragment extends BaseCompatFragment {

    private ChatSettings chatSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_chat, container, false);
        chatSettings = ChatSettings.getInstance();

        getBinder().bindChecked((CheckBox) res.findViewById(R.id.sendByEnter), chatSettings.sendByEnterValue());
        getBinder().bindOnClick(res.findViewById(R.id.sendByEnterCont), chatSettings.sendByEnterValue());
        getBinder().bindOnClick(res.findViewById(R.id.sendByEnter), chatSettings.sendByEnterValue());

        return res;
    }
}
