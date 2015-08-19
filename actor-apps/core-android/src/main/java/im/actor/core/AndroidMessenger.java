package im.actor.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.view.Display;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.content.FastThumb;
import im.actor.core.network.NetworkState;
import im.actor.core.utils.AppStateActor;
import im.actor.core.utils.ImageHelper;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import me.leolin.shortcutbadger.ShortcutBadger;

import static im.actor.runtime.actors.ActorSystem.system;

public class AndroidMessenger extends im.actor.core.Messenger {

    private Context context;
    private final Random random = new Random();
    private ActorRef appStateActor;

    public AndroidMessenger(Context context, im.actor.core.Configuration configuration) {
        super(configuration);

        this.context = context;

        this.appStateActor = system().actorOf(Props.create(AppStateActor.class, new ActorCreator<AppStateActor>() {
            @Override
            public AppStateActor create() {
                return new AppStateActor(AndroidMessenger.this);
            }
        }), "actor/android/state");

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


        // Screen change processor
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    appStateActor.send(new AppStateActor.OnScreenOn());
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    appStateActor.send(new AppStateActor.OnScreenOff());
                }
            }
        }, screenFilter);
        if (isScreenOn()) {
            appStateActor.send(new AppStateActor.OnScreenOn());
        } else {
            appStateActor.send(new AppStateActor.OnScreenOff());
        }

        modules.getAppStateModule().getAppStateVM().getGlobalCounter().subscribe(new ValueChangedListener<Integer>() {
            @Override
            public void onChanged(Integer val, ValueModel<Integer> valueModel) {
                ShortcutBadger.with(AndroidContext.getContext()).count(val);
            }
        });
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


    public void onActivityOpen() {
        appStateActor.send(new AppStateActor.OnActivityOpened());
    }

    public void onActivityClosed() {
        appStateActor.send(new AppStateActor.OnActivityClosed());
    }

    public BindedDisplayList<Dialog> getDialogsDisplayList() {
        return (BindedDisplayList<Dialog>) modules.getDisplayListsModule().getDialogsSharedList();
    }

    public BindedDisplayList<Message> getMessageDisplayList(Peer peer) {
        return (BindedDisplayList<Message>) modules.getDisplayListsModule().getMessagesSharedList(peer);
    }

    public BindedDisplayList<SearchEntity> buildSearchDisplayList() {
        return (BindedDisplayList<SearchEntity>) modules.getDisplayListsModule().buildSearchList(false);
    }

    public BindedDisplayList<Contact> buildContactsDisplayList() {
        return (BindedDisplayList<Contact>) modules.getDisplayListsModule().buildContactList(false);
    }

    private boolean isScreenOn() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
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