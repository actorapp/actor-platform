package im.actor.model.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;

public class ServiceExChangedAvatar extends BserObject {

    private Avatar avatar;

    public ServiceExChangedAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public ServiceExChangedAvatar() {

    }

    public Avatar getAvatar() {
        return this.avatar;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.avatar = values.optObj(1, new Avatar());
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.avatar != null) {
            writer.writeObject(1, this.avatar);
        }
    }

    @Override
    public String toString() {
        String res = "struct ServiceExChangedAvatar{";
        res += "avatar=" + (this.avatar != null ? "set":"empty");
        res += "}";
        return res;
    }

}
