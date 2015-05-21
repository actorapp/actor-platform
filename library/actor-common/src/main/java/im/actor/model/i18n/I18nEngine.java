/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.i18n;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import im.actor.model.LocaleProvider;
import im.actor.model.droidkit.actors.Environment;
import im.actor.model.entity.ContentType;
import im.actor.model.entity.Message;
import im.actor.model.entity.Sex;
import im.actor.model.entity.User;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.ServiceGroupAvatarChanged;
import im.actor.model.entity.content.ServiceGroupCreated;
import im.actor.model.entity.content.ServiceGroupTitleChanged;
import im.actor.model.entity.content.ServiceGroupUserAdded;
import im.actor.model.entity.content.ServiceGroupUserKicked;
import im.actor.model.entity.content.ServiceGroupUserLeave;
import im.actor.model.entity.content.ServiceUserRegistered;
import im.actor.model.entity.content.TextContent;
import im.actor.model.log.Log;
import im.actor.model.modules.Modules;
import im.actor.model.viewmodel.UserPresence;

public class I18nEngine {
    private final Modules modules;
    private final HashMap<String, String> locale;
    private final boolean is24Hours;
    private final String[] MONTHS_SHORT;
    private final String[] MONTHS;

    public I18nEngine(LocaleProvider provider, Modules modules) {
        this.modules = modules;
        this.locale = provider.loadLocale();
        this.is24Hours = provider.is24Hours();
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

    public String formatTyping() {
        return locale.get("Typing");
    }

    public String formatTyping(String name) {
        return locale.get("TypingUser").replace("{user}", name);
    }

    public String formatTyping(int count) {
        return locale.get("TypingMultiple").replace("{count}", String.valueOf(count));
    }

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

    public String formatTime(long date) {
        Date dateVal = new Date(date);
        if (is24Hours) {
            return dateVal.getHours() + ":" + formatTwoDigit(dateVal.getMinutes());
        } else {
            int hours = dateVal.getHours();
            if (hours > 12) {
                return (hours - 12) + ":" + formatTwoDigit(dateVal.getMinutes()) + " PM";
            } else {
                return hours + ":" + formatTwoDigit(dateVal.getMinutes()) + " AM";
            }
        }
    }

    public String formatDate(long date) {
        Date dateVal = new Date(date);
        return dateVal.getDate() + "/" + (dateVal.getMonth() + 1) + "/" + formatTwoDigit(dateVal.getYear());
    }

    public String formatPresence(UserPresence value, Sex sex) {
        if (value == null) {
            return null;
        }

        if (value.getState() == UserPresence.State.OFFLINE) {
            long currentTime = Environment.getCurrentSyncedTime() / 1000L;
            int delta = (int) (currentTime - value.getLastSeen());

            if (delta < 60) {
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

    public String formatDuration(int duration) {
        if (duration < 60) {
            return formatTwoDigit(0) + ":" + formatTwoDigit(duration);
        } else if (duration < 60 * 60) {
            return formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        } else {
            return formatTwoDigit(duration / 3600) + ":" + formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        }
    }

    public String formatGroupMembers(int count) {
        return locale.get("GroupMembers").replace("{count}", "" + count);
    }

    public String formatGroupOnline(int count) {
        return locale.get("GroupOnline").replace("{count}", "" + count);
    }

    public String formatContentDialogText(int senderId, ContentType contentType, String text, int relatedUid) {
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
            case EMPTY:
                return "";
            default:
            case UNKNOWN_CONTENT:
                return locale.get("ContentUnsupported");
        }
    }

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
                return true;
            default:
                return false;
        }
    }

    public String formatFullServiceMessage(int senderId, ServiceContent content) {
        if (content instanceof ServiceUserRegistered) {
            return getTemplateNamed(senderId, "ServiceRegisteredFull");
        } else if (content instanceof ServiceGroupCreated) {
            return getTemplateNamed(senderId, "ServiceGroupCreatedFull")
                    .replace("{title}",
                            ((ServiceGroupCreated) content).getGroupTitle());
        } else if (content instanceof ServiceGroupUserAdded) {
            return getTemplateNamed(senderId, "ServiceGroupAdded")
                    .replace("{name_added}",
                            getSubjectName(((ServiceGroupUserAdded) content).getAddedUid()));
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
        }

        Log.w("i18NEngine", "Unknown service content: " + content);

        return content.getCompatText();
    }

    public String formatPerformerName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return locale.get("You");
        } else {
            return getUser(uid).getName();
        }
    }

    public String getSubjectName(int uid) {
        if (uid == modules.getAuthModule().myUid()) {
            return locale.get("Thee");
        } else {
            return getUser(uid).getName();
        }
    }

    public String formatMessages(Message[] messages) {
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
                text += modules.getUsersModule().getUsers().getValue(model.getSenderId()).getName() + ": ";
                text += ((TextContent) model.getContent()).getText();
            }
        }
        return text;
    }

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
        return modules.getUsersModule().getUsers().getValue(uid);
    }
}
