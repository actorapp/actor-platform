package im.actor.core.entity.content;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class JsonContent extends AbsContent {

    private String rawJson;

    public JsonContent(ContentRemoteContainer contentRemoteContainer) {
        super(contentRemoteContainer);
        ApiJsonMessage json = ((ApiJsonMessage) contentRemoteContainer.getMessage());
        rawJson = json.getRawJson();
    }

    public String getRawJson() {
        return rawJson;
    }

    public String getContentDescription() {
        String res;
        try {
            JSONObject data = new JSONObject(getRawJson());
            res = data.getJSONObject("data").getString("text");
        } catch (JSONException e) {
            res = "";
        }
        return res;
    }
}
