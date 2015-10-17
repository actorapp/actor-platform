/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;

import java.io.IOException;

import im.actor.runtime.bser.BserCreator;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;
import im.actor.runtime.storage.ListEngineItem;

public class SearchEntity extends BserObject implements ListEngineItem {

    public static final BserCreator<SearchEntity> CREATOR = new BserCreator<SearchEntity>() {
        @Override
        public SearchEntity createInstance() {
            return new SearchEntity();
        }
    };

    public static final String ENTITY_NAME = "Search";

    @SuppressWarnings("NullableProblems")
    @Property("readonly, nonatomic")
    private Peer peer;
    @Property("readonly, nonatomic")
    private long order;
    @Property("readonly, nonatomic")
    private Avatar avatar;
    @Property("readonly, nonatomic")
    @SuppressWarnings("NullableProblems")
    private String title;

    public SearchEntity(Peer peer, long order, Avatar avatar, String title) {
        this.peer = peer;
        this.order = order;
        this.avatar = avatar;
        this.title = title;
    }

    private SearchEntity() {

    }

    public Peer getPeer() {
        return peer;
    }

    public Avatar getAvatar() {
        return avatar;
    }

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
