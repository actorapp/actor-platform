package im.actor.messenger.app.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.settings.NotificationSettings;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class NotificationSettingsFragment extends BaseCompatFragment {

    private NotificationSettings settings = NotificationSettings.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_notifications, container, false);

        // Conversation tone
        getBinder().bindChecked((CheckBox) res.findViewById(R.id.enableConversationTones), settings.inAppSoundValue());
        getBinder().bindOnClick(res.findViewById(R.id.enableConversationTones), settings.inAppSoundValue());
        getBinder().bindOnClick(res.findViewById(R.id.conversationTonesCont), settings.inAppSoundValue());

        // Sound
        getBinder().bindChecked((CheckBox) res.findViewById(R.id.enableSound), settings.soundValue());
        getBinder().bindOnClick(res.findViewById(R.id.enableSound), settings.soundValue());
        getBinder().bindOnClick(res.findViewById(R.id.soundCont), settings.soundValue());

        // Vibration
        getBinder().bindChecked((CheckBox) res.findViewById(R.id.enableVibration), settings.vibrateValue());
        getBinder().bindOnClick(res.findViewById(R.id.enableVibration), settings.vibrateValue());
        getBinder().bindOnClick(res.findViewById(R.id.vibrationCont), settings.vibrateValue());

        // Names and messages
        getBinder().bindChecked((CheckBox) res.findViewById(R.id.enableTitles), settings.showTitlesValue());
        getBinder().bindOnClick(res.findViewById(R.id.enableTitles), settings.showTitlesValue());
        getBinder().bindOnClick(res.findViewById(R.id.titlesCont), settings.showTitlesValue());

        return res;
    }
}
