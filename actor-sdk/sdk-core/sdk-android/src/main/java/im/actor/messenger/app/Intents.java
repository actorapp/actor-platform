package im.actor.messenger.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Peer;
import im.actor.core.utils.IOUtils;
import im.actor.messenger.app.activity.AddContactActivity;
import im.actor.messenger.app.activity.TakePhotoActivity;
import im.actor.messenger.app.fragment.chat.ChatActivity;
import im.actor.messenger.app.fragment.group.GroupInfoActivity;
import im.actor.messenger.app.fragment.group.IntegrationTokenActivity;
import im.actor.messenger.app.fragment.group.InviteLinkActivity;
import im.actor.messenger.app.fragment.preview.PictureActivity;
import im.actor.messenger.app.fragment.profile.ProfileActivity;
import im.actor.messenger.app.fragment.settings.EditAboutActivity;
import im.actor.messenger.app.fragment.settings.EditNameActivity;

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

    public static Intent editGroupTheme(int groupId, Context context) {
        return new Intent(context, EditNameActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditNameActivity.TYPE_GROUP_THEME)
                .putExtra(EXTRA_EDIT_ID, groupId);
    }

    public static Intent editUserAbout(Context context) {
        return new Intent(context, EditAboutActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditAboutActivity.TYPE_ME)
                .putExtra(EXTRA_EDIT_ID, 0);
    }

    public static Intent editGroupAbout(int groupId, Context context) {
        return new Intent(context, EditAboutActivity.class)
                .putExtra(EXTRA_EDIT_TYPE, EditAboutActivity.TYPE_GROUP)
                .putExtra(EXTRA_EDIT_ID, groupId);
    }

    public static Intent openGroup(int chatId, Context context) {
        Intent res = new Intent(context, GroupInfoActivity.class);
        res.putExtra(EXTRA_GROUP_ID, chatId);
        return res;
    }

    public static Intent inviteLink(int chatId, Context context) {
        Intent res = new Intent(context, InviteLinkActivity.class);
        res.putExtra(EXTRA_GROUP_ID, chatId);
        return res;
    }

    public static Intent integrationToken(int chatId, Context context) {
        Intent res = new Intent(context, IntegrationTokenActivity.class);
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

    private static Uri getAvatarUri(FileReference location) {
        return Uri.parse("content://im.actor.avatar/" + location.getFileId());
    }

    public static Intent openDoc(String fileName, String downloadFileName) {
        String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(IOUtils.getFileExtension(fileName));
        if (mimeType == null) {
            mimeType = "*/*";
        }

        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(new File(downloadFileName)), mimeType);
    }

    public static Intent shareDoc(String fileName, String downloadFileName) {
        String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(IOUtils.getFileExtension(fileName));
        if (mimeType == null) {
            mimeType = "*/*";
        }

        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(downloadFileName)))
                .setType(mimeType);
    }

    public static Intent shareAvatar(FileReference location) {
        return new Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, getAvatarUri(location))
                .setType("image/jpeg");
    }

    public static Intent openAvatar(FileReference location) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(getAvatarUri(location), "image/jpeg");
    }

    public static Intent setAsAvatar(FileReference location) {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(getAvatarUri(location), "image/jpg");
        intent.putExtra("mimeType", "image/jpg");
        return intent;
    }

    public static Intent pickFile(Context context) {
        return com.droidkit.pickers.Intents.pickFile(context);
    }

    public static void openMedia(Activity activity, View photoView, String path, int senderId) {
        PictureActivity.launchPhoto(activity, photoView, path, senderId);
    }

    public static void savePicture(Context context, Bitmap bitmap) {

        File actorPicturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        actorPicturesFolder = new File(actorPicturesFolder, "Actor");
        actorPicturesFolder.mkdirs();
        try {
            File pictureFile = new File(actorPicturesFolder, System.currentTimeMillis()+".jpg");
            pictureFile.createNewFile();


            FileOutputStream ostream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();



            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(pictureFile);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
            Log.d("Picture saving", "Saved as " + pictureFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
