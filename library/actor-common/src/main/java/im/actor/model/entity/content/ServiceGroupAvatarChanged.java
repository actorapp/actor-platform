package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.entity.Avatar;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupAvatarChanged extends ServiceContent {

    public static ServiceGroupAvatarChanged fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupAvatarChanged(), data);
    }

    private Avatar newAvatar;

    public ServiceGroupAvatarChanged(Avatar newAvatar) {
        super("Group avatar changed");
        this.newAvatar = newAvatar;
    }

    private ServiceGroupAvatarChanged(){

    }

    public Avatar getNewAvatar() {
        return newAvatar;
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_AVATAR;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        super.parse(values);
        byte[] data = values.optBytes(10);
        if (data != null) {
            newAvatar = Avatar.fromBytes(data);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        super.serialize(writer);
        if (newAvatar != null) {
            writer.writeObject(10, newAvatar);
        }
    }
}
