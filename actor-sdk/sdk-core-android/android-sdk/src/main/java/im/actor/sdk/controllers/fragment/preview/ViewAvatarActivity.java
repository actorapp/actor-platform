package im.actor.sdk.controllers.fragment.preview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.viewmodel.AvatarUploadState;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.runtime.Log;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.util.Files;
import im.actor.sdk.util.images.common.ImageLoadException;
import im.actor.sdk.util.images.ops.ImageLoading;
import im.actor.runtime.files.FileSystemReference;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.Value;
import uk.co.senab.photoview.PhotoView;

import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.showView;
import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ViewAvatarActivity extends BaseActivity {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private String externalFile;
    private String avatarPath;
    private Peer peer;
    private PhotoView photoView;
    private View progress;
    private TextView noPhoto;
    private FileVM bindedDownloadFile;

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

        noPhoto = (TextView) findViewById(R.id.noPhoto);
        noPhoto.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (peer.getPeerType() == PeerType.PRIVATE && peer.getPeerId() == myUid()) {
            bind(getAvatar(), messenger().getOwnAvatarVM().getUploadState(), new ValueDoubleChangedListener<Avatar, AvatarUploadState>() {
                @Override
                public void onChanged(Avatar val, Value<Avatar> Value, AvatarUploadState val2, Value<AvatarUploadState> Value2) {
                    performBind(val, val2);
                }
            });
        } else if (peer.getPeerType() == PeerType.GROUP) {
            bind(getAvatar(), messenger().getGroupAvatarVM(peer.getPeerId()).getUploadState(), new ValueDoubleChangedListener<Avatar, AvatarUploadState>() {
                @Override
                public void onChanged(Avatar val, Value<Avatar> Value, AvatarUploadState val2, Value<AvatarUploadState> Value2) {
                    performBind(val, val2);
                }
            });
        } else if (peer.getPeerType() == PeerType.PRIVATE) {
            bind(getAvatar(), new ValueChangedListener<Avatar>() {
                @Override
                public void onChanged(Avatar val, Value<Avatar> Value) {
                    performBind(val, null);
                }
            });
        } else {
            throw new RuntimeException("Unknown peer type:" + peer.getPeerType());
        }
    }

    private Value<Avatar> getAvatar() {
        if (peer.getPeerType() == PeerType.GROUP) {
            return groups().get(peer.getPeerId()).getAvatar();
        } else {
            return users().get(peer.getPeerId()).getAvatar();
        }
    }


    private void performBind(Avatar avatar, AvatarUploadState uploadState) {
        unbind();

        if (uploadState != null && uploadState.isUploading()) {
            if (uploadState.getDescriptor() != null) {
                photoView.setImageURI(Uri.fromFile(new File(uploadState.getDescriptor())));
            } else {
                photoView.setImageURI(null);
            }
            showView(progress);
            goneView(noPhoto);
            return;
        }

        if (avatar == null || avatar.getFullImage() == null) {
            photoView.setImageBitmap(null);
            showView(noPhoto);
            goneView(progress);
        } else {
            goneView(noPhoto);

            // Large image
            String file = messenger().findDownloadedDescriptor(avatar.getFullImage().getFileReference().getFileId());
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
            String largeFile = messenger().findDownloadedDescriptor(avatar.getLargeImage().getFileReference().getFileId());
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
                String smallFile = messenger().findDownloadedDescriptor(avatar.getSmallImage().getFileReference().getFileId());
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

            bindedDownloadFile = messenger().bindFile(avatar.getFullImage().getFileReference(), true, new FileVMCallback() {
                @Override
                public void onNotDownloaded() {

                }

                @Override
                public void onDownloading(float progressV) {

                }

                @Override
                public void onDownloaded(FileSystemReference reference) {
                    try {
                        Bitmap bitmap = ImageLoading.loadBitmapOptimized(reference.getDescriptor());
                        photoView.setImageBitmap(bitmap);
                        photoView.setZoomable(true);

                        showView(photoView);
                        goneView(progress);
                    } catch (ImageLoadException e) {
                        e.printStackTrace();
                    }
                }
            });
            // receiver.request(new FullAvatarTask(avatar));
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

            new AlertDialog.Builder(this)
                    .setItems(args, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int which) {
                            if (which == 0) {
                                externalFile = Files.getExternalTempFile("capture", "jpg");
                                if (externalFile == null) {
                                    Toast.makeText(ViewAvatarActivity.this, R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (ContextCompat.checkSelfPermission(ViewAvatarActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    Log.d("Permissions", "camera - no permission :c");
                                    ActivityCompat.requestPermissions(ViewAvatarActivity.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            PERMISSIONS_REQUEST_CAMERA);

                                } else {
                                    startCamera();
                                }
                            } else if (which == 1) {
                                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                i.setType("image/*");
                                startActivityForResult(i, REQUEST_GALLERY);
                            } else if (which == 2) {
                                if (peer.getPeerType() == PeerType.PRIVATE) {
                                    if (peer.getPeerId() == myUid()) {
                                        messenger().removeMyAvatar();
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

    private void startCamera() {
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(externalFile))),
                REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            avatarPath = Files.getInternalTempFile("avatar", "jpg");
            Crop.of(data.getData(), Uri.fromFile(new File(avatarPath)))
                    .asSquare()
                    .start(this);
        } else if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {
            avatarPath = Files.getInternalTempFile("avatar", "jpg");
            Crop.of(Uri.fromFile(new File(externalFile)), Uri.fromFile(new File(avatarPath)))
                    .asSquare()
                    .start(this);
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            if (avatarPath == null) {
                return;
            }
            if (peer.getPeerType() == PeerType.PRIVATE) {
                if (peer.getPeerId() == myUid()) {
                    messenger().changeMyAvatar(avatarPath);
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

    private void unbind() {
        if (bindedDownloadFile != null) {
            bindedDownloadFile.detach();
            bindedDownloadFile = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }
}
