/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.i18n;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Notification;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.entity.content.ServiceCallEnded;
import im.actor.core.entity.content.ServiceCallMissed;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.ServiceGroupAvatarChanged;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupTitleChanged;
import im.actor.core.entity.content.ServiceGroupTopicChanged;
import im.actor.core.entity.content.ServiceGroupAboutChanged;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserJoined;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.entity.content.TextContent;
import im.actor.core.modules.Modules;
import im.actor.core.network.RpcException;
import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.UserPresence;
import im.actor.runtime.Assets;
import im.actor.runtime.Runtime;
import im.actor.runtime.intl.IntlEngine;
import im.actor.runtime.json.JSONException;

public class I18nEngine extends IntlEngine {

    private static final String TAG = "I18nEngine";

    private static final String[] SUPPORTED_LOCALES = new String[]{"Ru", "Ar", "Zn", "Pt", "Es", "Fa"};
    private static final String[] FEMALE = new String[]{"female", "other"};
    private static final String[] MALE = new String[]{"male", "other"};
    private static final String[] DEFAULT = new String[]{"other"};
    private static final String[] YOU = new String[]{"you"};

    public static I18nEngine create(Modules modules) {
        String currentLocale = Runtime.getLocaleRuntime().getCurrentLocale();

        if (currentLocale != null) {
            if (JavaUtil.contains(SUPPORTED_LOCALES, currentLocale)) {
                try {
                    return new I18nEngine(
                            modules,
                            Assets.loadAsset("AppText_" + currentLocale + ".json"),
                            Assets.loadAsset("AppText.json"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            return new I18nEngine(
                    modules,
                    Assets.loadAsset("AppText.json"),
                    null);
        } catch (JSONException e) {
            // Shoud not happen
            throw new RuntimeException(e);
        }
    }

    private final Modules modules;

    @ObjectiveCName("initWithModules:withLocalization:withFallback:")
    public I18nEngine(Modules modules, String localization, String fallback) throws JSONException {
        super(localization, fallback);
        this.modules = modules;
    }

    @Override
    public String getAppName() {
        String appName = modules.getConfiguration().getCustomAppName();
        if (appName != null) {
            return appName;
        }
        return super.getAppName();
    }


    //
    // Typing
    //

    @ObjectiveCName("formatTyping")
    public String formatTyping() {
        return get("typing.simple");
    }

    @ObjectiveCName("formatTypingWithName:")
    public String formatTyping(String name) {
        return get("typing.user")
                .replace("{user}", name);
    }

    @ObjectiveCName("formatTypingWithNames:")
    public String formatTyping(List<String> names) {
        if (names.size() == 1) {
            return formatTyping(names.get(0));
        }
        return get("typing.group.sequenced")
                .replace("{users}", formatSequence(names));
    }

    @ObjectiveCName("formatTypingWithCount:")
    public String formatTyping(int count) {
        return get("typing.group.many")
                .replace("{count}", "" + count);
    }


    //
    // Presence
    //

    @ObjectiveCName("formatPresence:withSex:")
    public String formatPresence(UserPresence value, Sex sex) {
        if (value == null) {
            return null;
        }

        String[] sexType = DEFAULT;
        if (sex == Sex.MALE) {
            sexType = MALE;
        } else if (sex == Sex.FEMALE) {
            sexType = FEMALE;
        }

        if (value.getState() == UserPresence.State.OFFLINE) {

            long currentTime = im.actor.runtime.Runtime.getCurrentSyncedTime() / 1000L;
            int delta = (int) (currentTime - value.getLastSeen());
            if (delta < 60) {
                return get("presence.now", sexType);
            } else if (delta < 24 * 60 * 60) {
                String time = formatTime(value.getLastSeen() * 1000L);
                if (areSameDays(value.getLastSeen() * 1000L, new Date().getTime())) {
                    return get("presence.today", sexType)
                            .replace("{time}", time);
                } else {
                    return get("presence.yesterday", sexType)
                            .replace("{time}", time);
                }
            } else if (delta < 14 * 24 * 60 * 60) {

                String time = formatTime(value.getLastSeen() * 1000L);
                String date = formatDate(value.getLastSeen() * 1000L);

                return get("presence.at_day_time", sexType)
                        .replace("{time}", time)
                        .replace("{date}", date);

            } else if (delta < 6 * 30 * 24 * 60 * 60) {
                String date = formatDate(value.getLastSeen() * 1000L);

                return get("presence.at_day", sexType)
                        .replace("{date}", date);
            } else {
                return get("presence.offline", sexType);
            }
        } else if (value.getState() == UserPresence.State.ONLINE) {
            return get("presence.online", sexType);
        }

        return null;
    }

    @ObjectiveCName("formatGroupOnline:")
    public String formatGroupOnline(int count) {
        return getPlural("presence.members", count)
                .replace("{count}", "" + count);
    }


    //
    // Group
    //

    /**
     * Formatting Group Members counter
     *
     * @param count number of members
     * @return formatted string like "12 members"
     */
    @ObjectiveCName("formatGroupMembers:")
    public String formatGroupMembers(int count) {
        return getPlural("groups.members", count)
                .replace("{count}", "" + count);
    }


    //
    // Content
    //

    /**
     * Formatting Dialog List text. Deprecated: you need to manually format Content and append
     * performer if needed and highlight it
     *
     * @param dialog dialog to format
     * @return formatted content
     */
    @Deprecated
    @ObjectiveCName("formatDialogText:")
    public String formatDialogText(Dialog dialog) {
        // Detecting if dialog is empty
        if (dialog.getSenderId() == 0) {
            return "";
        } else {
            String contentText = formatContentText(dialog.getSenderId(),
                    dialog.getMessageType(), dialog.getText(), dialog.getRelatedUid());
            if (dialog.getPeer().getPeerType() == PeerType.GROUP) {
                if (!isLargeDialogMessage(dialog.getMessageType())) {
                    return formatPerformerName(dialog.getSenderId()) + ": " + contentText;
                } else {
                    return contentText;
                }
            } else {
                return contentText;
            }
        }
    }

    /**
     * If Dialog List message need to be wide in group chat as it is already includes performer
     * in it's body.
     *
     * @param contentType Type of Content
     * @return true if content is wide
     */
    @ObjectiveCName("isLargeDialogMessage:")
    public boolean isLargeDialogMessage(ContentType contentType) {
        switch (contentType) {
            case SERVICE:
            case SERVICE_AVATAR:
            case SERVICE_AVATAR_REMOVED:
            case SERVICE_CREATED:
            case SERVICE_TITLE:
            case SERVICE_LEAVE:
            case SERVICE_REGISTERED:
            case SERVICE_KICK:
            case SERVICE_ADD:
            case SERVICE_JOINED:
            case SERVICE_CALL_ENDED:
            case SERVICE_CALL_MISSED:
            case SERVICE_ABOUT:
            case SERVICE_TOPIC:
                return true;
            default:
                return false;
        }
    }

    /**
     * Formatting Pending notification text
     *
     * @param pendingNotification pending notification
     * @return formatted notification
     */
    @ObjectiveCName("formatNotificationText:")
    public String formatNotificationText(Notification pendingNotification) {
        return formatContentText(pendingNotification.getSender(),
                pendingNotification.getContentDescription().getContentType(),
                pendingNotification.getContentDescription().getText(),
                pendingNotification.getContentDescription().getRelatedUser());
    }

    /**
     * Formatting content for Dialog List and Notifications
     *
     * @param senderId    sender of message (used in service messages)
     * @param contentType type of content
     * @param text        text of message
     * @param relatedUid  optional related uid
     * @return formatted content
     */
    @ObjectiveCName("formatContentTextWithSenderId:withContentType:withText:withRelatedUid:")
    public String formatContentText(int senderId, ContentType contentType, String text, int relatedUid) {
        switch (contentType) {
            case TEXT:
                return text;
            case DOCUMENT:
                if (text == null || text.length() == 0) {
                    return get("content.document");
                }
                return text;// File name
            case DOCUMENT_PHOTO:
                return get("content.photo");
            case DOCUMENT_VIDEO:
                return get("content.video");
            case DOCUMENT_AUDIO:
                return get("content.audio");
            case CONTACT:
                return get("content.contact");
            case LOCATION:
                return get("content.location");
            case STICKER:
                if (text != null && !"".equals(text)) {
                    return text + " " + get("content.sticker");
                } else {
                    return get("content.sticker");
                }
            case SERVICE:
                return text;// Should be service message
            case SERVICE_REGISTERED:
                return getTemplateNamed(senderId, "content.service.registered.compact")
                        .replace("{app_name}", getAppName());
            case SERVICE_CREATED:
                return getTemplateNamed(senderId, "content.service.groups.created");
            case SERVICE_ADD:
                return getTemplateNamed(senderId, "content.service.groups.invited")
                        .replace("{name_added}", getSubjectName(relatedUid));
            case SERVICE_LEAVE:
                return getTemplateNamed(senderId, "content.service.groups.left");
            case SERVICE_KICK:
                return getTemplateNamed(senderId, "content.service.groups.kicked")
                        .replace("{name_kicked}", getSubjectName(relatedUid));
            case SERVICE_AVATAR:
                return getTemplateNamed(senderId, "content.service.groups.avatar_changed");
            case SERVICE_AVATAR_REMOVED:
                return getTemplateNamed(senderId, "content.service.groups.avatar_removed");
            case SERVICE_TITLE:
                return getTemplateNamed(senderId, "content.service.groups.title_changed.compact");
            case SERVICE_TOPIC:
                return getTemplateNamed(senderId, "content.service.groups.topic_changed.compact");
            case SERVICE_ABOUT:
                return getTemplateNamed(senderId, "content.service.groups.about_changed.compact");
            case SERVICE_JOINED:
                return getTemplateNamed(senderId, "content.service.groups.joined");
            case SERVICE_CALL_ENDED:
                return get("content.service.calls.ended");
            case SERVICE_CALL_MISSED:
                return get("content.service.calls.missed");
            case NONE:
                return "";
            default:
            case UNKNOWN_CONTENT:
                return get("content.unsupported");
        }
    }

    /**
     * Formatting Service Content
     *
     * @param senderId sender of a message
     * @param content  content of a message
     * @return formatted message
     */
    @ObjectiveCName("formatFullServiceMessageWithSenderId:withContent:")
    public String formatFullServiceMessage(int senderId, ServiceContent content) {
        if (content instanceof ServiceUserRegistered) {
            return getTemplateNamed(senderId, "content.service.registered.full")
                    .replace("{app_name}", getAppName());
        } else if (content instanceof ServiceGroupCreated) {
            return getTemplateNamed(senderId, "content.service.groups.created");
        } else if (content instanceof ServiceGroupUserInvited) {
            return getTemplateNamed(senderId, "content.service.groups.invited")
                    .replace("{name_added}",
                            getSubjectName(((ServiceGroupUserInvited) content).getAddedUid()));
        } else if (content instanceof ServiceGroupUserKicked) {
            return getTemplateNamed(senderId, "content.service.groups.kicked")
                    .replace("{name_kicked}",
                            getSubjectName(((ServiceGroupUserKicked) content).getKickedUid()));
        } else if (content instanceof ServiceGroupUserLeave) {
            return getTemplateNamed(senderId, "content.service.groups.left");
        } else if (content instanceof ServiceGroupTitleChanged) {
            return getTemplateNamed(senderId, "content.service.groups.title_changed.full")
                    .replace("{title}",
                            ((ServiceGroupTitleChanged) content).getNewTitle());
        } else if (content instanceof ServiceGroupTopicChanged) {
            return getTemplateNamed(senderId, "content.service.groups.topic_changed.full")
                    .replace("{topic}",
                            ((ServiceGroupTopicChanged) content).getNewTopic());
        } else if (content instanceof ServiceGroupAboutChanged) {
            return getTemplateNamed(senderId, "content.service.groups.about_changed.full")
                    .replace("{about}",
                            ((ServiceGroupAboutChanged) content).getNewAbout());
        } else if (content instanceof ServiceGroupAvatarChanged) {
            if (((ServiceGroupAvatarChanged) content).getNewAvatar() != null) {
                return getTemplateNamed(senderId, "content.service.groups.avatar_changed");
            } else {
                return getTemplateNamed(senderId, "content.service.groups.avatar_removed");
            }
        } else if (content instanceof ServiceGroupUserJoined) {
            return getTemplateNamed(senderId, "content.service.groups.joined");
        } else if (content instanceof ServiceCallEnded) {
            return get("content.service.calls.ended");
        } else if (content instanceof ServiceCallMissed) {
            return get("content.service.calls.missed");
        }

        return content.getCompatText();
    }


    //
    // Formatting errors
    //

    @ObjectiveCName("formatErrorTextWithTag:")
    public String formatErrorText(String tag) {
        return get(Errors.mapError(tag));
    }

    @ObjectiveCName("formatErrorTextWithError:")
    public String formatErrorText(Object o) {
        if (o instanceof RpcException) {
            RpcException e = (RpcException) o;
            String res = Errors.mapError(e.getTag(), null);
            if (res != null) {
                return get(res);
            } else {
                if (e.getMessage().equals("")) {
                    return e.getTag();
                } else {
                    return e.getMessage();
                }
            }
        } else if (o instanceof Exception) {
            return ((Exception) o).getMessage();
        } else {
            return "" + o;
        }
    }


    //
    // Exporting messages
    //

    /**
     * Formatting messages for exporting
     *
     * @param messages messages to export
     * @return formatted text
     */
    @ObjectiveCName("formatMessagesExport:")
    public String formatMessagesExport(Message[] messages) {
        String text = "";
        Arrays.sort(messages, new Comparator<Message>() {

            int compare(long lhs, long rhs) {
                return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
            }

            @Override
            public int compare(Message lhs, Message rhs) {
                return compare(lhs.getEngineSort(), rhs.getEngineSort());
            }
        });

        if (messages.length == 1) {
            for (Message model : messages) {
                if (!(model.getContent() instanceof TextContent)) {
                    continue;
                }
                text += ((TextContent) model.getContent()).getText();
            }
        } else {
            for (Message model : messages) {
                if (!(model.getContent() instanceof TextContent)) {
                    continue;
                }
                if (text.length() > 0) {
                    text += "\n";
                }
                text += getUser(model.getSenderId()).getName() + ": ";
                text += ((TextContent) model.getContent()).getText();
            }
        }
        return text;
    }


    //
    // Tools
    //

    private String getTemplateNamed(int senderId, String baseString) {
        String newString = getTemplate(senderId, baseString)
                .replace("{name}", formatPerformerName(senderId));

        // term with 'you' in persian language should be appended with suffix
        if (getLocaleName().equals("Fa")) {
            if (senderId == modules.getAuthModule().myUid()) {
                newString += "ید";
            }
        }
        return newString;
    }

    private String getTemplate(int senderId, String baseString) {
        if (senderId == modules.getAuthModule().myUid()) {
            return get(baseString, YOU);
        }

        User u = getUser(senderId);
        String[] sexType = DEFAULT;
        if (u.getSex() == Sex.MALE) {
            sexType = MALE;
        } else if (u.getSex() == Sex.FEMALE) {
            sexType = FEMALE;
        }
        return get(baseString, sexType);
    }

    @ObjectiveCName("formatPerformerNameWithUid:")
    public String formatPerformerName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return getYouVerb();
        } else {
            return getUser(uid).getName();
        }
    }

    @ObjectiveCName("getSubjectNameWithUid:")
    public String getSubjectName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return getTheeVerb();
        } else {
            User user = getUser(uid);
            return user != null ? user.getName() : "";
        }
    }

    private User getUser(int uid) {
        return modules.getUsersModule().getUsersStorage().getValue(uid);
    }
}
