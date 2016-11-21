package im.actor.sdk.controllers.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class ChatSettingsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fr_settings_chat, container, false);
        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
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
        ((TextView) res.findViewById(R.id.settings_send_by_enter_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.settings_set_by_enter_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());

        setupCheckbox(res, R.id.animationAutoPlay, R.id.animationAutoPlayCont, R.id.settings_animation_auto_play_title, () -> messenger().changeAnimationAutoPlayEnabled(!messenger().isAnimationAutoPlayEnabled()), () -> messenger().isAnimationAutoPlayEnabled());
        ((TextView) res.findViewById(R.id.settings_animation_auto_play_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());

        setupCheckbox(res, R.id.animationAutoDownload, R.id.animationAutoDownloadCont, R.id.settings_animation_download_title, () -> messenger().changeAnimationAutoDownloadEnabled(!messenger().isAnimationAutoDownloadEnabled()), () -> messenger().isAnimationAutoDownloadEnabled());
        setupCheckbox(res, R.id.imageAutoDownload, R.id.imageAutoDownloadCont, R.id.settings_image_download_title, () -> messenger().changeImageAutoDownloadEnabled(!messenger().isImageAutoDownloadEnabled()), () -> messenger().isImageAutoDownloadEnabled());
        setupCheckbox(res, R.id.videoAutoDownload, R.id.videoAutoDownloadCont, R.id.settings_video_download_title, () -> messenger().changeVideoAutoDownloadEnabled(!messenger().isVideoAutoDownloadEnabled()), () -> messenger().isVideoAutoDownloadEnabled());
        setupCheckbox(res, R.id.audioAutoDownload, R.id.audioAutoDownloadCont, R.id.settings_audio_download_title, () -> messenger().changeAudioAutoDownloadEnabled(!messenger().isAudioAutoDownloadEnabled()), () -> messenger().isAudioAutoDownloadEnabled());
        setupCheckbox(res, R.id.docAutoDownload, R.id.docAutoDownloadCont, R.id.settings_doc_download_title, () -> messenger().changeDocAutoDownloadEnabled(!messenger().isDocAutoDownloadEnabled()), () -> messenger().isDocAutoDownloadEnabled());

        return res;
    }

    protected void setupCheckbox(View root, int chbId, int contId, int titleId, OnClLstnr lstnr, Checker checker) {
        final CheckBox animationsAtoPlay = (CheckBox) root.findViewById(chbId);
        animationsAtoPlay.setChecked(checker.check());
        View.OnClickListener animListener = v -> {
            lstnr.onClick();
            animationsAtoPlay.setChecked(checker.check());
        };
        animationsAtoPlay.setOnClickListener(animListener);
        root.findViewById(contId).setOnClickListener(animListener);
        ((TextView) root.findViewById(titleId)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
    }

    private interface OnClLstnr {
        void onClick();
    }

    private interface Checker {
        boolean check();
    }
}
