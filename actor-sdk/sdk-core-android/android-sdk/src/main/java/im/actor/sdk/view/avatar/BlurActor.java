package im.actor.sdk.view.avatar;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

import im.actor.runtime.actors.Actor;
import im.actor.sdk.util.images.BitmapUtil;
import im.actor.sdk.util.images.common.ImageLoadException;
import im.actor.sdk.util.images.ops.ImageLoading;

public class BlurActor extends Actor {

    private void onNeedBlur(String path, int blurRadius, BluredListener bl){
        File blured = new File(path+"_blured");
        if(!blured.exists() || blured.length()==0){
            try {
                Bitmap blurdBitmap = BitmapUtil.fastBlur(ImageLoading.loadBitmap(path), blurRadius);
                BitmapUtil.save(blurdBitmap, blured.getPath());
                bl.onBlured(blured);
            } catch (ImageLoadException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            bl.onBlured(blured);
        }
    }

    @Override
    public void onReceive(Object message) {
        if(message instanceof RequestBlur){
            onNeedBlur(((RequestBlur) message).getPath(), ((RequestBlur) message).getRadius(), ((RequestBlur) message).getBl());
        }
    }

    public static class RequestBlur{
        String path;
        int radius;
        BluredListener bl;

        public String getPath() {
            return path;
        }

        public int getRadius() {
            return radius;
        }

        public BluredListener getBl() {
            return bl;
        }

        public RequestBlur(String path, int radius, BluredListener bl) {
            this.path = path;
            this.radius = radius;
            this.bl = bl;
        }
    }

    interface BluredListener{
        void onBlured(File f);
    }
}
