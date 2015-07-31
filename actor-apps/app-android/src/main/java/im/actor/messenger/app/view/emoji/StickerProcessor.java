/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.view.emoji;

import android.app.Application;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import im.actor.model.droidkit.json.JSONArray;
import im.actor.model.droidkit.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.messenger.app.fragment.ActorBinder;
import im.actor.messenger.app.view.emoji.stickers.Sticker;
import im.actor.messenger.app.view.emoji.stickers.StickerRecentController;
import im.actor.messenger.app.view.emoji.stickers.StickersPack;
import im.actor.messenger.app.util.io.IOUtils;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickerProcessor {


    protected static ActorBinder BINDER = new ActorBinder();

    private static final String TAG = "StickerProcessor";
    private final Application application;
    private boolean loading;
    private boolean loaded;
    private AssetManager assetsManager;
    private StickerRecentController recentController;

    public StickerProcessor(Application application) {

        this.application = application;
    }

    public void loadStickers() {
        if (loading || loaded) {
            return;
        }
        loading = true;

        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                long startTime = System.currentTimeMillis();
                Log.d(TAG, "Loading started");

                assetsManager = application.getAssets();

                try {
                    String[] stickersPacks = assetsManager.list("stickers");
                    int stickersCount = 0;
                    for (String stickersPackId : stickersPacks) {

                        String[] files = assetsManager.list("stickers/" + stickersPackId);

                        String packTitle = stickersPackId;
                        Sticker packLogo = null;
                        Sticker[] order = null;
                        ArrayList<Sticker> stickers = new ArrayList<Sticker>();


                        File sourceFileDir= new File(application.getFilesDir() + "/stickers/" + stickersPackId);
                        sourceFileDir.mkdirs();

                        for (String fileName : files) {
                            if(fileName.contains(".png")) {
                                stickersCount++;
                                try {
                                    File sourceFile = new File(sourceFileDir, fileName);
                                    if (!sourceFile.exists()) {
                                        InputStream fileIS = assetsManager.open("stickers/" + stickersPackId + "/" + fileName);
                                        IOUtils.copy(fileIS, sourceFile);
                                        fileIS.close();
                                    }
                                    stickers.add(new Sticker(fileName.replace(".png", ""), stickersPackId));
                                } catch (Exception ex) {
                                    Log.e(TAG, "Eror reading sticker", ex);
                                }
                            } else {
                                if (fileName.equals("meta.json")) {
                                    File sourceFile = new File(sourceFileDir, fileName);
                                    if(!sourceFile.exists()) {
                                        InputStream fileIS = assetsManager.open("stickers/" + stickersPackId + "/" + fileName);
                                        IOUtils.copy(fileIS, sourceFile);
                                        fileIS.close();
                                    }
                                    try {
                                        String total = IOUtils.toString(new FileInputStream(sourceFile));
                                        JSONObject jObj = new JSONObject(total);
                                        packTitle = jObj.optString("title");
                                        packLogo = new Sticker(jObj.optString("logo"), stickersPackId);
                                        JSONArray jOrder = jObj.optJSONArray("order");
                                        order = new Sticker[jOrder.length()];
                                        for (int i = 0; i < jOrder.length(); i++) {
                                            order[i] = new Sticker(jOrder.optString(i), stickersPackId);
                                        }
                                    } catch (Exception exp){
                                        Log.e(TAG, "Eror reading pack meta", exp);
                                    }
                                }
                            }
                        }
                        if(stickers.isEmpty()){
                            continue;
                        }
                        if(order==null){
                            order = new Sticker[stickers.size()];
                            order = stickers.toArray(order);
                        }
                        if(packLogo==null){
                            packLogo = stickers.get(0);
                        }
                        StickersPack pack = new StickersPack(stickersPackId, packTitle, packLogo, order);

                        stickerPacks.add(pack);

                    }
                    /**/

                    Log.d(TAG, stickersPacks.length + " packs with " + stickersCount + " stickers loaded in " + (System.currentTimeMillis() - startTime));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recentController = StickerRecentController.getInstance();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (ValueModel valueModel : mvvmCollection.values()) {
                            valueModel.change(null);
                        }
                        mvvmCollection.clear();
                    }
                });

                loading = false;
                loaded = false;
            }
        }.start();
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isLoading() {
        return loading;
    }

    public String getStickerPath(Sticker sticker) {
        return application.getFilesDir().getAbsolutePath()+"/stickers/" + sticker.getPackId() + "/" + sticker.getId() + ".png";
    }



    HashMap<String, ValueModel> mvvmCollection = new HashMap<String, ValueModel>();

    public void bindSticker(final ImageView tabView, final Sticker sticker) {
        String stickerId = sticker.getId();
        String packId = sticker.getPackId();
        if(isLoaded()){
            tabView.setImageURI(Uri.parse("file://" + getStickerPath(sticker)));
            return;
        }
        String bindingId = "stickers." + packId + "." + stickerId;

        ValueModel vm = mvvmCollection.get(bindingId);
        if(vm==null) {
            vm = new ValueModel<Object>(bindingId, null);
            mvvmCollection.put(bindingId, vm);
        }
        BINDER.bind(vm, new ValueChangedListener<Object>() {
            @Override
            public void onChanged(Object val, ValueModel<Object> valueModel) {
                tabView.setImageURI(Uri.parse("file://" + getStickerPath(sticker)));
                valueModel.unsubscribe(this);
            }
        });
    }


    private ArrayList<StickersPack> stickerPacks = new ArrayList<StickersPack>();

    public ArrayList<StickersPack> getPacks() {
        return stickerPacks;
    }

    public StickerRecentController getRecentController() {
        return recentController;
    }

    public void upRecentSticker(Sticker sticker) {
        recentController.push(sticker);
    }
}
