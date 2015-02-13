package im.actor.messenger.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.droidkit.images.cache.BitmapReference;
import com.droidkit.images.cache.DiskCache;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.loading.ImageReceiver;
import com.droidkit.images.loading.ReceiverCallback;
import com.droidkit.images.loading.tasks.RawFileTask;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.mvvm.ValueModel;
import com.droidkit.mvvm.ui.Listener;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseBarActivity;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.Core;
import im.actor.messenger.core.actors.groups.GroupAvatarActor;
import im.actor.messenger.core.actors.groups.GroupAvatarState;
import im.actor.messenger.core.actors.profile.AvatarChangeActor;
import im.actor.messenger.core.actors.profile.AvatarChangeState;
import im.actor.messenger.core.images.FileKeys;
import im.actor.messenger.core.images.FullAvatarTask;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import uk.co.senab.photoview.PhotoView;

import static im.actor.messenger.app.view.ViewUtils.*;
import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 29.10.14.
 */
public class ViewAvatarActivity extends BaseBarActivity {

    public static Intent viewAvatar(int uid, Context context) {
        Intent res = new Intent(context, ViewAvatarActivity.class);
        res.putExtra(Intents.EXTRA_CHAT_TYPE, DialogType.TYPE_USER);
        res.putExtra(Intents.EXTRA_CHAT_ID, uid);
        return res;
    }

    public static Intent viewGroupAvatar(int gid, Context context) {
        Intent res = new Intent(context, ViewAvatarActivity.class);
        res.putExtra(Intents.EXTRA_CHAT_TYPE, DialogType.TYPE_GROUP);
        res.putExtra(Intents.EXTRA_CHAT_ID, gid);
        return res;
    }

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_PHOTO = 2;

    private String externalFile;
    private String avatarPath;

    private int chatType;
    private int chatId;

    private ImageReceiver receiver;
    private PhotoView photoView;
    private View progress;
    private View noPhoto;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatType = getIntent().getIntExtra(Intents.EXTRA_CHAT_TYPE, 0);
        chatId = getIntent().getIntExtra(Intents.EXTRA_CHAT_ID, 0);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);

        if (chatType == DialogType.TYPE_USER) {
            if (chatId == myUid()) {
                getSupportActionBar().setTitle(R.string.avatar_title_your);
            } else {
                getSupportActionBar().setTitle(R.string.avatar_title_person);
            }
        } else {
            getSupportActionBar().setTitle(R.string.avatar_title_group);
        }

        setContentView(R.layout.activity_avatar);

        photoView = (PhotoView) findViewById(R.id.avatar);

        progress = findViewById(R.id.uploadProgress);

        noPhoto = findViewById(R.id.noPhoto);

        receiver = Core.core().getImageLoader().createReceiver(new ReceiverCallback() {
            @Override
            public void onImageLoaded(BitmapReference bitmap) {
                photoView.setImageBitmap(bitmap.getBitmap());
                photoView.setZoomable(true);
                showView(photoView);
                if (!isUploading) {
                    goneView(progress);
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onImageCleared() {

            }

            @Override
            public void onImageError() {

            }
        });
    }

    private ValueModel<Avatar> getAvatar() {
        if (chatType == DialogType.TYPE_GROUP) {
            return groups().get(chatId).getAvatarModel();
        } else {
            return users().get(chatId).getAvatar();
        }
    }

    private void bindImage() {

        getBinder().bind(getAvatar(), new Listener<Avatar>() {
            @Override
            public void onUpdated(Avatar avatar) {
                performBind(avatar);
            }
        });
        if (chatType == DialogType.TYPE_USER) {
            if (chatId == myUid()) {
                getBinder().bind(AvatarChangeState.uploadingState(), new Listener<AvatarChangeState.State>() {
                    @Override
                    public void onUpdated(AvatarChangeState.State state) {
                        performBind(getAvatar().getValue());
                    }
                });
            }
        } else if (chatType == DialogType.TYPE_GROUP) {
            getBinder().bind(GroupAvatarState.getGroupState(chatId), new Listener<GroupAvatarState.StateHolder>() {
                @Override
                public void onUpdated(GroupAvatarState.StateHolder stateHolder) {
                    performBind(getAvatar().getValue());
                }
            });
        }
    }

    private void performBind(Avatar avatar) {
        if (chatType == DialogType.TYPE_USER) {
            if (chatId == myUid()) {
                if (AvatarChangeState.uploadingState().getValue() == AvatarChangeState.State.UPLOADING) {
                    if (AvatarChangeState.getFileName() != null) {
                        receiver.request(new RawFileTask(AvatarChangeState.getFileName()));
                    } else {
                        receiver.clear();
                    }
                    showView(progress);
                    goneView(noPhoto);
                    isUploading = true;
                    return;
                }
            }
        } else if (chatType == DialogType.TYPE_GROUP) {
            GroupAvatarState.StateHolder uploadState =
                    GroupAvatarState.getGroupState(chatId).getValue();
            if (uploadState.getState() == GroupAvatarState.State.UPLOADING) {
                if (uploadState.getFileName() != null) {
                    receiver.request(new RawFileTask(uploadState.getFileName()));
                } else {
                    receiver.clear();
                }
                showView(progress);
                goneView(noPhoto);
                isUploading = true;
                return;
            }
        }

        isUploading = false;
        receiver.clear();
        if (avatar == null) {
            photoView.setImageBitmap(null);
            showView(noPhoto);
            goneView(progress);
        } else {
            goneView(noPhoto);
            DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();

            String file = diskCache.lockFile(FileKeys.avatarKey(avatar.getFullImage().getFileLocation().getFileId()));
            if (file != null) {
                try {
                    Bitmap bitmap = ImageLoading.loadBitmapOptimized(file);
                    photoView.setImageBitmap(bitmap);
                    photoView.setZoomable(true);
                    goneView(progress);
                    return;
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                }
            }

            showView(progress);

            boolean isAppliedPreview = false;

            String largeFile = diskCache.lockFile(FileKeys.avatarKey(avatar.getLargeImage().getFileLocation().getFileId()));
            if (largeFile != null) {
                try {
                    Bitmap bitmap = ImageLoading.loadBitmapOptimized(largeFile);
                    photoView.setImageBitmap(bitmap);
                    photoView.setZoomable(false);
                    isAppliedPreview = true;
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                }
            }

            if (!isAppliedPreview) {
                String smallFile = diskCache.lockFile(FileKeys.avatarKey(avatar.getSmallImage().getFileLocation().getFileId()));
                if (smallFile != null) {
                    try {
                        Bitmap bitmap = ImageLoading.loadBitmapOptimized(smallFile);
                        photoView.setImageBitmap(bitmap);
                        photoView.setZoomable(false);
                    } catch (ImageLoadException e) {
                        e.printStackTrace();
                    }
                }
            }

            showView(progress);
            receiver.request(new FullAvatarTask(avatar));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.avatar, menu);

        DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();
        Avatar avatar = getAvatar().getValue();
        String file = avatar == null ? null : diskCache.lockFile(FileKeys.avatarKey(
                avatar.getFullImage().getFileLocation().getFileId()));
        menu.findItem(R.id.shareAvatar).setVisible(file != null);
        menu.findItem(R.id.setAs).setVisible(file != null);
        if (chatType == DialogType.TYPE_GROUP) {
            menu.findItem(R.id.editAvatar).setVisible(true);
        } else {
            menu.findItem(R.id.editAvatar).setVisible(chatId == myUid());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editAvatar) {
            CharSequence[] args;
            if (getAvatar().getValue() != null) {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery),
                        getString(R.string.pick_photo_remove)};
            } else {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery)};
            }
            new AlertDialog.Builder(this)
                    .setItems(args, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                externalFile = AppContext.getExternalTempFile("capture", "jpg");
                                if (externalFile == null) {
                                    Toast.makeText(ViewAvatarActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                startActivityForResult(
                                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(externalFile))),
                                        REQUEST_PHOTO);
                            } else if (which == 1) {
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                i.setType("image/*");
                                startActivityForResult(i, REQUEST_GALLERY);
                            } else if (which == 2) {
                                if (chatType == DialogType.TYPE_USER) {
                                    if (chatId == myUid()) {
                                        AvatarChangeActor.avatarSender().clearAvatar();
                                    }
                                } else if (chatType == DialogType.TYPE_GROUP) {
                                    GroupAvatarActor.get().clearAvatar(chatId);
                                }
                            }
                        }
                    }).show()
                    .setCanceledOnTouchOutside(true);
            return true;
        } else if (item.getItemId() == R.id.shareAvatar) {
            DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();
            String file = diskCache.lockFile(FileKeys.avatarKey(getAvatar().getValue().getFullImage().getFileLocation().getFileId()));
            if (file != null) {
                startActivity(Intents.shareAvatar(getAvatar().getValue().getFullImage().getFileLocation()));
            }
        } else if (item.getItemId() == R.id.setAs) {
            DiskCache diskCache = Core.core().getImageLoader().getInternalDiskCache();
            String file = diskCache.lockFile(FileKeys.avatarKey(getAvatar().getValue().getFullImage().getFileLocation().getFileId()));
            if (file != null) {
                startActivity(Intents.setAsAvatar(getAvatar().getValue().getFullImage().getFileLocation()));
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            avatarPath = AppContext.getInternalTempFile("avatar", "jpg");
            new Crop(data.getData())
                    .output(Uri.fromFile(new File(avatarPath)))
                    .asSquare()
                    .start(this);
        } else if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {
            avatarPath = AppContext.getInternalTempFile("avatar", "jpg");
            new Crop(Uri.fromFile(new File(externalFile)))
                    .output(Uri.fromFile(new File(avatarPath)))
                    .asSquare()
                    .start(this);
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            if (chatType == DialogType.TYPE_USER) {
                if (chatId == myUid()) {
                    AvatarChangeActor.avatarSender().changeAvatar(avatarPath);
                }
            } else if (chatType == DialogType.TYPE_GROUP) {
                GroupAvatarActor.get().changeAvatar(chatId, avatarPath);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.close();
    }
}
