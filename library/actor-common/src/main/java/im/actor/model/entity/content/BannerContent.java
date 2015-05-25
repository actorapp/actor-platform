/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity.content;

import im.actor.model.droidkit.json.JSONException;
import im.actor.model.droidkit.json.JSONObject;

import im.actor.model.api.JsonMessage;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.content.internal.ContentRemoteContainer;

/**
 * Created by ex3ndr on 25.05.15.
 */
public class BannerContent extends AbsContent {

    private String adUrl;
    private FileReference reference;
    private int w;
    private int h;

    public BannerContent(ContentRemoteContainer contentContainer) throws JSONException {
        super(contentContainer);

        String json = ((JsonMessage) contentContainer.getMessage()).getRawJson();
        JSONObject data = new JSONObject(json).getJSONObject("data");
        JSONObject image = data.getJSONObject("image");
        adUrl = data.getString("advertUrl");

        w = image.getInt("width");
        h = image.getInt("height");

//        reference = new FileReference(new FileLocation(
//                data.getInt("fileId"),
//                data.getLong("fileHash")),
//                "banner.jpg", data.getInt("fileSize"));
//        w = data.getInt("width");
//        h = data.getInt("height");
    }

    public String getAdUrl() {
        return adUrl;
    }

    public FileReference getReference() {
        return reference;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
