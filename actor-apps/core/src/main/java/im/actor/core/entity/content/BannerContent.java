/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content;

import im.actor.core.api.ApiFileLocation;
import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.FileReference;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

/**
 * Created by ex3ndr on 25.05.15.
 */
public class BannerContent extends AbsContent {

    private String adUrl;
    private FileReference reference;

    public BannerContent(ContentRemoteContainer contentContainer) throws JSONException {
        super(contentContainer);

        String json = ((ApiJsonMessage) contentContainer.getMessage()).getRawJson();
        JSONObject data = new JSONObject(json).getJSONObject("data");
        JSONObject image = data.getJSONObject("image");
        adUrl = data.getString("advertUrl");

        reference = new FileReference(new ApiFileLocation(
                image.getInt("fileId"),
                image.getLong("fileHash")),
                "banner.jpg", image.getInt("fileSize"));
    }

    public String getAdUrl() {
        return adUrl;
    }

    public FileReference getReference() {
        return reference;
    }
}
