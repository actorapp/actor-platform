package im.actor.messenger.storage;

import android.content.Context;
import com.droidkit.bser.Bser;
import im.actor.messenger.storage.scheme.state.AuthState;
import im.actor.messenger.util.io.SafeFileWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 24.09.14.
 */
public class AuthStorage {
    private SafeFileWriter fileWriter;
    private AuthState auth;

    public AuthStorage(Context context) {
        fileWriter = new SafeFileWriter(context, "auth.bin");
        byte[] data = fileWriter.loadData();
        if (data != null && data.length > 0) {
            try {
                auth = Bser.parse(AuthState.class, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (auth == null) {
            auth = new AuthState(false, 0);
        }
    }

    public int getUid() {
        return auth.getUid();
    }

    public boolean isLoggedIn() {
        return auth.isLoggedIn();
    }

    public void logIn(int uid) {
        auth = new AuthState(true, uid);
        fileWriter.saveData(auth.toByteArray());
    }

    public void logOut() {
        auth = new AuthState(false, 0);
        fileWriter.saveData(auth.toByteArray());
    }
}
