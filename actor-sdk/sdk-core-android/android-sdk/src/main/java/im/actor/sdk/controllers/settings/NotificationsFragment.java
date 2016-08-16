package im.actor.sdk.controllers.settings;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.util.ViewUtils;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class NotificationsFragment extends BaseFragment {

    public static int SOUND_PICKER_REQUEST_CODE = 122;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_notifications, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        // Conversation tone
        final CheckBox enableTones = (CheckBox) res.findViewById(R.id.enableConversationTones);
        enableTones.setChecked(messenger().isConversationTonesEnabled());
        View.OnClickListener enableTonesListener = v -> {
            messenger().changeConversationTonesEnabled(!messenger().isConversationTonesEnabled());
            enableTones.setChecked(messenger().isConversationTonesEnabled());
        };
        ActorStyle style = ActorSDK.sharedActor().style;
        ((TextView) res.findViewById(R.id.settings_conversation_tones_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_conversation_tones_hint)).setTextColor(style.getTextSecondaryColor());
        enableTones.setOnClickListener(enableTonesListener);
        res.findViewById(R.id.conversationTonesCont).setOnClickListener(enableTonesListener);

        // Vibration
        final CheckBox enableVibration = (CheckBox) res.findViewById(R.id.enableVibration);
        enableVibration.setChecked(messenger().isNotificationVibrationEnabled());
        View.OnClickListener enableVibrationListener = v -> {
            messenger().changeNotificationVibrationEnabled(!messenger().isNotificationVibrationEnabled());
            enableVibration.setChecked(messenger().isNotificationVibrationEnabled());
        };
        enableVibration.setOnClickListener(enableVibrationListener);
        res.findViewById(R.id.vibrationCont).setOnClickListener(enableVibrationListener);
        ((TextView) res.findViewById(R.id.settings_vibration_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_vibration_hint)).setTextColor(style.getTextSecondaryColor());

        // Group
        final CheckBox enableGroup = (CheckBox) res.findViewById(R.id.enableGroup);
        enableGroup.setChecked(messenger().isGroupNotificationsEnabled());
        View.OnClickListener enableGroupListener = v -> {
            messenger().changeGroupNotificationsEnabled(!messenger().isGroupNotificationsEnabled());
            enableGroup.setChecked(messenger().isGroupNotificationsEnabled());
        };
        enableGroup.setOnClickListener(enableGroupListener);
        res.findViewById(R.id.groupCont).setOnClickListener(enableGroupListener);
        ((TextView) res.findViewById(R.id.settings_group_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_group_hint)).setTextColor(style.getTextSecondaryColor());

        // Mentions
        final CheckBox enableGroupMentions = (CheckBox) res.findViewById(R.id.enableGroupMentions);
        enableGroupMentions.setChecked(messenger().isGroupNotificationsOnlyMentionsEnabled());
        View.OnClickListener enableGroupMentionsListener = v -> {
            messenger().changeGroupNotificationsOnlyMentionsEnabled(!messenger().isGroupNotificationsOnlyMentionsEnabled());
            enableGroupMentions.setChecked(messenger().isGroupNotificationsOnlyMentionsEnabled());
        };
        enableGroupMentions.setOnClickListener(enableGroupMentionsListener);
        res.findViewById(R.id.groupMentionsCont).setOnClickListener(enableGroupMentionsListener);
        ((TextView) res.findViewById(R.id.settings_group_mentions_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_group_mentions_hint)).setTextColor(style.getTextSecondaryColor());

        // Names and messages
        final CheckBox enableText = (CheckBox) res.findViewById(R.id.enableTitles);
        enableText.setChecked(messenger().isShowNotificationsText());
        View.OnClickListener enableTextListener = v -> {
            messenger().changeShowNotificationTextEnabled(!messenger().isShowNotificationsText());
            enableText.setChecked(messenger().isShowNotificationsText());
        };
        enableText.setOnClickListener(enableTextListener);
        res.findViewById(R.id.titlesCont).setOnClickListener(enableTextListener);
        ((TextView) res.findViewById(R.id.settings_titles_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_titles_hint)).setTextColor(style.getTextSecondaryColor());

        // Sound
        View soundPickerCont = res.findViewById(R.id.soundPickerCont);
        View soundPickerDivider = res.findViewById(R.id.divider);

        if (messenger().isNotificationSoundEnabled()) {
            ViewUtils.showViews(false, soundPickerCont, soundPickerDivider);
        } else {
            ViewUtils.goneViews(false, soundPickerCont, soundPickerDivider);
        }

        final CheckBox enableSound = (CheckBox) res.findViewById(R.id.enableSound);
        enableSound.setChecked(messenger().isNotificationSoundEnabled());
        View.OnClickListener enableSoundListener = v -> {
            messenger().changeNotificationSoundEnabled(!messenger().isNotificationSoundEnabled());
            enableSound.setChecked(messenger().isNotificationSoundEnabled());

            //show/hide sound picker
            if (messenger().isNotificationSoundEnabled()) {
                ViewUtils.showViews(soundPickerCont, soundPickerDivider);
            } else {
                ViewUtils.goneViews(soundPickerCont, soundPickerDivider);
            }
        };
        enableSound.setOnClickListener(enableSoundListener);
        res.findViewById(R.id.soundCont).setOnClickListener(enableSoundListener);
        ((TextView) res.findViewById(R.id.settings_sound_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_sound_hint)).setTextColor(style.getTextSecondaryColor());

        // Sound picker
        View.OnClickListener soundPickerListener = v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            Uri currentSound = null;
            String defaultPath = null;
            Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            if (defaultUri != null) {
                defaultPath = defaultUri.getPath();
            }

            String path = messenger().getPreferences().getString("globalNotificationSound");
            if (path == null) {
                path = defaultPath;
            }
            if (path != null && !path.equals("none")) {
                if (path.equals(defaultPath)) {
                    currentSound = defaultUri;
                } else {
                    currentSound = Uri.parse(path);
                }
            }
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSound);
            startActivityForResult(intent, SOUND_PICKER_REQUEST_CODE);
        };
        res.findViewById(R.id.soundPickerCont).setOnClickListener(soundPickerListener);
        ((TextView) res.findViewById(R.id.settings_sound_picker_title)).setTextColor(style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_sound_picker_hint)).setTextColor(style.getTextSecondaryColor());
        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SOUND_PICKER_REQUEST_CODE) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringtone != null) {
                messenger().getPreferences().putString("globalNotificationSound", ringtone.toString());
            } else {
                messenger().getPreferences().putString("globalNotificationSound", "none");
            }
        }
    }
}
