package im.actor.messenger.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import im.actor.messenger.BuildConfig;
import im.actor.messenger.app.activity.AddContactActivity;
import im.actor.messenger.app.fragment.chat.ChatActivity;
import im.actor.messenger.app.activity.DocumentsActivity;
import im.actor.messenger.app.activity.EditNameActivity;
import im.actor.messenger.app.activity.GroupInfoActivity;
import im.actor.messenger.app.fragment.group.PickUserActivity;
import im.actor.messenger.app.activity.ProfileActivity;
import im.actor.messenger.app.activity.TakePhotoActivity;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class Intents {

    public static final String EXTRA_UID = "uid";

    public static final String EXTRA_GROUP_ID = "group_id";

    public static final String EXTRA_CHAT_PEER = "chat_peer";
    public static final String EXTRA_CHAT_COMPOSE = "compose";

    public static final String EXTRA_EDIT_TYPE = "edit_type";
    public static final String EXTRA_EDIT_ID = "edit_id";

    public static final int RESULT_DELETE = 0;
    public static final int RESULT_IMAGE = 1;

    public static final String EXTRA_ALLOW_DELETE = "allow_delete";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_IMAGE = "image";

    public static Intent pickAvatar(boolean isAllowDelete, Context context) {
        return new Intent(context, TakePhotoActivity.class)
                .putExtra(EXTRA_ALLOW_DELETE, isAllowDelete);
    }

    public static Intent editMyName(Context context) {
        return new Intent(context, EditNameActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditNameActivity.TYPE_ME)
                .putExtra(EXTRA_EDIT_ID, 0);
    }

    public static Intent editUserName(int uid, Context context) {
        return new Intent(context, EditNameActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditNameActivity.TYPE_USER)
                .putExtra(EXTRA_EDIT_ID, uid);
    }

    public static Intent editGroupTitle(int groupId, Context context) {
        return new Intent(context, EditNameActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditNameActivity.TYPE_GROUP)
                .putExtra(EXTRA_EDIT_ID, groupId);
    }

    public static Intent openGroup(int chatId, Context context) {
        Intent res = new Intent(context, GroupInfoActivity.class);
        res.putExtra(EXTRA_GROUP_ID, chatId);
        return res;
    }

    public static Intent openDialog(Peer peer, boolean compose, Context context) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        intent.putExtra(EXTRA_CHAT_COMPOSE, compose);
        return intent;
    }

    public static Intent openPrivateDialog(int uid, boolean compose, Context context) {
        return openDialog(Peer.user(uid), compose, context);
    }

    public static Intent openGroupDialog(int chatId, boolean compose, Context context) {
        return openDialog(Peer.group(chatId), compose, context);
    }

    public static Intent openProfile(int uid, Context context) {
        return new Intent(context, ProfileActivity.class).putExtra(EXTRA_UID, uid);
    }

    public static Intent pickUser(Context context) {
        return new Intent(context, PickUserActivity.class);
    }

    public static Intent call(long phone) {
        return call(phone + "");
    }

    public static Intent call(String phone) {
        return new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:+" + phone));
    }

    public static Intent findContacts(Context context) {
        return new Intent(context, AddContactActivity.class);
    }

    // External intents

    public static Intent openDocs(Peer peer, Context context) {
        final Intent intent = new Intent(context, DocumentsActivity.class);
        intent.putExtra(EXTRA_CHAT_PEER, peer.getUnuqueId());
        return intent;
    }

//    private static Uri getDocUri(Downloaded downloaded) {
//        return Uri.fromFile(new File(downloaded.getDownloadedPath()));
////        if (BuildConfig.ENABLE_CHROME) {
////            return Uri.fromFile(new File(downloaded.getDownloadedPath()));
////        } else {
////            return Uri.parse("content://im.actor.media/" + downloaded.getFileId());
////        }
//    }

    private static Uri getAvatarUri(FileReference location) {
        return Uri.parse("content://im.actor.avatar/" + location.getFileId());
    }

//    public static Intent shareDoc(Downloaded downloaded) {
//        String mimeType = MimeTypeMap.getSingleton()
//                .getMimeTypeFromExtension(IOUtils.getFileExtension(downloaded.getName()));
//        if (mimeType == null) {
//            mimeType = "*/*";
//        }
//
//        return new Intent(Intent.ACTION_SEND)
//                .putExtra(Intent.EXTRA_STREAM, getDocUri(downloaded))
//                .setType(mimeType);
//    }
//
//    public static Intent openDoc(Downloaded downloaded) {
//        String mimeType = MimeTypeMap.getSingleton()
//                .getMimeTypeFromExtension(IOUtils.getFileExtension(downloaded.getName()));
//        if (mimeType == null) {
//            mimeType = "*/*";
//        }
//
//        if (BuildConfig.ENABLE_CHROME) {
//            return shareDoc(downloaded);
//        } else {
//            return new Intent(Intent.ACTION_VIEW)
//                    .setDataAndType(getDocUri(downloaded), mimeType);
//        }
//    }
//
//    public static Intent sharePhoto(Downloaded downloaded) {
//        return new Intent(Intent.ACTION_SEND)
//                .putExtra(Intent.EXTRA_STREAM, getDocUri(downloaded))
//                .setType("image/jpeg");
//    }

    public static Intent shareAvatar(FileReference location) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, getAvatarUri(location))
                .setType("image/jpeg");
    }

    public static Intent openAvatar(FileReference location) {
        if (BuildConfig.ENABLE_CHROME) {
            return shareAvatar(location);
        } else {
            return new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(getAvatarUri(location), "image/jpeg");
        }
    }

    public static Intent setAsAvatar(FileReference location) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(getAvatarUri(location), "image/jpg");
        intent.putExtra("mimeType", "image/jpg");
        return intent;
    }

//    public static Intent openPhoto(Downloaded downloaded) {
//        if (BuildConfig.ENABLE_CHROME) {
//            return sharePhoto(downloaded);
//        } else {
//            return new Intent(Intent.ACTION_VIEW)
//                    .setDataAndType(getDocUri(downloaded), "image/jpeg");
//        }
//    }
//
//    public static Intent shareVideo(Downloaded downloaded) {
//        return new Intent(Intent.ACTION_SEND)
//                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(downloaded.getDownloadedPath())))
//                .setType("image/jpeg");
//    }
//
//    public static Intent openVideo(Downloaded downloaded) {
//        if (BuildConfig.ENABLE_CHROME) {
//            return shareVideo(downloaded);
//        } else {
//            return new Intent(Intent.ACTION_VIEW)
//                    .setDataAndType(getDocUri(downloaded), "video/mp4");
//        }
//    }

    public static Intent pickFile(Context context) {
        if (BuildConfig.ENABLE_CHROME) {
            return new Intent(Intent.ACTION_PICK).setType("*/*");
        } else {
            return com.droidkit.pickers.Intents.pickFile(context);
        }
    }
}
