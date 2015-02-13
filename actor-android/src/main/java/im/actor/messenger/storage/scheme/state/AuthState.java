package im.actor.messenger.storage.scheme.state;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class AuthState extends BserObject {
    private boolean isLoggedIn;
    private int uid;

    public AuthState(boolean isLoggedIn, int uid) {
        this.isLoggedIn = isLoggedIn;
        this.uid = uid;
    }

    public AuthState() {

    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        isLoggedIn = values.getBool(1);
        uid = values.getInt(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(1, isLoggedIn);
        writer.writeInt(2, uid);
    }
}
