/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;

public class ServiceUserRegistered extends ServiceContent {

    public static ServiceUserRegistered fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ServiceUserRegistered(), data);
    }

    public ServiceUserRegistered() {
        super("User registered");
    }
}
