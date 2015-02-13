package im.actor.messenger.core.actors.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.typed.TypedActor;
import com.droidkit.actors.typed.TypedCreator;
import com.droidkit.engine._internal.util.SortedArrayList;
import com.droidkit.engine.persistence.BserMap;
import com.droidkit.engine.persistence.PersistenceSet;
import com.droidkit.engine.persistence.storage.SqliteStorage;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.settings.NotificationSettings;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.storage.scheme.messages.ConversationMessage;
import im.actor.messenger.storage.scheme.messages.types.AudioMessage;
import im.actor.messenger.storage.scheme.messages.types.DocumentMessage;
import im.actor.messenger.storage.scheme.messages.types.GroupAdd;
import im.actor.messenger.storage.scheme.messages.types.GroupCreated;
import im.actor.messenger.storage.scheme.messages.types.GroupKick;
import im.actor.messenger.storage.scheme.messages.types.GroupLeave;
import im.actor.messenger.storage.scheme.messages.types.PhotoMessage;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.storage.scheme.messages.types.UserAddedDeviceMessage;
import im.actor.messenger.storage.scheme.messages.types.UserRegisteredMessage;
import im.actor.messenger.storage.scheme.messages.types.VideoMessage;
import im.actor.messenger.util.RandomUtil;

import java.util.*;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 07.09.14.
 */
public class NotificationsActor extends TypedActor<NotificationsInt> implements NotificationsInt {

    public static NotificationsInt notifications() {
        return TypedCreator.typed(
                ActorSystem.system().actorOf(NotificationsActor.class, "notifications"),
                NotificationsInt.class);
    }

    private static final int NOTIFICATION_ID = 1;
    private static final long SOUND_DELAY = 300;

    public NotificationsActor() {
        super(NotificationsInt.class);
    }

    private int openedType = 0;
    private int openedId = 0;
    private SortedArrayList<PendingNotification> currentNotifications = new SortedArrayList<PendingNotification>(new Comparator<PendingNotification>() {
        @Override
        public int compare(PendingNotification lhs, PendingNotification rhs) {
            return -(lhs.getDate() < rhs.getDate() ? -1 : (lhs.getDate() == rhs.getDate() ? 0 : 1));
        }
    });
    private HashSet<Long> chats = new HashSet<Long>();

    private NotificationSettings notificationSettings;

    private SoundPool soundPool;
    private int inMessageId;
    private long lastSoundPlay = 0;

    private PersistenceSet<PendingNotification> pendingNotifications;

    @Override
    public void preStart() {
        super.preStart();
        pendingNotifications = new PersistenceSet<PendingNotification>(
                new BserMap<PendingNotification>(new SqliteStorage(DbProvider.getDatabase(AppContext.getContext()), "pending_notifications"),
                        PendingNotification.class)
        );


        currentNotifications.addAll(pendingNotifications);
        for (PendingNotification p : currentNotifications) {
            chats.add(DialogUids.getDialogUid(p.getConvType(), p.getConvId()));
        }
        notificationSettings = NotificationSettings.getInstance();

        soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        inMessageId = soundPool.load(AppContext.getContext(), R.raw.notification, 1);
    }

    @Override
    public void onAppOpened() {

    }

    @Override
    public void onAppClosed() {

    }

    @Override
    public void onChatOpen(int type, int id) {
        openedId = id;
        openedType = type;

        Iterator<PendingNotification> iterator = currentNotifications.iterator();
        while (iterator.hasNext()) {
            PendingNotification n = iterator.next();
            if (n.getConvId() == id && n.getConvType() == type) {
                iterator.remove();
                pendingNotifications.remove(n);
            }
        }

        hideNotification();
    }

    @Override
    public void onChatClose(int type, int id) {
        if (openedType == type && openedId == id) {
            openedType = 0;
            openedId = 0;
        }
    }

    @Override
    public void onNewMessage(int type, int id, ConversationMessage conversationMessage) {
        if (!notificationSettings.convValue(DialogUids.getDialogUid(type, id)).getValue()) {
            disableNotifications(type, id);
            return;
        }

        if (openedType == type && openedId == id) {
            if (conversationMessage.getSenderId() != myUid()) {
                if (notificationSettings.isInAppEnabled()) {
                    if (SystemClock.uptimeMillis() - lastSoundPlay > SOUND_DELAY) {
                        soundPool.play(inMessageId, 1.0f, 1.0f, 0, 0, 1.0f);
                        lastSoundPlay = SystemClock.uptimeMillis();
                    }
                }
            }
            return;
        }

        chats.add(DialogUids.getDialogUid(type, id));

        PendingNotificationBuilder pendingNotification = new PendingNotificationBuilder()
                .setConvType(type)
                .setConvId(id)
                .setRid(conversationMessage.getRid())
                .setUid(conversationMessage.getSenderId())
                .setDate(conversationMessage.getTime());

        if (conversationMessage.getContent() instanceof TextMessage) {
            pendingNotification.setType(PendingNotification.Type.TEXT);
            pendingNotification.setText(((TextMessage) conversationMessage.getContent()).getText());
        } else if (conversationMessage.getContent() instanceof PhotoMessage) {
            pendingNotification.setType(PendingNotification.Type.PHOTO);
        } else if (conversationMessage.getContent() instanceof VideoMessage) {
            pendingNotification.setType(PendingNotification.Type.VIDEO);
        } else if (conversationMessage.getContent() instanceof DocumentMessage) {
            DocumentMessage documentMessage = (DocumentMessage) conversationMessage.getContent();
            pendingNotification.setType(PendingNotification.Type.DOCUMENT);
            pendingNotification.setText(documentMessage.getName());
        } else if (conversationMessage.getContent() instanceof AudioMessage) {
            pendingNotification.setType(PendingNotification.Type.VOICE);
        } else if (conversationMessage.getContent() instanceof UserRegisteredMessage) {
            pendingNotification.setType(PendingNotification.Type.USER_REGISTERED);
        } else if (conversationMessage.getContent() instanceof UserAddedDeviceMessage) {
            pendingNotification.setType(PendingNotification.Type.USER_DEVICE);
        } else if (conversationMessage.getContent() instanceof GroupCreated) {
            pendingNotification.setType(PendingNotification.Type.GROUP_CREATED);
        } else if (conversationMessage.getContent() instanceof GroupAdd) {
            pendingNotification.setType(PendingNotification.Type.GROUP_ADDED);
            pendingNotification.setDestUid(((GroupAdd) conversationMessage.getContent()).getAddedUid());
        } else if (conversationMessage.getContent() instanceof GroupKick) {
            pendingNotification.setType(PendingNotification.Type.GROUP_KICKED);
            pendingNotification.setDestUid(((GroupKick) conversationMessage.getContent()).getKickedUid());
        } else if (conversationMessage.getContent() instanceof GroupLeave) {
            pendingNotification.setType(PendingNotification.Type.GROUP_LEFT);
        } else {
            return;
        }

        PendingNotification notification = pendingNotification.createPendingNotification();
        currentNotifications.add(notification);
        pendingNotifications.add(notification);

        performNotify(false);
    }

    @Override
    public void toggleNotifications(int type, int id) {

    }

    private String getNotificationSender(PendingNotification pendingNotification) {
        String sender;
        if (pendingNotification.getConvType() == DialogType.TYPE_GROUP) {
            sender = users().get(pendingNotification.getUid()).getName();
            sender += "@";
            sender += groups().get(pendingNotification.getConvId()).getTitle();
        } else {
            sender = users().get(pendingNotification.getUid()).getName();
        }
        return sender;
    }

    private CharSequence getNotificationTextFull(PendingNotification pendingNotification) {
        SpannableStringBuilder res = new SpannableStringBuilder();
        res.append(getNotificationSender(pendingNotification));
        res.append(": ");
        res.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, res.length(), 0);
        res.append(getNotificationText(pendingNotification));
        return res;
    }

    private CharSequence getNotificationTextSender(PendingNotification pendingNotification) {
        String sender = users().get(pendingNotification.getUid()).getName();
        return sender + ": " + getNotificationText(pendingNotification);
    }

    private CharSequence getNotificationText(PendingNotification pendingNotification) {
        switch (pendingNotification.getType()) {
            default:
            case TEXT:
                return pendingNotification.getText();
            case PHOTO:
                return "Photo";
            case VIDEO:
                return "Video";
            case VOICE:
                return "Voice";
            case DOCUMENT:
                if (pendingNotification.getText() != null) {
                    return pendingNotification.getText();
                }
                return "Document";
            case USER_REGISTERED:
                return "User registered";
            case GROUP_CREATED:
                return "Created group";
            case GROUP_ADDED:
                if (pendingNotification.getDestUid() != 0) {
                    UserModel u = users().get(pendingNotification.getDestUid());
                    if (u != null) {
                        return "Added " + u.getName() + " to group";
                    }
                }
                return "Added someone to group";
            case GROUP_KICKED:
                if (pendingNotification.getDestUid() != 0) {
                    UserModel u = users().get(pendingNotification.getDestUid());
                    if (u != null) {
                        return "Kicked " + u.getName() + " from group";
                    }
                }
                return "Kicked someone from group";
            case GROUP_LEFT:
                return "Left group";
        }
    }

    private void performNotify(boolean isSilent) {
        if (chats.size() == 0 || currentNotifications.size() == 0) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(AppContext.getContext());

        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_app_notify);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        if (SystemClock.uptimeMillis() - lastSoundPlay > SOUND_DELAY && !isSilent) {
            int defaults = Notification.DEFAULT_LIGHTS;
            if (notificationSettings.isVibrationEnabled()) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
            if (notificationSettings.isSoundsEnabled()) {
                defaults |= Notification.DEFAULT_SOUND;
            }
            builder.setDefaults(defaults);
            lastSoundPlay = SystemClock.uptimeMillis();
        } else {
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        }

        Notification result;
        if (!notificationSettings.isShowTitles()) {
            builder.setContentTitle(AppContext.getContext().getString(R.string.app_name));
            if (chats.size() == 1) {
                PendingNotification pendingNotification = currentNotifications.get(0);
                if (currentNotifications.size() == 1) {
                    builder.setTicker("1 new message");
                    builder.setContentText("1 new message");
                } else {
                    builder.setTicker(currentNotifications.size() + " new messages");
                    builder.setContentText(currentNotifications.size() + " new messages");
                }

                builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(),
                        RandomUtil.randomInt(),
                        Intents.openDialog(pendingNotification.getConvType(), pendingNotification.getConvId(), false, AppContext.getContext()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

            } else {
                builder.setTicker(currentNotifications.size() + " new messages from " + chats.size() + " readStates");
                builder.setContentText(currentNotifications.size() + " new messages from " + chats.size() + " readStates");
                builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(),
                        RandomUtil.randomInt(), new Intent(AppContext.getContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
            }

            result = builder.build();
        } else {
            if (chats.size() == 1) {
                PendingNotification pendingNotification = currentNotifications.get(0);
                if (currentNotifications.size() == 1) {
                    CharSequence text = getNotificationTextFull(pendingNotification);
                    String sender = getNotificationSender(pendingNotification);
                    builder.setTicker(getNotificationTextFull(pendingNotification));
                    builder.setContentTitle(sender);
                    builder.setContentText(text);
                    builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(),
                            0, Intents.openDialog(pendingNotification.getConvType(), pendingNotification.getConvId(), false, AppContext.getContext()), 0));

                    NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle(builder);
                    inboxStyle.setBigContentTitle(sender);
                    inboxStyle.bigText(getNotificationText(pendingNotification));
                    result = inboxStyle.build();
                } else {
                    NotificationCompat.InboxStyle inboxStyle;
                    if (pendingNotification.getConvType() == DialogType.TYPE_USER) {
                        String title = users().get(pendingNotification.getConvId()).getName();
                        builder.setContentTitle(title);
                        builder.setTicker("Messages from " + title);
                        builder.setContentText("Messages from " + title);

                        inboxStyle = new NotificationCompat.InboxStyle(builder);
                        inboxStyle.setBigContentTitle(title);
                        if (currentNotifications.size() <= 5) {
                            for (PendingNotification notification : currentNotifications) {
                                inboxStyle.addLine(getNotificationText(notification));
                            }
                            inboxStyle.setSummaryText(currentNotifications.size() + " new messages");
                        } else {
                            for (int i = 0; i < 5; i++) {
                                PendingNotification notification = currentNotifications.get(i);
                                inboxStyle.addLine(getNotificationText(notification));
                            }
                            inboxStyle.setSummaryText("+ " + (currentNotifications.size() - 5) + " more");
                        }
                    } else {
                        String title = groups().get(pendingNotification.getConvId()).getTitle();
                        builder.setContentTitle(title);
                        builder.setTicker("Messages in " + title);
                        builder.setContentText("Messages in " + title);

                        inboxStyle = new NotificationCompat.InboxStyle(builder);
                        inboxStyle.setBigContentTitle(title);
                        if (currentNotifications.size() <= 5) {
                            for (PendingNotification notification : currentNotifications) {
                                inboxStyle.addLine(getNotificationTextSender(notification));
                            }
                            inboxStyle.setSummaryText(currentNotifications.size() + " new messages");
                        } else {
                            for (int i = 0; i < 5; i++) {
                                PendingNotification notification = currentNotifications.get(i);
                                inboxStyle.addLine(getNotificationTextSender(notification));
                            }
                            inboxStyle.setSummaryText("+ " + (currentNotifications.size() - 5) + " more");
                        }
                    }

                    builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(),
                            RandomUtil.randomInt(), Intents.openDialog(pendingNotification.getConvType(), pendingNotification.getConvId(), false, AppContext.getContext()), 0));

                    result = inboxStyle.build();
                }
            } else {
                builder.setTicker(currentNotifications.size() + " new messages");
                builder.setContentText(currentNotifications.size() + " new messages");
                builder.setContentTitle(AppContext.getContext().getString(R.string.app_name));
                builder.setContentIntent(PendingIntent.getActivity(AppContext.getContext(),
                        RandomUtil.randomInt(), new Intent(AppContext.getContext(), MainActivity.class), 0));

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
                if (currentNotifications.size() <= 5) {
                    for (PendingNotification notification : currentNotifications) {
                        inboxStyle.addLine(getNotificationTextFull(notification));
                    }
                    inboxStyle.setSummaryText(currentNotifications.size() + " new messages");
                } else {
                    for (int i = 0; i < 5; i++) {
                        PendingNotification notification = currentNotifications.get(i);
                        inboxStyle.addLine(getNotificationTextFull(notification));
                    }
                    inboxStyle.setSummaryText("+ " + (currentNotifications.size() - 5) + " more");
                }

                result = inboxStyle.build();
            }
        }

        NotificationManager manager = (NotificationManager) AppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, result);
    }

    private void hideNotification() {
        NotificationManager manager = (NotificationManager) AppContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }

    private void disableNotifications() {
        pendingNotifications.clear();
        currentNotifications.clear();
        hideNotification();
    }

    private void disableNotifications(int type, int id) {
        Iterator<PendingNotification> iterator = currentNotifications.iterator();
        while (iterator.hasNext()) {
            PendingNotification n = iterator.next();
            if (n.getConvId() == id && n.getConvType() == type) {
                iterator.remove();
                pendingNotifications.remove(n);
            }
        }
        if (currentNotifications.size() == 0) {
            hideNotification();
        } else {
            performNotify(true);
        }
    }
}
