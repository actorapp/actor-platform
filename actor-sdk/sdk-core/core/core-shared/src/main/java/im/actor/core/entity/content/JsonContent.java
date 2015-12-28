package im.actor.core.entity.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class JsonContent extends AbsContent {

    private JSONObject jsonObject;
    private String rawJson;

    @NotNull
    public static JsonContent create(@NotNull JSONObject json) {

        try {
            return new JsonContent(new ContentRemoteContainer(new ApiJsonMessage(json.toString())));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonContent(ContentRemoteContainer contentContainer) throws JSONException {
        super(contentContainer);
        rawJson = ((ApiJsonMessage) contentContainer.getMessage()).getRawJson();
        jsonObject = new JSONObject(rawJson);
    }


    public String getRawJson() {
        return rawJson;
    }

    public JSONObject getJson() {
        return jsonObject;
    }
}
