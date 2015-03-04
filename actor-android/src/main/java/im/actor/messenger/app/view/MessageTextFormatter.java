package im.actor.messenger.app.view;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.model.entity.Sex;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;

/**
 * Created by ex3ndr on 22.12.14.
 */
public class MessageTextFormatter {

    private static final int[] JOINED = new int[]{
            R.string.service_registered,
            R.string.service_registered_male,
            R.string.service_registered_female,
    };

    private static final int[] JOINED_FULL = new int[]{
            R.string.service_registered_full,
            R.string.service_registered_full_male,
            R.string.service_registered_full_female,
    };

    private static final int[] NEW_DEVICE = new int[]{
            R.string.service_new_device,
            R.string.service_new_device_male,
            R.string.service_new_device_female,
    };

    private static final int[] NEW_DEVICE_FULL = new int[]{
            R.string.service_new_device_full,
            R.string.service_new_device_full_male,
            R.string.service_new_device_full_female,
    };

    private static final int[] GROUP_CREATED = new int[]{
            R.string.service_created_you,
            R.string.service_created,
            R.string.service_created_male,
            R.string.service_created_female,
    };

    private static final int[] GROUP_CREATED_FULL = new int[]{
            R.string.service_created_you,
            R.string.service_created_full,
            R.string.service_created_full_male,
            R.string.service_created_full_female,
    };

    private static final int[] GROUP_LEAVE = new int[]{
            R.string.service_leave_you,
            R.string.service_leave,
            R.string.service_leave_male,
            R.string.service_leave_female,
    };

    private static final int[] GROUP_ADD = new int[]{
            R.string.service_add_you,
            R.string.service_add,
            R.string.service_add_male,
            R.string.service_add_female,
    };

    private static final int[] GROUP_KICK = new int[]{
            R.string.service_kicked_you,
            R.string.service_kicked,
            R.string.service_kicked_male,
            R.string.service_kicked_female,
    };

    private static final int[] GROUP_CHANGE_TITLE = new int[]{
            R.string.service_changed_title_you,
            R.string.service_changed_title,
            R.string.service_changed_title_male,
            R.string.service_changed_title_female,
    };

    private static final int[] GROUP_CHANGE_TITLE_FULL = new int[]{
            R.string.service_changed_title_full_you,
            R.string.service_changed_title_full,
            R.string.service_changed_title_full_male,
            R.string.service_changed_title_full_female,
    };

    private static final int[] GROUP_CHANGE_AVATAR = new int[]{
            R.string.service_changed_avatar_you,
            R.string.service_changed_avatar,
            R.string.service_changed_avatar_male,
            R.string.service_changed_avatar_female,
    };

    private static final int[] GROUP_REMOVE_AVATAR = new int[]{
            R.string.service_removed_avatar_you,
            R.string.service_removed_avatar,
            R.string.service_removed_avatar_male,
            R.string.service_removed_avatar_female,
    };

    public static CharSequence textMessage(int uid, boolean isGroup, String message) {
        if (isGroup) {
            String senderName = getUserName(uid, true);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(senderName).append(": ");
            builder.append(message);
            builder.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.text_secondary)),
                    senderName.length() + 2, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return builder;
        } else {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(message);
            builder.setSpan(new ForegroundColorSpan(AppContext.getContext().getResources().getColor(R.color.text_secondary)),
                    0, builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return builder;
        }
    }

    public static CharSequence photoMessage(int uid, boolean isGroup) {
        if (isGroup) {
            return getUserName(uid, true) + ": " + getString(R.string.dialogs_photo);
        } else {
            return getString(R.string.dialogs_photo);
        }
    }

    public static CharSequence videoMessage(int uid, boolean isGroup) {
        if (isGroup) {
            return getUserName(uid, true) + ": " + getString(R.string.dialogs_video);
        } else {
            return getString(R.string.dialogs_video);
        }
    }

    public static CharSequence documentMessage(int uid, boolean isGroup) {
        if (isGroup) {
            return getUserName(uid, true) + ": " + getString(R.string.dialogs_document);
        } else {
            return getString(R.string.dialogs_document);
        }
    }

    public static CharSequence audioMessage(int uid, boolean isGroup) {
        if (isGroup) {
            return getUserName(uid, true) + ": " + getString(R.string.dialogs_audio);
        } else {
            return getString(R.string.dialogs_audio);
        }
    }

    public static String joinedActor(int uid) {
        return getSexString(JOINED, getUserSex(uid));
    }

    public static String joinedActorFull(int uid) {
        return getSexString(JOINED_FULL, getUserSex(uid), getUserName(uid, true));
    }

    public static String newDevice(int uid) {
        return getSexString(NEW_DEVICE, getUserSex(uid));
    }

    public static String newDeviceFull(int uid) {
        return getSexString(NEW_DEVICE_FULL, getUserSex(uid), getUserName(uid, true));
    }

    public static String groupCreated(int uid) {
        return getUidString(GROUP_CREATED, uid, getUserName(uid, true));
    }

    public static String groupCreatedFull(int uid, String groupTitle) {
        return getUidString(GROUP_CREATED_FULL, uid, getUserName(uid, true), groupTitle);
    }

    public static String groupLeave(int uid) {
        return getUidString(GROUP_LEAVE, uid, getUserName(uid, true));
    }

    public static String groupAdd(int uid, int addedUid) {
        return getUidString(GROUP_ADD, uid, getUserName(uid, true), getUserName(addedUid, false));
    }

    public static String groupKicked(int uid, int kickedUid) {
        return getUidString(GROUP_KICK, uid, getUserName(uid, true), getUserName(kickedUid, false));
    }

    public static String groupChangeTitle(int uid) {
        return getUidString(GROUP_CHANGE_TITLE, uid, getUserName(uid, true));
    }

    public static String groupChangeTitleFull(int uid, String title) {
        return getUidString(GROUP_CHANGE_TITLE_FULL, uid, getUserName(uid, true), title);
    }

    public static String groupChangeAvatar(int uid) {
        return getUidString(GROUP_CHANGE_AVATAR, uid, getUserName(uid, true));
    }

    public static String groupRemoveAvatar(int uid) {
        return getUidString(GROUP_REMOVE_AVATAR, uid, getUserName(uid, true));
    }

    // Toos methods

    private static Sex getUserSex(int uid) {
        UserVM u = users().get(uid);
        if (u != null) {
            return u.getSex();
        } else {
            return Sex.UNKNOWN;
        }
    }

    private static String getUserName(int uid, boolean author) {
        if (uid == myUid()) {
            if (author) {
                return getString(R.string.service_you);
            } else {
                return getString(R.string.service_your);
            }
        }
        UserVM u = users().get(uid);
        if (u != null) {
            return u.getName().get();
        } else {
            return "#" + uid;
        }
    }

    private static String getSexString(int[] ids, Sex sex) {
        int id = sex == Sex.UNKNOWN
                ? ids[0]
                : sex == Sex.MALE
                ? ids[1]
                : ids[2];
        return getString(id);
    }

    private static String getUidString(int[] ids, int uid) {
        Sex sex = getUserSex(uid);
        int id = uid == myUid()
                ? ids[0]
                : sex == Sex.UNKNOWN
                ? ids[1]
                : sex == Sex.MALE
                ? ids[2]
                : ids[3];
        return getString(id);
    }

    private static String getUidString(int[] ids, int uid, Object v1) {
        return getUidString(ids, uid)
                .replace("{0}", v1 + "");
    }

    private static String getUidString(int[] ids, int uid, Object v1, Object v2) {
        return getUidString(ids, uid)
                .replace("{0}", v1 + "")
                .replace("{1}", v2 + "");
    }

    private static String getSexString(int[] ids, Sex sex, Object v1) {
        return getSexString(ids, sex)
                .replace("{0}", v1 + "");
    }

    private static String getSexString(int[] ids, Sex sex, Object v1, Object v2) {
        return getSexString(ids, sex)
                .replace("{0}", v1 + "")
                .replace("{1}", v2 + "");
    }

    private static String getString(int id) {
        return AppContext.getContext().getString(id);
    }

    private static String getString(int id, Object v1) {
        return getString(id)
                .replace("{0}", v1 + "");
    }

    private static String getString(int id, Object v1, Object v2) {
        return getString(id)
                .replace("{0}", v1 + "")
                .replace("{1}", v2 + "");
    }
}
