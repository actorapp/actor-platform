package im.actor.messenger.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.AppContext;

/**
 * Created by ex3ndr on 09.01.15.
 */
public class TakePhotoActivity extends Activity {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_PHOTO = 2;

    private boolean isAllowDelete = false;
    private boolean isPerformedAction = false;

    private String tempAvatarPath;
    private String externalFile;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isPerformedAction = savedInstanceState.getBoolean("isPerformedAction");
            isAllowDelete = savedInstanceState.getBoolean("isAllowDelete");
            externalFile = savedInstanceState.getString("externalFile");
            tempAvatarPath = savedInstanceState.getString("tempAvatarPath");
        } else {
            isAllowDelete = getIntent().getBooleanExtra(Intents.EXTRA_ALLOW_DELETE, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPerformedAction) {
            CharSequence[] args;
            if (isAllowDelete) {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery),
                        getString(R.string.pick_photo_remove)};
            } else {
                args = new CharSequence[]{getString(R.string.pick_photo_camera),
                        getString(R.string.pick_photo_gallery)};
            }
            dialog = new AlertDialog.Builder(this)
                    .setItems(args, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            if (which == 0) {
                                externalFile = AppContext.getExternalTempFile("capture", "jpg");
                                if (externalFile == null) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.toast_no_sdcard, Toast.LENGTH_LONG).show();
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
                                setResult(RESULT_OK, new Intent().putExtra(Intents.EXTRA_RESULT, Intents.RESULT_DELETE));
                                finish();
                            }
                            isPerformedAction = true;
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    }).show();
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            tempAvatarPath = AppContext.getInternalTempFile("avatar", "jpg");
            Crop.of(data.getData(), Uri.fromFile(new File(tempAvatarPath)))

                    .asSquare()
                    .start(this);
            return;
        } else if (requestCode == REQUEST_PHOTO && resultCode == Activity.RESULT_OK) {
            tempAvatarPath = AppContext.getInternalTempFile("avatar", "jpg");
            Crop.of(Uri.fromFile(new File(externalFile)), Uri.fromFile(new File(tempAvatarPath)))
                    .asSquare()
                    .start(this);
            return;
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK, new Intent()
                    .putExtra(Intents.EXTRA_RESULT, Intents.RESULT_IMAGE)
                    .putExtra(Intents.EXTRA_IMAGE, tempAvatarPath));
            finish();
            return;
        }

        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPerformedAction", isPerformedAction);
        outState.putBoolean("isAllowDelete", isAllowDelete);
        if (externalFile != null) {
            outState.putString("externalFile", externalFile);
        }
        if (tempAvatarPath != null) {
            outState.putString("tempAvatarPath", tempAvatarPath);
        }
    }
}