package im.actor.model.entity.content;

import im.actor.model.droidkit.bser.Bser;

import java.io.IOException;

/**
 * Created by ex3ndr on 14.02.15.
 */
public class ServiceUserRegistered extends ServiceContent {

    public static ServiceUserRegistered fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceUserRegistered(), data);
    }

    public ServiceUserRegistered() {
        super("User registered");
    }
}
