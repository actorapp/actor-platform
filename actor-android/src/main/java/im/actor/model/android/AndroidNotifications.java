package im.actor.model.android;

import android.media.AudioManager;
import android.media.SoundPool;

import java.util.List;

import im.actor.messenger.R;
import im.actor.messenger.core.AppContext;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.Notification;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class AndroidNotifications implements NotificationProvider {

    private SoundPool soundPool;
    private int soundId;

    public AndroidNotifications() {
        soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        soundId = soundPool.load(AppContext.getContext(), R.raw.notification, 1);
    }

    @Override
    public void onMessageArriveInApp() {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    @Override
    public void onNotification(List<Notification> topNotifications, int messagesCount, int conversationsCount) {

    }
}
