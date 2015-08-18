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
public class NotificationsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_notifications, container, false);

        // Conversation tone
        final CheckBox enableTones = (CheckBox) res.findViewById(R.id.enableConversationTones);
        enableTones.setChecked(messenger().isConversationTonesEnabled());
        View.OnClickListener enableTonesListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeConversationTonesEnabled(!messenger().isConversationTonesEnabled());
                enableTones.setChecked(messenger().isConversationTonesEnabled());
            }
        };
        enableTones.setOnClickListener(enableTonesListener);
        res.findViewById(R.id.conversationTonesCont).setOnClickListener(enableTonesListener);

        // Sound
        final CheckBox enableSound = (CheckBox) res.findViewById(R.id.enableSound);
        enableSound.setChecked(messenger().isNotificationSoundEnabled());
        View.OnClickListener enableSoundListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeNotificationSoundEnabled(!messenger().isNotificationSoundEnabled());
                enableSound.setChecked(messenger().isNotificationSoundEnabled());
            }
        };
        enableSound.setOnClickListener(enableSoundListener);
        res.findViewById(R.id.soundCont).setOnClickListener(enableSoundListener);

        // Vibration
        final CheckBox enableVibration = (CheckBox) res.findViewById(R.id.enableVibration);
        enableVibration.setChecked(messenger().isNotificationVibrationEnabled());
        View.OnClickListener enableVibrationListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeNotificationVibrationEnabled(!messenger().isNotificationVibrationEnabled());
                enableVibration.setChecked(messenger().isNotificationVibrationEnabled());
            }
        };
        enableVibration.setOnClickListener(enableVibrationListener);
        res.findViewById(R.id.vibrationCont).setOnClickListener(enableVibrationListener);

        // Group
        final CheckBox enableGroup = (CheckBox) res.findViewById(R.id.enableGroup);
        enableGroup.setChecked(messenger().isGroupNotificationsEnabled());
        View.OnClickListener enableGroupListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeGroupNotificationsEnabled(!messenger().isGroupNotificationsEnabled());
                enableGroup.setChecked(messenger().isGroupNotificationsEnabled());
            }
        };
        enableGroup.setOnClickListener(enableGroupListener);
        res.findViewById(R.id.groupCont).setOnClickListener(enableGroupListener);

        // Mentions
        final CheckBox enableGroupMentions = (CheckBox) res.findViewById(R.id.enableGroupMentions);
        enableGroupMentions.setChecked(messenger().isGroupNotificationsOnlyMentionsEnabled());
        View.OnClickListener enableGroupMentionsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeGroupNotificationsOnlyMentionsEnabled(!messenger().isGroupNotificationsOnlyMentionsEnabled());
                enableGroupMentions.setChecked(messenger().isGroupNotificationsOnlyMentionsEnabled());
            }
        };
        enableGroupMentions.setOnClickListener(enableGroupMentionsListener);
        res.findViewById(R.id.groupMentionsCont).setOnClickListener(enableGroupMentionsListener);

        // Names and messages

        final CheckBox enableText = (CheckBox) res.findViewById(R.id.enableTitles);
        enableText.setChecked(messenger().isShowNotificationsText());
        View.OnClickListener enableTextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeShowNotificationTextEnabled(!messenger().isShowNotificationsText());
                enableText.setChecked(messenger().isShowNotificationsText());
            }
        };
        enableText.setOnClickListener(enableTextListener);
        res.findViewById(R.id.titlesCont).setOnClickListener(enableTextListener);

        return res;
    }
}
