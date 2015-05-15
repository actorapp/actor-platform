package im.actor.model.android.providers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.List;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.model.Messenger;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.users;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class AndroidNotifications implements NotificationProvider {

    private static final int NOTIFICATION_ID = 1;

    private SoundPool soundPool;
    private int soundId;

    private Peer visiblePeer;

    private Context context;

    public AndroidNotifications(Context context) {
        this.context = context;
        soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        soundId = soundPool.load(context, R.raw.notification, 1);
    }

    @Override
    public void onMessageArriveInApp(Messenger messenger) {
        soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    @Override
    public void onNotification(Messenger messenger, List<Notification> topNotifications, int messagesCount, int conversationsCount,
                               boolean silentUpdate, boolean isInApp) {

        // Android ignores isInApp argument because it is ok to send normal notification
        // instead in-app

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_app_notify);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);

        int defaults = NotificationCompat.DEFAULT_LIGHTS;
        if (messenger().isNotificationSoundEnabled()) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        if (messenger().isNotificationVibrationEnabled()) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        if (silentUpdate) {
            defaults = 0;
        }
        builder.setDefaults(defaults);

        // Wearable

//        builder.extend(new NotificationCompat.WearableExtender()
//                .setBackground(((BitmapDrawable) AppContext.getContext().getResources().getDrawable(R.drawable.wear_bg)).getBitmap())
//                .setHintHideIcon(true));

        final Notification topNotification = topNotifications.get(0);

        if (!silentUpdate) {
            builder.setTicker(getNotificationTextFull(topNotification));
        }

        final AvatarView avatar = new AvatarView(context);
        avatar.init(Screen.dp(42), 12);

        if (messagesCount == 1) {

            // Single message notification

            final String sender = getNotificationSender(topNotification);
            final CharSequence text = getNotificationText(topNotification);

            visiblePeer = topNotification.getPeer();

            avatar.bind(visiblePeer, new NotifaicationCallback(){

                @Override
                public void onAvatarLoaded() {
                    android.app.Notification result = builder
                            .setContentTitle(sender)
                            .setContentText(text)
                            .setLargeIcon(drawableToBitmap(avatar.getDrawable()))
                            .setContentIntent(PendingIntent.getActivity(context, 0,
                                    Intents.openDialog(topNotification.getPeer(), false, context),
                                    PendingIntent.FLAG_UPDATE_CURRENT))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(text))
                            .build();

                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, result);
                }
            });


        } else if (conversationsCount == 1) {

            // Single conversation notification

            String sender = getNotificationSender(topNotification);
            builder.setContentTitle(sender);
            builder.setContentText(messagesCount + " messages");
            visiblePeer = topNotification.getPeer();

            builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    Intents.openDialog(topNotification.getPeer(), false, context),
                    PendingIntent.FLAG_UPDATE_CURRENT));

            final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Notification n : topNotifications) {
                if (topNotification.getPeer().getPeerType() == PeerType.GROUP) {
                    inboxStyle.addLine(getNotificationTextFull(n));
                } else {
                    inboxStyle.addLine(getNotificationText(n));
                }
            }
            inboxStyle.setSummaryText(messagesCount + " messages");

            avatar.bind(visiblePeer, new NotifaicationCallback() {
                @Override
                public void onAvatarLoaded() {
                    android.app.Notification result = builder
                            .setLargeIcon(drawableToBitmap(avatar.getDrawable()))
                            .setStyle(inboxStyle)
                            .build();
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, result);
                }
            });


        } else {
            // Multiple conversations notification
            builder.setContentTitle(context.getString(R.string.app_name));
            builder.setContentText(messagesCount + " messages in " + conversationsCount + " chats");
            visiblePeer = null;

            builder.setContentIntent(PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT));

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Notification n : topNotifications) {
                inboxStyle.addLine(getNotificationTextFull(n));
            }
            inboxStyle.setSummaryText(messagesCount + " messages in " + conversationsCount + " chats");

            android.app.Notification result = builder
                    .setStyle(inboxStyle)
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, result);
        }


    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(Screen.dp(42), Screen.dp(42), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight()); drawable.draw(canvas); return bitmap;
    }


    @Override
    public void hideAllNotifications() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }


    private CharSequence getNotificationTextFull(Notification notification) {
        SpannableStringBuilder res = new SpannableStringBuilder();
        if (!messenger().getFormatter().isLargeDialogMessage(notification.getContentDescription().getContentType())) {
            res.append(getNotificationSender(notification));
            res.append(": ");
            res.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, res.length(), 0);
        }
        res.append(getNotificationText(notification));
        return res;
    }

    private String getNotificationSender(Notification pendingNotification) {
        String sender;
        if (pendingNotification.getPeer().getPeerType() == PeerType.GROUP) {
            sender = users().get(pendingNotification.getSender()).getName().get();
            sender += "@";
            sender += groups().get(pendingNotification.getPeer().getPeerId()).getName().get();
        } else {
            sender = users().get(pendingNotification.getSender()).getName().get();
        }
        return sender;
    }

    private CharSequence getNotificationText(Notification pendingNotification) {
        return messenger().getFormatter().formatContentDialogText(pendingNotification.getSender(),
                pendingNotification.getContentDescription().getContentType(),
                pendingNotification.getContentDescription().getText(),
                pendingNotification.getContentDescription().getRelatedUser());
    }

    public interface NotifaicationCallback{
        void onAvatarLoaded();
    }
}
