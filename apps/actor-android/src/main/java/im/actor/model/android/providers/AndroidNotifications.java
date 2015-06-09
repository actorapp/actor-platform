package im.actor.model.android.providers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.app.NotificationCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.List;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarPlaceholderDrawable;
import im.actor.model.Messenger;
import im.actor.model.NotificationProvider;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Notification;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.files.FileSystemReference;
import im.actor.model.viewmodel.FileVMCallback;

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

//    Bypass bypass;

    public AndroidNotifications(Context context) {
        this.context = context;
//        bypass = new Bypass(context);
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

        android.app.Notification result;

        if (messagesCount == 1) {

            // Single message notification

            final String sender = getNotificationSender(topNotification);

//            final CharSequence text = bypass.markdownToSpannable(messenger().getFormatter().formatNotificationText(topNotification), true).toString();
            final CharSequence text = messenger().getFormatter().formatNotificationText(topNotification);
            visiblePeer = topNotification.getPeer();

            Avatar avatar =null;
            int id = 0;
            String avatarTitle = "";
            switch (visiblePeer.getPeerType()){
                case PRIVATE:
                    avatar = users().get(visiblePeer.getPeerId()).getAvatar().get();
                    id = users().get(visiblePeer.getPeerId()).getId();
                    avatarTitle = users().get(visiblePeer.getPeerId()).getName().get();
                    break;

                case GROUP:
                    avatar = groups().get(visiblePeer.getPeerId()).getAvatar().get();
                    id = groups().get(visiblePeer.getPeerId()).getId();
                    avatarTitle = groups().get(visiblePeer.getPeerId()).getName().get();
                    break;
            }

            Drawable avatarDrawable = new AvatarPlaceholderDrawable(avatarTitle, id, 18, context);

            result = buildSingleMessageNotification(avatarDrawable, builder, sender, text, topNotification);

            final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, result);

            if(avatar!=null && avatar.getSmallImage()!=null && avatar.getSmallImage().getFileReference()!=null){
                messenger().bindFile(avatar.getSmallImage().getFileReference(), true, new FileVMCallback() {

                    @Override
                    public void onNotDownloaded() {
                    }

                    @Override
                    public void onDownloading(float progress) {
                    }

                    @Override
                    public void onDownloaded(FileSystemReference reference) {

                        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(context.getResources(), reference.getDescriptor());
                        d.setCornerRadius(d.getIntrinsicHeight()/2);
                        d.setAntiAlias(true);
                        android.app.Notification result = buildSingleMessageNotification(d, builder, sender, text, topNotification);
                        //NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(NOTIFICATION_ID, result);
                    }
                });
            }else{
                manager.notify(NOTIFICATION_ID, result);
            }


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
                    inboxStyle.addLine(messenger().getFormatter().formatNotificationText(n));
                }
            }
            inboxStyle.setSummaryText(messagesCount + " messages");
            Avatar avatar =null;
            int id = 0;
            String avatarTitle = "";
            switch (visiblePeer.getPeerType()){
                case PRIVATE:
                    avatar = users().get(visiblePeer.getPeerId()).getAvatar().get();
                    id = users().get(visiblePeer.getPeerId()).getId();
                    avatarTitle = users().get(visiblePeer.getPeerId()).getName().get();
                    break;

                case GROUP:
                    avatar = groups().get(visiblePeer.getPeerId()).getAvatar().get();
                    id = groups().get(visiblePeer.getPeerId()).getId();
                    avatarTitle = groups().get(visiblePeer.getPeerId()).getName().get();
                    break;
            }

            Drawable avatarDrawable = new AvatarPlaceholderDrawable(avatarTitle, id, 18, context);

            result = buildSingleConversationNotification(builder, inboxStyle, avatarDrawable);
            final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, result);

            if(avatar!=null && avatar.getSmallImage()!=null && avatar.getSmallImage().getFileReference()!=null){
                messenger().bindFile(avatar.getSmallImage().getFileReference(), true, new FileVMCallback() {

                    @Override
                    public void onNotDownloaded() {
                    }

                    @Override
                    public void onDownloading(float progress) {
                    }

                    @Override
                    public void onDownloaded(FileSystemReference reference) {
                        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(context.getResources(), reference.getDescriptor());
                        d.setCornerRadius(d.getIntrinsicHeight() / 2);
                        d.setAntiAlias(true);
                        android.app.Notification result = buildSingleConversationNotification(builder, inboxStyle, d);
                        manager.notify(NOTIFICATION_ID, result);
                    }
                });
            }else{
                manager.notify(NOTIFICATION_ID, result);
            }


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

           result = builder
                    .setStyle(inboxStyle)
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, result);
        }



    }

    private android.app.Notification buildSingleConversationNotification(NotificationCompat.Builder builder, NotificationCompat.InboxStyle inboxStyle, Drawable avatarDrawable) {

        return builder
                .setLargeIcon(drawableToBitmap(avatarDrawable))
                .setStyle(inboxStyle)
                .build();
    }

    private android.app.Notification buildSingleMessageNotification(Drawable d, NotificationCompat.Builder builder, String sender, CharSequence text, Notification topNotification) {
        return builder
            .setContentTitle(sender)
            .setContentText(text)
            .setLargeIcon(drawableToBitmap(d))
            .setContentIntent(PendingIntent.getActivity(context, 0,
                    Intents.openDialog(topNotification.getPeer(), false, context),
                    PendingIntent.FLAG_UPDATE_CURRENT))
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .build();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(height > 0 ? height : Screen.dp(42), height > 0 ? height : Screen.dp(42), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
//        res.append(bypass.markdownToSpannable(messenger().getFormatter().formatNotificationText(notification), true).toString());
        res.append(messenger().getFormatter().formatNotificationText(notification));
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


}
