package im.actor.messenger.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import im.actor.images.cache.BitmapReference;
import im.actor.images.common.ImageLoadException;
import im.actor.images.loading.ImageReceiver;
import im.actor.images.loading.ReceiverCallback;
import im.actor.images.ops.ImageLoading;
import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.Core;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.base.BaseActivity;
import im.actor.messenger.app.images.FullAvatarTask;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueDoubleChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.AvatarUploadState;
import uk.co.senab.photoview.PhotoView;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;

/**
 * Created by ex3ndr on 29.10.14.
 */
public class ViewAvatarActivity extends BaseActivity {

    public static Intent viewAvatar(int uid, Context context) {
        Intent res = new Intent(context, ViewAvatarActivity.class);
        res.putExtra(Intents.EXTRA_CHAT_PEER, Peer.user(uid).getUnuqueId());
        return res;
    }

    public static Intent viewGroupAvatar(int gid, Context context) {
        Intent res = new Intent(context, ViewAvatarActivity.class);
        res.putExtra(Intents.EXTRA_CHAT_PEER, Peer.group(gid).getUnuqueId());
        return res;
    }

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_PHOTO = 2;

    private String externalFile;
    private String avatarPath;

    private Peer peer;

    private ImageReceiver receiver;
    private PhotoView photoView;
    private View progress;
    private View noPhoto;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        peer = Peer.fromUniqueId(getIntent().getLongExtra(Intents.EXTRA_CHAT_PEER, 0));

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);

        if (savedInstanceState != null) {
            externalFile = savedInstanceState.getString("externalFile", null);
            avatarPath = savedInstanceState.getString("avatarPath", null);
        }

        if (peer.getPeerType() == PeerType.PRIVATE) {
            if (peer.getPeerId() == myUid()) {
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

        receiver = Core.getImageLoader().createReceiver(new ReceiverCallback() {
            @Override
            public void onImageLoaded(BitmapReference bitmap) {
                photoView.setImageBitmap(bitmap.getBitmap());
                photoView.setZoomable(true);
                showView(photoView);
                if (!isUploading) {
                    goneView(progress);
                }
            }

            @Override
            public void onImageCleared() {

            }

            @Override
            public void onImageError() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindImage();
    }

    private ValueModel<Avatar> getAvatar() {
        if (peer.getPeerType() == PeerType.GROUP) {
            return groups().get(peer.getPeerId()).getAvatar();
        } else {
            return users().get(peer.getPeerId()).getAvatar();
        }
    }

    private void bindImage() {
        if (peer.getPeerType() == PeerType.PRIVATE && peer.getPeerId() == myUid()) {
            bind(getAvatar(), messenger().getOwnAvatarVM().getUploadState(), new ValueDoubleChangedListener<Avatar, AvatarUploadState>() {
                @Override
                public void onChanged(Avatar val, ValueModel<Avatar> valueModel, AvatarUploadState val2, ValueModel<AvatarUploadState> valueModel2) {
                    performBind(val, val2);
                }
            });
        } else if (peer.getPeerType() == PeerType.GROUP) {
            bind(getAvatar(), messenger().getGroupAvatarVM(peer.getPeerId()).getUploadState(), new ValueDoubleChangedListener<Avatar, AvatarUploadState>() {
                @Override
                public void onChanged(Avatar val, ValueModel<Avatar> valueModel, AvatarUploadState val2, ValueModel<AvatarUploadState> valueModel2) {
                    performBind(val, val2);
                }
            });
        } else {
            bind(getAvatar(), new ValueChangedListener<Avatar>() {
                @Override
                public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                    performBind(val, null);
                }
            });
        }
    }

    private void performBind(Avatar avatar, AvatarUploadState uploadState) {
        if (uploadState != null && uploadState.isUploading()) {
            if (uploadState.getDescriptor() != null) {
                photoView.setImageURI(Uri.fromFile(new File(uploadState.getDescriptor())));
            } else {
                photoView.setImageURI(null);
            }
            showView(progress);
            goneView(noPhoto);
            isUploading = true;
            return;
        }

        isUploading = false;
        receiver.clear();
        if (avatar == null || avatar.getFullImage() == null) {
            photoView.setImageBitmap(null);
            showView(noPhoto);
            goneView(progress);
        } else {
            goneView(noPhoto);

            // Large image
            String file = messenger().getDownloadedDescriptor(avatar.getFullImage().getFileReference().getFileId());
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

            // Full image not available: showing progress

            showView(progress);

            // Trying to show preview first
            boolean isAppliedPreview = false;
            String largeFile = messenger().getDownloadedDescriptor(avatar.getLargeImage().getFileReference().getFileId());
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
                String smallFile = messenger().getDownloadedDescriptor(avatar.getSmallImage().getFileReference().getFileId());
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

            receiver.request(new FullAvatarTask(avatar));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.avatar, menu);

        if (peer.getPeerType() == PeerType.GROUP) {
            menu.findItem(R.id.editAvatar).setVisible(true);
        } else if (peer.getPeerType() == PeerType.PRIVATE && peer.getPeerId() == myUid()) {
            menu.findItem(R.id.editAvatar).setVisible(true);
        } else {
            menu.findItem(R.id.editAvatar).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editAvatar) {
            CharSequence[] args;
            if (getAvatar().get() != null) {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery),
                        getString(R.string.pick_photo_remove)};
            } else {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery)};
            }
            new MaterialDialog.Builder(this)
                    .items(args)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
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
                                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                i.setType("image/*");
                                startActivityForResult(i, REQUEST_GALLERY);
                            } else if (which == 2) {
                                if (peer.getPeerType() == PeerType.PRIVATE) {
                                    if (peer.getPeerId() == myUid()) {
                                        messenger().removeAvatar();
                                    }
                                } else if (peer.getPeerType() == PeerType.GROUP) {
                                    messenger().removeGroupAvatar(peer.getPeerId());
                                }
                            }
                        }
                    })
                    .show();

            return true;
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
            if (peer.getPeerType() == PeerType.PRIVATE) {
                if (peer.getPeerId() == myUid()) {
                    messenger().changeAvatar(avatarPath);
                }
            } else if (peer.getPeerType() == PeerType.GROUP) {
                messenger().changeGroupAvatar(peer.getPeerId(), avatarPath);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (avatarPath != null) {
            outState.putString("avatarPath", avatarPath);
        }
        if (externalFile != null) {
            outState.putString("externalFile", externalFile);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.close();
    }
}
