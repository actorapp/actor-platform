package im.actor.model.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.List;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.core.AppContext;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.ContentDescription;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.core.Core.groups;
import static im.actor.messenger.core.Core.users;

/**
 * Created by ex3ndr on 01.03.15.
 */
public class AndroidNotifications implements NotificationProvider {

    private static final int NOTIFICATION_ID = 1;

    private SoundPool soundPool;
    private int soundId;

    private Peer visiblePeer;

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(AppContext.getContext());

        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_app_notify);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);

        // Wearable

//        builder.extend(new NotificationCompat.WearableExtender()
//                .setBackground(((BitmapDrawable) AppContext.getContext().getResources().getDrawable(R.drawable.wear_bg)).getBitmap())
//                .setHintHideIcon(true));

        Notification topNotification = topNotifications.get(0);

        builder.setTicker(getNotificationTextFull(topNotification));

        android.app.Notification result;
        if (messagesCount == 1) {

            // Single message notification

            String sender = getNotificationSender(topNotification);
            CharSequence text = getNotificationText(topNotification);

            visiblePeer = topNotification.getPeer();

            result = builder
                    .setContentTitle(sender)
                    .setContentText(text)
                    .setContentIntent(PendingIntent.getActivity(AppContext.getContext(), 0,
                            Intents.openDialog(topNotification.getPeer(), false, AppContext.getContext()),
                            PendingIntent.FLAG_ONE_SHOT))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(text))
                    .build();
        } else if (conversationsCount == 1) {

            // Single conversation notification

            String sender = getNotificationSender(topNotification);
            builder.setContentTitle(sender);
            builder.setContentText(messagesCount + " messages");
            visiblePeer = topNotification.getPeer();

            builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(), 0,
                    Intents.openDialog(topNotification.getPeer(), false, AppContext.getContext()),
                    PendingIntent.FLAG_ONE_SHOT));

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Notification n : topNotifications) {
                if (topNotification.getPeer().getPeerType() == PeerType.GROUP) {
                    inboxStyle.addLine(getNotificationTextFull(n));
                } else {
                    inboxStyle.addLine(getNotificationText(n));
                }
            }
            inboxStyle.setSummaryText(messagesCount + " messages");

            result = builder
                    .setStyle(inboxStyle)
                    .build();
        } else {
            // Multiple conversations notification
            builder.setContentTitle(AppContext.getContext().getString(R.string.app_name));
            builder.setContentText(messagesCount + " messages in " + conversationsCount + " chats");
            visiblePeer = null;

            builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(), 0,
                    new Intent(AppContext.getContext(), MainActivity.class),
                    PendingIntent.FLAG_ONE_SHOT));

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (Notification n : topNotifications) {
                inboxStyle.addLine(getNotificationTextFull(n));
            }
            inboxStyle.setSummaryText(messagesCount + " messages in " + conversationsCount + " chats");

            result = builder
                    .setStyle(inboxStyle)
                    .build();
        }

        NotificationManager manager = (NotificationManager) AppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, result);
    }

    @Override
    public void onDialogsOpen() {
        hideNotifications();
    }

    @Override
    public void onChatOpen(Peer peer) {
        if (visiblePeer != null && visiblePeer.equals(peer)) {
            hideNotifications();
            visiblePeer = null;
        }
    }

    private void hideNotifications() {
        NotificationManager manager = (NotificationManager) AppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    private CharSequence getNotificationTextFull(Notification notification) {
        SpannableStringBuilder res = new SpannableStringBuilder();
        res.append(getNotificationSender(notification));
        res.append(": ");
        res.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, res.length(), 0);
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
        // TODO: Better implementation
        ContentDescription description = pendingNotification.getContentDescription();
        switch (description.getContentType()) {
            default:
            case TEXT:
                return description.getText();
            case DOCUMENT_PHOTO:
                return "Photo";
            case DOCUMENT_VIDEO:
                return "Video";
            case DOCUMENT:
                if (description.getText() != null) {
                    return description.getText();
                }
                return "Document";
            case SERVICE_REGISTERED:
                return "User registered";
            case SERVICE_CREATED:
                return "Created group";
            case SERVICE_ADD:
                if (description.getRelatedUser() != 0) {
                    UserVM u = users().get(description.getRelatedUser());
                    if (u != null) {
                        return "Added " + u.getName() + " to group";
                    }
                }
                return "Added someone to group";
            case SERVICE_KICK:
                if (description.getRelatedUser() != 0) {
                    UserVM u = users().get(description.getRelatedUser());
                    if (u != null) {
                        return "Kicked " + u.getName() + " from group";
                    }
                }
                return "Kicked someone from group";
            case SERVICE_LEAVE:
                return "Left group";
        }
    }
}
