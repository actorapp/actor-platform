package im.actor.messenger.app.fragment.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.send.MessageDeliveryActor;
import im.actor.messenger.storage.scheme.messages.DialogItem;
import im.actor.messenger.util.RandomUtil;
import im.actor.messenger.util.io.IOUtils;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class ShareFragment extends BaseDialogFragment {

    public static ShareFragment share(String mimeType, String uri) {
        Bundle args = new Bundle();
        args.putString("MIME_TYPE", mimeType);
        args.putStringArray("DATA", new String[]{uri});
        ShareFragment fragment = new ShareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ShareFragment share(String mimeType, String[] uri) {
        Bundle args = new Bundle();
        args.putString("MIME_TYPE", mimeType);
        args.putStringArray("DATA", uri);
        ShareFragment fragment = new ShareFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String shareType;
    private String[] data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shareType = getArguments().getString("MIME_TYPE");
        data = getArguments().getStringArray("DATA");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void onItemClick(final DialogItem item) {
        String shareObject;
        if (shareType.startsWith("image")) {
            shareObject = "photo" + ((data.length > 1) ? "s" : "");
        } else if (shareType.startsWith("video")) {
            shareObject = "video" + ((data.length > 1) ? "s" : "");
        } else if (shareType.startsWith("text")) {
            shareObject = "text";
        } else {
            shareObject = "document" + ((data.length > 1) ? "s" : "");
        }

        new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure want to share " + shareObject + " to '" + item.getDialogTitle() + "'?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        performShare(item);
                    }
                })
                .setNegativeButton("No", null)
                .show()
                .setCanceledOnTouchOutside(true);
    }

    private void performShare(final DialogItem item) {
        // Sending text content
        if (shareType.startsWith("text")) {
            MessageDeliveryActor.messageSender().sendText(item.getType(), item.getId(), data[0]);
            startActivity(Intents.openDialog(item.getType(), item.getId(),false, getActivity()));
            getActivity().finish();
            return;
        }

        // Sending single local file
        if (data.length == 1) {
            Uri u = Uri.parse(data[0]);
            String[] path = getRealPathFromURI(u);
            if (path != null) {
                if (shareType.startsWith("video/")) {
                    MessageDeliveryActor.messageSender().sendVideo(item.getType(), item.getId(), path[0]);
                } else if (shareType.startsWith("image/")) {
                    MessageDeliveryActor.messageSender().sendPhoto(item.getType(), item.getId(), path[0]);
                } else {
                    MessageDeliveryActor.messageSender().sendDocument(item.getType(), item.getId(), path[0], path[1]);
                }
                startActivity(Intents.openDialog(item.getType(), item.getId(),false, getActivity()));
                getActivity().finish();
            }
            return;
        }

        // Performing async files loading
        new AsyncTask<Void, Void, Void>() {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Downloading...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < data.length; i++) {
                    Uri uri = Uri.parse(data[i]);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.MIME_TYPE,
                            MediaStore.Video.Media.TITLE};
                    Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                    // String mimeType = cursor.getString(cursor.getColumnIndex(filePathColumn[1]));
                    String fileName = cursor.getString(cursor.getColumnIndex(filePathColumn[2]));
//                if (mimeType == null) {
//                    mimeType = "?/?";
//                }
                    cursor.close();

                    if (picturePath == null || !uri.getScheme().equals("file")) {
                        File externalFile = AppContext.getContext().getExternalFilesDir(null);
                        if (externalFile == null) {
                            return null;
                        }
                        String externalPath = externalFile.getAbsolutePath();

                        File dest = new File(externalPath + "/Actor/");
                        dest.mkdirs();

                        File outputFile = new File(dest, "upload_" + RandomUtil.randomId() + ".jpg");
                        picturePath = outputFile.getAbsolutePath();

                        try {
                            IOUtils.copy(getActivity().getContentResolver().openInputStream(uri), new File(picturePath));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    if (shareType.startsWith("video/")) {
                        MessageDeliveryActor.messageSender().sendVideo(item.getType(), item.getId(), picturePath);
                    } else if (shareType.startsWith("image/")) {
                        MessageDeliveryActor.messageSender().sendPhoto(item.getType(), item.getId(), picturePath);
                    } else {
                        MessageDeliveryActor.messageSender().sendDocument(item.getType(), item.getId(), picturePath,
                                fileName);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                startActivity(Intents.openDialog(item.getType(), item.getId(),false, getActivity()));
                getActivity().finish();

                progressDialog.dismiss();
            }
        }.execute();
    }

    public String[] getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.TITLE};
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_index2 = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return new String[]{cursor.getString(column_index), cursor.getString(column_index2)};
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
