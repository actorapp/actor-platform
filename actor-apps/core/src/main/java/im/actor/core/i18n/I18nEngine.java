/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.i18n;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import im.actor.core.entity.ContentType;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Notification;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Sex;
import im.actor.core.entity.User;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.ServiceGroupAvatarChanged;
import im.actor.core.entity.content.ServiceGroupCreated;
import im.actor.core.entity.content.ServiceGroupTitleChanged;
import im.actor.core.entity.content.ServiceGroupUserInvited;
import im.actor.core.entity.content.ServiceGroupUserJoined;
import im.actor.core.entity.content.ServiceGroupUserKicked;
import im.actor.core.entity.content.ServiceGroupUserLeave;
import im.actor.core.entity.content.ServiceUserRegistered;
import im.actor.core.entity.content.TextContent;
import im.actor.core.modules.Errors;
import im.actor.core.modules.Modules;
import im.actor.core.network.RpcException;
import im.actor.core.util.JavaUtil;
import im.actor.core.viewmodel.UserPresence;
import im.actor.runtime.LocaleRuntime;
import im.actor.runtime.Log;
import im.actor.runtime.Runtime;

public class I18nEngine {

    private static final String TAG = "I18nEngine";

    private static final String[] SUPPORTED_LOCALES = new String[]{"Ru", "Ar", "Cn", "Pt"};

    private final Modules modules;
    private final LocaleRuntime runtime;
    private final HashMap<String, String> locale;

    private final String[] MONTHS_SHORT;
    private final String[] MONTHS;

    @ObjectiveCName("initWithModules:")
    public I18nEngine(Modules modules) {
        this.modules = modules;
        this.runtime = Runtime.getLocaleRuntime();

        // Loading locale
        this.locale = new HashMap<String, String>();
        String currentLocale = runtime.getCurrentLocale();
        boolean isLoaded = false;
        if (currentLocale != null) {
            if (JavaUtil.contains(SUPPORTED_LOCALES, currentLocale)) {
                this.locale.putAll(LocaleLoader.loadPropertiesFile("AppText_" + currentLocale + ".properties"));
                this.locale.putAll(LocaleLoader.loadPropertiesFile("Months_" + currentLocale + ".properties"));
                isLoaded = true;
            }
        }
        if (!isLoaded) {
            this.locale.putAll(LocaleLoader.loadPropertiesFile("AppText.properties"));
            this.locale.putAll(LocaleLoader.loadPropertiesFile("Months.properties"));
        }

        MONTHS_SHORT = new String[]{
                locale.get("JanShort"),
                locale.get("FebShort"),
                locale.get("MarShort"),
                locale.get("AprShort"),
                locale.get("MayShort"),
                locale.get("JunShort"),
                locale.get("JulShort"),
                locale.get("AugShort"),
                locale.get("SepShort"),
                locale.get("OctShort"),
                locale.get("NovShort"),
                locale.get("DecShort"),
        };

        MONTHS = new String[]{
                locale.get("JanFull"),
                locale.get("FebFull"),
                locale.get("MarFull"),
                locale.get("AprFull"),
                locale.get("MayFull"),
                locale.get("JunFull"),
                locale.get("JulFull"),
                locale.get("AugFull"),
                locale.get("SepFull"),
                locale.get("OctFull"),
                locale.get("NovFull"),
                locale.get("DecFull"),
        };
    }

    private String formatTwoDigit(int v) {
        if (v < 0) {
            return "00";
        } else if (v < 10) {
            return "0" + v;
        } else if (v < 100) {
            return "" + v;
        } else {
            String res = "" + v;
            return res.substring(res.length() - 2);
        }
    }

    private static boolean areSameDays(long a, long b) {
        Date date1 = new Date(a);
        int y1 = date1.getYear();
        int m1 = date1.getMonth();
        int d1 = date1.getDate();
        Date date2 = new Date(b);
        int y2 = date2.getYear();
        int m2 = date2.getMonth();
        int d2 = date2.getDate();

        return y1 == y2 && m1 == m2 && d1 == d2;
    }

    @ObjectiveCName("formatShortDate:")
    public String formatShortDate(long date) {
        // Not using Calendar for GWT
        long delta = new Date().getTime() - date;
        if (delta < 60 * 1000) {
            return locale.get("TimeShortNow");
        } else if (delta < 60 * 60 * 1000) {
            return locale.get("TimeShortMinutes").replace("{minutes}", "" + delta / 60000);
        } else if (delta < 24 * 60 * 60 * 1000) {
            return locale.get("TimeShortHours").replace("{hours}", "" + delta / 3600000);
        } else if (delta < 2 * 24 * 60 * 60 * 1000) {
            return locale.get("TimeShortYesterday").replace("{hours}", "" + delta / 3600000);
        } else {
            // Not using Calendar for GWT
            Date date1 = new Date(date);
            int month = date1.getMonth();
            int d = date1.getDate();
            return d + " " + MONTHS_SHORT[month].toUpperCase();
        }
    }

    @ObjectiveCName("formatTyping")
    public String formatTyping() {
        return locale.get("Typing");
    }

    @ObjectiveCName("formatTypingWithName:")
    public String formatTyping(String name) {
        return locale.get("TypingUser").replace("{user}", name);
    }

    @ObjectiveCName("formatTypingWithCount:")
    public String formatTyping(int count) {
        return locale.get("TypingMultiple").replace("{count}", "" + count);
    }

    @ObjectiveCName("formatFileSize:")
    public String formatFileSize(int bytes) {
        if (bytes < 0) {
            bytes = 0;
        }

        if (bytes < 1024) {
            return locale.get("FileB").replace("{bytes}", "" + bytes);
        } else if (bytes < 1024 * 1024) {
            return locale.get("FileKb").replace("{kbytes}", "" + (bytes / 1024));
        } else if (bytes < 1024 * 1024 * 1024) {
            return locale.get("FileMb").replace("{mbytes}", "" + (bytes / (1024 * 1024)));
        } else {
            return locale.get("FileGb").replace("{gbytes}", "" + (bytes / (1024 * 1024 * 1024)));
        }
    }

    @ObjectiveCName("formatTime:")
    public String formatTime(long date) {
        return runtime.formatTime(date);
    }

    @ObjectiveCName("formatDate:")
    public String formatDate(long date) {
        return runtime.formatDate(date);
    }

    @ObjectiveCName("formatPresence:withSex:")
    public String formatPresence(UserPresence value, Sex sex) {
        if (value == null) {
            return null;
        }

        if (value.getState() == UserPresence.State.OFFLINE) {

            long currentTime = im.actor.runtime.Runtime.getCurrentSyncedTime() / 1000L;
            int delta = (int) (currentTime - value.getLastSeen());
            if (delta < 60) {
                Log.d(TAG, "formatPresence: onlineNow");
                if (locale.containsKey("OnlineNowMale") && locale.containsKey("OnlineNowFemale")) {
                    return sex == Sex.UNKNOWN
                            ? locale.get("OnlineNow")
                            : sex == Sex.MALE
                            ? locale.get("OnlineNowMale")
                            : locale.get("OnlineNowFemale");
                } else {
                    return locale.get("OnlineNow");
                }
            } else if (delta < 24 * 60 * 60) {

                String time = formatTime(value.getLastSeen() * 1000L);
                if (areSameDays(value.getLastSeen() * 1000L, new Date().getTime())) {
                    if (locale.containsKey("OnlineLastSeenTodayMale") && locale.containsKey("OnlineLastSeenTodayMale")) {
                        return (sex == Sex.UNKNOWN
                                ? locale.get("OnlineLastSeenToday")
                                : sex == Sex.MALE
                                ? locale.get("OnlineLastSeenTodayMale")
                                : locale.get("OnlineLastSeenTodayFemale")).replace("{time}", time);
                    } else {
                        return locale.get("OnlineLastSeenToday").replace("{time}", time);
                    }
                } else {
                    if (locale.containsKey("OnlineLastSeenYesterdayMale") && locale.containsKey("OnlineLastSeenYesterdayMale")) {
                        return (sex == Sex.UNKNOWN
                                ? locale.get("OnlineLastSeenYesterday")
                                : sex == Sex.MALE
                                ? locale.get("OnlineLastSeenYesterdayMale")
                                : locale.get("OnlineLastSeenYesterdayFemale")).replace("{time}", time);
                    } else {
                        return locale.get("OnlineLastSeenYesterday").replace("{time}", time);
                    }
                }
            } else if (delta < 14 * 24 * 60 * 60) {

                String time = formatTime(value.getLastSeen() * 1000L);
                String date = formatDate(value.getLastSeen() * 1000L);
                if (locale.containsKey("OnlineLastSeenDateTimeMale") && locale.containsKey("OnlineLastSeenDateTimeMale")) {
                    return (sex == Sex.UNKNOWN
                            ? locale.get("OnlineLastSeenDateTime")
                            : sex == Sex.MALE
                            ? locale.get("OnlineLastSeenDateTimeMale")
                            : locale.get("OnlineLastSeenDateTimeFemale"))
                            .replace("{time}", time)
                            .replace("{date}", date);
                } else {
                    return locale.get("OnlineLastSeenDateTime")
                            .replace("{time}", time)
                            .replace("{date}", date);
                }
            } else if (delta < 6 * 30 * 24 * 60 * 60) {
                String date = formatDate(value.getLastSeen() * 1000L);
                if (locale.containsKey("OnlineLastSeenDateMale") && locale.containsKey("OnlineLastSeenDateMale")) {
                    return (sex == Sex.UNKNOWN
                            ? locale.get("OnlineLastSeenDate")
                            : sex == Sex.MALE
                            ? locale.get("OnlineLastSeenDateMale")
                            : locale.get("OnlineLastSeenDateFemale"))
                            .replace("{date}", date);
                } else {
                    return locale.get("OnlineLastSeenDate")
                            .replace("{date}", date);
                }
            } else {
                return locale.get("OnlineOff");
            }
        } else if (value.getState() == UserPresence.State.ONLINE) {
            return locale.get("OnlineOn");
        }

        return null;
    }

    @ObjectiveCName("formatDuration:")
    public String formatDuration(int duration) {
        if (duration < 60) {
            return formatTwoDigit(0) + ":" + formatTwoDigit(duration);
        } else if (duration < 60 * 60) {
            return formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        } else {
            return formatTwoDigit(duration / 3600) + ":" + formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        }
    }

    @ObjectiveCName("formatGroupMembers:")
    public String formatGroupMembers(int count) {
        return locale.get("GroupMembers").replace("{count}", "" + count);
    }

    @ObjectiveCName("formatGroupOnline:")
    public String formatGroupOnline(int count) {
        return locale.get("GroupOnline").replace("{count}", "" + count);
    }

    @ObjectiveCName("formatDialogText:")
    public String formatDialogText(Dialog dialog) {
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

    @ObjectiveCName("formatNotificationText:")
    public String formatNotificationText(Notification pendingNotification) {
        return formatContentText(pendingNotification.getSender(),
                pendingNotification.getContentDescription().getContentType(),
                pendingNotification.getContentDescription().getText(),
                pendingNotification.getContentDescription().getRelatedUser());
    }

    @ObjectiveCName("formatContentTextWithSenderId:withContentType:withText:withRelatedUid:")
    public String formatContentText(int senderId, ContentType contentType, String text, int relatedUid) {
        switch (contentType) {
            case TEXT:
                return text;
            case DOCUMENT:
                if (text == null || text.length() == 0) {
                    return locale.get("ContentDocument");
                }
                return text;// File name
            case DOCUMENT_PHOTO:
                return locale.get("ContentPhoto");
            case DOCUMENT_VIDEO:
                return locale.get("ContentVideo");
            case SERVICE:
                return text;// Should be service message
            case SERVICE_REGISTERED:
                return getTemplateNamed(senderId, "ServiceRegistered");
            case SERVICE_CREATED:
                return getTemplateNamed(senderId, "ServiceGroupCreated");
            case SERVICE_ADD:
                return getTemplateNamed(senderId, "ServiceGroupAdded")
                        .replace("{name_added}", getSubjectName(relatedUid));
            case SERVICE_LEAVE:
                return getTemplateNamed(senderId, "ServiceGroupLeaved");
            case SERVICE_KICK:
                return getTemplateNamed(senderId, "ServiceGroupKicked")
                        .replace("{name_kicked}", getSubjectName(relatedUid));
            case SERVICE_AVATAR:
                return getTemplateNamed(senderId, "ServiceGroupAvatarChanged");
            case SERVICE_AVATAR_REMOVED:
                return getTemplateNamed(senderId, "ServiceGroupAvatarRemoved");
            case SERVICE_TITLE:
                return getTemplateNamed(senderId, "ServiceGroupTitle");
            case SERVICE_JOINED:
                return getTemplateNamed(senderId, "ServiceGroupJoined");
            case NONE:
                return "";
            default:
            case UNKNOWN_CONTENT:
                return locale.get("ContentUnsupported");
        }
    }

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
                return true;
            default:
                return false;
        }
    }

    @ObjectiveCName("formatFullServiceMessageWithSenderId:withContent:")
    public String formatFullServiceMessage(int senderId, ServiceContent content) {
        if (content instanceof ServiceUserRegistered) {
            return getTemplateNamed(senderId, "ServiceRegisteredFull");
        } else if (content instanceof ServiceGroupCreated) {
            return getTemplateNamed(senderId, "ServiceGroupCreatedFull");
        } else if (content instanceof ServiceGroupUserInvited) {
            return getTemplateNamed(senderId, "ServiceGroupAdded")
                    .replace("{name_added}",
                            getSubjectName(((ServiceGroupUserInvited) content).getAddedUid()));
        } else if (content instanceof ServiceGroupUserKicked) {
            return getTemplateNamed(senderId, "ServiceGroupKicked")
                    .replace("{name_kicked}",
                            getSubjectName(((ServiceGroupUserKicked) content).getKickedUid()));
        } else if (content instanceof ServiceGroupUserLeave) {
            return getTemplateNamed(senderId, "ServiceGroupLeaved");
        } else if (content instanceof ServiceGroupTitleChanged) {
            return getTemplateNamed(senderId, "ServiceGroupTitleFull")
                    .replace("{title}",
                            ((ServiceGroupTitleChanged) content).getNewTitle());
        } else if (content instanceof ServiceGroupAvatarChanged) {
            if (((ServiceGroupAvatarChanged) content).getNewAvatar() != null) {
                return getTemplateNamed(senderId, "ServiceGroupAvatarChanged");
            } else {
                return getTemplateNamed(senderId, "ServiceGroupAvatarRemoved");
            }
        } else if (content instanceof ServiceGroupUserJoined) {
            return getTemplateNamed(senderId, "ServiceGroupJoined");
        }

        return content.getCompatText();
    }

    @ObjectiveCName("formatErrorTextWithTag:")
    public String formatErrorText(String tag) {
        return locale.get(Errors.mapError(tag));
    }

    @ObjectiveCName("formatErrorTextWithError:")
    public String formatErrorText(Object o) {
        if (o instanceof RpcException) {
            RpcException e = (RpcException) o;
            String res = Errors.mapError(e.getTag(), null);
            if (res != null) {
                return locale.get(res);
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

    @ObjectiveCName("formatPerformerNameWithUid:")
    public String formatPerformerName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return locale.get("You");
        } else {
            return getUser(uid).getName();
        }
    }

    @ObjectiveCName("getSubjectNameWithUid:")
    public String getSubjectName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return locale.get("Thee");
        } else {
            return getUser(uid).getName();
        }
    }

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

    @ObjectiveCName("formatFastName:")
    public String formatFastName(String name) {
        if (name.length() > 1) {
            if (Character.isLetter(name.charAt(0))) {
                return name.substring(0, 1).toUpperCase();
            } else {
                return "#";
            }
        } else {
            return "#";
        }
    }

    private String getTemplateNamed(int senderId, String baseString) {
        return getTemplate(senderId, baseString).replace("{name}",
                formatPerformerName(senderId));
    }

    private String getTemplate(int senderId, String baseString) {
        if (senderId == modules.getAuthModule().myUid()) {
            if (locale.containsKey(baseString + "You")) {
                return locale.get(baseString + "You");
            }
        }
        if (locale.containsKey(baseString + "Male") && locale.containsKey(baseString + "Female")) {
            User u = getUser(senderId);
            if (u.getSex() == Sex.MALE) {
                return locale.get(baseString + "Male");
            } else if (u.getSex() == Sex.FEMALE) {
                return locale.get(baseString + "Female");
            }
        }
        return locale.get(baseString);
    }

    private User getUser(int uid) {
        return modules.getUsersModule().getUsersStorage().getValue(uid);
    }
}
