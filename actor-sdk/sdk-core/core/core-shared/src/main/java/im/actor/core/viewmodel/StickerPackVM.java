/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import im.actor.core.entity.Group;
import im.actor.core.entity.GroupMember;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.viewmodel.generics.AvatarValueModel;
import im.actor.core.viewmodel.generics.BooleanValueModel;
import im.actor.core.viewmodel.generics.StringValueModel;
import im.actor.runtime.annotations.MainThread;
import im.actor.runtime.mvvm.BaseValueModel;
import im.actor.runtime.mvvm.ModelChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.runtime.mvvm.ValueModelCreator;

/**
 * Group View Model
 */
public class StickerPackVM extends BaseValueModel<StickersPack> {

    public static ValueModelCreator<StickersPack, StickerPackVM> CREATOR() {
        return new ValueModelCreator<StickersPack, StickerPackVM>() {
            @Override
            public StickerPackVM create(StickersPack baseValue) {
                return new StickerPackVM(baseValue);
            }
        };
    }

    @Property("nonatomic, readonly")
    private int id;
    @NotNull
    @Property("nonatomic, readonly")
    private ValueModel<ArrayList<Sticker>> stickers;

    @Override
    protected void updateValues(@NotNull StickersPack rawObj) {
        boolean isChanged = stickers.change(new ArrayList<Sticker>(rawObj.getStickers()));

        if (isChanged) {
            notifyChange();
        }
    }


    @NotNull
    private ArrayList<ModelChangedListener<StickerPackVM>> listeners = new ArrayList<ModelChangedListener<StickerPackVM>>();

    /**
     * <p>INTERNAL API</p>
     * Create Group View Model
     *
     * @param rawObj initial value of Group
     */
    public StickerPackVM(@NotNull StickersPack rawObj) {
        super(rawObj);
        this.id = rawObj.getId();
        this.stickers = new ValueModel<ArrayList<Sticker>>("stickerpack." + id + ".stickers", new ArrayList<Sticker>(rawObj.getStickers()));

    }

    /**
     * Get Pack Id
     *
     * @return Pack Id
     */
    @ObjectiveCName("getId")
    public int getId() {
        return id;
    }

    /**
     * Get stickers count
     *
     * @return stickers count
     */
    @ObjectiveCName("getMembersCount")
    public int getStickersCount() {
        return stickers.get().size();
    }

    /**
     * Get stickers Value Model
     *
     * @return Value Model of HashSet of GroupMember
     */
    @NotNull
    @ObjectiveCName("getMembersModel")
    public ValueModel<ArrayList<Sticker>> getStickers() {
        return stickers;
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:")
    public void subscribe(@NotNull ModelChangedListener<StickerPackVM> listener) {
        im.actor.runtime.Runtime.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.onChanged(this);
    }

    /**
     * Subscribe for GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("subscribeWithListener:withNotify:")
    public void subscribe(@NotNull ModelChangedListener<StickerPackVM> listener, boolean notify) {
        im.actor.runtime.Runtime.checkMainThread();
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (notify) {
            listener.onChanged(this);
        }
    }

    /**
     * Unsubscribe from GroupVM updates
     *
     * @param listener Listener for updates
     */
    @MainThread
    @ObjectiveCName("unsubscribeWithListener:")
    public void unsubscribe(@NotNull ModelChangedListener<StickerPackVM> listener) {
        im.actor.runtime.Runtime.checkMainThread();
        listeners.remove(listener);
    }

    private void notifyChange() {
        im.actor.runtime.Runtime.postToMainThread(new Runnable() {
            @Override
            public void run() {
                for (ModelChangedListener<StickerPackVM> l : listeners.toArray(new ModelChangedListener[listeners.size()])) {
                    l.onChanged(StickerPackVM.this);
                }
            }
        });
    }

}