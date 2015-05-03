package im.actor.messenger.app.keyboard.emoji.stickers;

import android.app.Application;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import im.actor.messenger.app.ActorBinder;
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
                        String packLogo = null;
                        String[] order = null;
                        ArrayList<String> stickers = new ArrayList<String>();


                        File sourceFileDir= new File(application.getFilesDir() + "/stickers/" + stickersPackId);
                        sourceFileDir.mkdirs();

                        for (String fileName : files) {
                            if(fileName.contains(".png")) {
                                stickersCount++;
                                try {
                                    File sourceFile = new File(sourceFileDir, fileName);
                                    if (sourceFile.exists()) {
                                        stickers.add(fileName.replace(".png", ""));
                                        continue;
                                    }
                                    InputStream fileIS = assetsManager.open("stickers/" + stickersPackId + "/" + fileName);
                                    IOUtils.copy(fileIS, sourceFile);
                                    fileIS.close();
                                    stickers.add(fileName.replace(".png", ""));
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
                                        packLogo = jObj.optString("logo");
                                        JSONArray jOrder = jObj.optJSONArray("order");
                                        order = new String[jOrder.length()];
                                        for (int i = 0; i < jOrder.length(); i++) {
                                            order[i] = jOrder.optString(i);
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
                            order = new String[stickers.size()];
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

    public String getStickerPath(String packId, String stickerId) {
        return application.getFilesDir().getAbsolutePath()+"/stickers/" + packId + "/" + stickerId + ".png";
    }



    HashMap<String, ValueModel> mvvmCollection = new HashMap<String, ValueModel>();

    public void bindSticker(final ImageView tabView, final String packId, final String stickerId) {
        if(isLoaded()){
            tabView.setImageURI(Uri.parse("file://" + getStickerPath(packId, stickerId)));
            return;
        }
        String bindingId = "stickers." + packId + "." + stickerId;

        ValueModel<Object> vm = mvvmCollection.get(bindingId);
        if(vm==null) {
            vm = new ValueModel<Object>(bindingId, null);
            mvvmCollection.put(bindingId, vm);
        }
        BINDER.bind(vm, new ValueChangedListener<Object>() {
            @Override
            public void onChanged(Object val, ValueModel<Object> valueModel) {
                tabView.setImageURI(Uri.parse("file://" + getStickerPath(packId, stickerId)));
                valueModel.unsubscribe(this);
            }
        });
    }


    private ArrayList<StickersPack> stickerPacks = new ArrayList<StickersPack>();

    public ArrayList<StickersPack> getPacks() {
        return stickerPacks;
    }
}
