package im.actor.messenger.app.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import im.actor.messenger.core.encryption.CryptoUtils;
import im.actor.messenger.util.Screen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static im.actor.messenger.util.io.StreamingUtils.writeLong;

/**
 * Created by ex3ndr on 09.10.14.
 */
public class SecurityKey {
    public static void showKey(List<Long> keys, Context context) {
        ArrayList<Long> keysC = new ArrayList<Long>(keys);
        Collections.sort(keysC);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Long l : keys) {
            try {
                writeLong(l, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        showKey(outputStream.toByteArray(), context);
    }

    public static void showKey(byte[] data, Context context) {
        byte[] sha256 = CryptoUtils.SHA256(data);
        Bitmap bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
        int[] colors = new int[]{0xff01579b, 0xff039be5, 0xff4fc3f7, 0xffe1f5fe};
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int index = i * 8 + j;
                byte v = sha256[index / 4];
                int v2 = (v >> ((index % 4) * 2)) & 0x3;
                bitmap.setPixel(i, j, colors[v2]);
            }
        }
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Screen.dp(256), Screen.dp(256), false);

        ImageView imageView = new ImageView(context);
        imageView.setMinimumWidth(Screen.dp(256));
        imageView.setMinimumHeight(Screen.dp(256));
        imageView.setImageBitmap(scaled);

        new AlertDialog.Builder(context)
                .setView(imageView)
                .setTitle("Encryption key")
                .show()
                .setCanceledOnTouchOutside(true);
    }
}
