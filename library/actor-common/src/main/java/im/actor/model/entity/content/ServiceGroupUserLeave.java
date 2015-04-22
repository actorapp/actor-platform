package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupUserLeave extends ServiceContent {


    public static ServiceGroupUserLeave fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupUserLeave(), data);
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.SERVICE_LEAVE;
    }

    public ServiceGroupUserLeave() {
        super("User leave");
    }
}
