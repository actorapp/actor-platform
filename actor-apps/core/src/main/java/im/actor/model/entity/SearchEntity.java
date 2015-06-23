/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.model.droidkit.bser.BserCreator;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.engine.ListEngineItem;

public class SearchEntity extends BserObject implements ListEngineItem {

    public static final BserCreator<SearchEntity> CREATOR = new BserCreator<SearchEntity>() {
        @Override
        public SearchEntity createInstance() {
            return new SearchEntity();
        }
    };

    @SuppressWarnings("NullableProblems")
    @NotNull
    private Peer peer;
    private long order;
    @Nullable
    private Avatar avatar;
    @SuppressWarnings("NullableProblems")
    @NotNull
    private String title;

    public SearchEntity(@NotNull Peer peer, long order, @Nullable Avatar avatar, @NotNull String title) {
        this.peer = peer;
        this.order = order;
        this.avatar = avatar;
        this.title = title;
    }

    private SearchEntity() {

    }

    @NotNull
    public Peer getPeer() {
        return peer;
    }

    @Nullable
    public Avatar getAvatar() {
        return avatar;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    public long getOrder() {
        return order;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        peer = Peer.fromBytes(values.getBytes(1));
        order = values.getLong(2);
        if (values.optBytes(3) != null) {
            avatar = new Avatar(values.getBytes(3));
        }
        title = values.getString(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeObject(1, peer);
        writer.writeLong(2, order);
        if (avatar != null) {
            writer.writeObject(3, avatar);
        }
        writer.writeString(4, title);
    }

    @Override
    public long getEngineId() {
        return peer.getUnuqueId();
    }

    @Override
    public long getEngineSort() {
        return order;
    }

    @Override
    public String getEngineSearch() {
        return title;
    }
}
