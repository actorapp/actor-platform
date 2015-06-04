/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import im.actor.android.images.ImageHelper;
import im.actor.model.BaseMessenger;
import im.actor.model.Configuration;
import im.actor.model.MessengerEnvironment;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.FastThumb;
import im.actor.model.network.NetworkState;

public class AndroidBaseMessenger extends BaseMessenger {
    private Context context;
    private final Random random = new Random();

    public AndroidBaseMessenger(Context context, Configuration configuration) {
        super(MessengerEnvironment.ANDROID, configuration);

        this.context = context;

        // Catch all phone book changes
        context.getContentResolver()
                .registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                        new ContentObserver(null) {
                            @Override
                            public void onChange(boolean selfChange) {
                                onPhoneBookChanged();
                            }
                        });

        // Catch network change
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                NetworkState state;
                if (isConnected) {
                    switch (activeNetwork.getType()) {
                        case ConnectivityManager.TYPE_WIFI:
                        case ConnectivityManager.TYPE_WIMAX:
                        case ConnectivityManager.TYPE_ETHERNET:
                            state = NetworkState.WI_FI;
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                            state = NetworkState.MOBILE;
                            break;
                        default:
                            state = NetworkState.UNKNOWN;
                    }
                } else {
                    state = NetworkState.NO_CONNECTION;
                }
                onNetworkChanged(state);
            }
        }, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void changeGroupAvatar(int gid, String descriptor) {
        try {
            Bitmap bmp = ImageHelper.loadOptimizedHQ(descriptor);
            if (bmp == null) {
                return;
            }
            String resultFileName = getExternalTempFile("image", "jpg");
            if (resultFileName == null) {
                return;
            }
            ImageHelper.save(bmp, resultFileName);

            super.changeGroupAvatar(gid, resultFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void changeMyAvatar(String descriptor) {
        try {
            Bitmap bmp = ImageHelper.loadOptimizedHQ(descriptor);
            if (bmp == null) {
                return;
            }
            String resultFileName = getExternalTempFile("image", "jpg");
            if (resultFileName == null) {
                return;
            }
            ImageHelper.save(bmp, resultFileName);

            super.changeMyAvatar(resultFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(Peer peer, String fullFilePath) {
        sendDocument(peer, fullFilePath, new File(fullFilePath).getName());
    }

    public void sendDocument(Peer peer, String fullFilePath, String fileName) {

        int dot = fileName.indexOf('.');
        String mimeType = null;
        if (dot >= 0) {
            String ext = fileName.substring(dot + 1);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        Bitmap fastThumb = ImageHelper.loadOptimizedHQ(fullFilePath);
        if (fastThumb != null) {
            fastThumb = ImageHelper.scaleFit(fastThumb, 90, 90);
            byte[] fastThumbData = ImageHelper.save(fastThumb);
            sendDocument(peer, fileName, mimeType,
                    new FastThumb(fastThumb.getWidth(), fastThumb.getHeight(), fastThumbData),
                    fullFilePath);
        } else {
            sendDocument(peer, fileName, mimeType, fullFilePath);
        }
    }

    public void sendPhoto(Peer peer, String fullFilePath) {
        sendPhoto(peer, fullFilePath, new File(fullFilePath).getName());
    }

    public void sendPhoto(Peer peer, String fullFilePath, String fileName) {
        try {
            Bitmap bmp = ImageHelper.loadOptimizedHQ(fullFilePath);
            if (bmp == null) {
                return;
            }
            Bitmap fastThumb = ImageHelper.scaleFit(bmp, 90, 90);

            String resultFileName = getExternalTempFile("image", "jpg");
            if (resultFileName == null) {
                return;
            }
            ImageHelper.save(bmp, resultFileName);

            byte[] fastThumbData = ImageHelper.save(fastThumb);

            sendPhoto(peer, fileName, bmp.getWidth(), bmp.getHeight(), new FastThumb(fastThumb.getWidth(), fastThumb.getHeight(),
                    fastThumbData), resultFileName);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void sendVideo(Peer peer, String fullFilePath) {
        sendVideo(peer, fullFilePath, new File(fullFilePath).getName());
    }

    public void sendVideo(Peer peer, String fullFilePath, String fileName) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(fullFilePath);
            int duration = (int) (Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000L);
            Bitmap img = retriever.getFrameAtTime(0);
            int width = img.getWidth();
            int height = img.getHeight();
            Bitmap smallThumb = ImageHelper.scaleFit(img, 90, 90);
            byte[] smallThumbData = ImageHelper.save(smallThumb);

            FastThumb thumb = new FastThumb(smallThumb.getWidth(), smallThumb.getHeight(), smallThumbData);

            sendVideo(peer, fileName, width, height, duration, thumb, fullFilePath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getExternalTempFile(String prefix, String postfix) {
        File externalFile = context.getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        File outputFile = new File(dest, prefix + "_" + random.nextLong() + "." + postfix);
        return outputFile.getAbsolutePath();
    }

    private String getInternalTempFile(String prefix, String postfix) {
        File externalFile = context.getFilesDir();
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        File outputFile = new File(dest, prefix + "_" + random.nextLong() + "." + postfix);
        return outputFile.getAbsolutePath();
    }
}