package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceGroupUserLeave extends ServiceContent {


    public static ServiceGroupUserLeave fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceGroupUserLeave(), data);
    }

    public ServiceGroupUserLeave() {
        super("User leave");
    }
}
