package im.actor.core.entity.content;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.content.internal.ContentRemoteContainer;

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
}
