package im.actor.messenger.storage.scheme;

import com.droidkit.bser.BserObject;
import com.droidkit.bser.BserValues;
import com.droidkit.bser.BserWriter;

import im.actor.messenger.storage.scheme.avatar.Avatar;

import java.io.IOException;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class GlobalSearch extends BserObject {

    private int contType;
    private int contId;
    private String title;
    private Avatar avatar;

    public GlobalSearch(int contType, int contId, String title, Avatar avatar) {
        this.contType = contType;
        this.contId = contId;
        this.title = title;
        this.avatar = avatar;
    }

    public GlobalSearch() {

    }

    public int getContType() {
        return contType;
    }

    public int getContId() {
        return contId;
    }

    public String getTitle() {
        return title;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        contType = values.getInt(1);
        contId = values.getInt(2);
        title = values.getString(3);
        avatar = values.optObj(4, Avatar.class);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, contType);
        writer.writeInt(2, contId);
        writer.writeString(3, title);
        if (avatar != null) {
            writer.writeObject(4, avatar);
        }
    }
}
