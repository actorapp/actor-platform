package im.actor.core.modules.messaging.router.entity;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ActiveDialogStorage extends BserObject {

    private ArrayList<ActiveDialogGroup> groups = new ArrayList<>();
    private boolean isLoaded = false;
    private boolean isHaveArchived = false;
    private boolean isShowInvite = false;

    public ActiveDialogStorage() {

    }

    public ActiveDialogStorage(byte[] data) throws IOException {
        super.load(data);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public boolean isHaveArchived() {
        return isHaveArchived;
    }

    public void setHaveArchived(boolean haveArchived) {
        isHaveArchived = haveArchived;
    }

    public boolean isShowInvite() {
        return isShowInvite;
    }

    public void setShowInvite(boolean showInvite) {
        isShowInvite = showInvite;
    }

    public ArrayList<ActiveDialogGroup> getGroups() {
        return groups;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        isLoaded = values.getBool(1);
        for (byte[] data : values.getRepeatedBytes(2)) {
            groups.add(new ActiveDialogGroup(data));
        }
        isHaveArchived = values.getBool(3);
        isShowInvite = values.getBool(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeBool(1, isLoaded);
        for (ActiveDialogGroup g : groups) {
            writer.writeObject(2, g);
        }
        writer.writeBool(3, isHaveArchived);
        writer.writeBool(4, isShowInvite);
    }
}
