package im.actor.core.entity.content;



import im.actor.core.api.ApiJsonMessage;
import im.actor.core.api.ApiMessage;
import im.actor.core.entity.content.internal.AbsContentContainer;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public abstract class JsonContent extends AbsContent {

    private JSONObject jsonObject;
    private String rawJson;

    public JsonContent() {
        super();
    }

    public static JsonContent convert(AbsContentContainer container, JsonContent content) {
        if (container instanceof ContentRemoteContainer) {
            ApiMessage message = ((ContentRemoteContainer) container).getMessage();
            if (message instanceof ApiJsonMessage) {
                try {
                    JSONObject jsonObject = new JSONObject(((ApiJsonMessage) message).getRawJson());
                    if (jsonObject.getString("dataType").equals(content.getDataType())) {
                        content.setJsonObject(jsonObject);
                        content.setContentContainer(new ContentRemoteContainer(new ApiJsonMessage(((ApiJsonMessage) message).getRawJson())));
                        content.setRawJson(((ApiJsonMessage) message).getRawJson());
                        return content;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static JsonContent create(JsonContent content, String simpleStringData) {
        JSONObject data = new JSONObject();
        try {
            data.put("data", simpleStringData);
            return create(content, data);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonContent create(JsonContent content, JSONObject data) {
        JSONObject json = new JSONObject();
        try {
            json.put("dataType", content.getDataType());
            json.put("data", data);
            ContentRemoteContainer container = new ContentRemoteContainer(new ApiJsonMessage(json.toString()));
            content.setJsonObject(json);
            content.setRawJson(json.toString());
            content.setContentContainer(container);
            return content;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    public String getRawJson() {
        return rawJson;
    }

    public JSONObject getJson() {
        return jsonObject;
    }

    public abstract String getDataType();

    public abstract String getContentDescriptionEn();

    public String getContentDescriptionRu() {
        return null;
    }

    public String getContentDescriptionPt() {
        return null;
    }

    public String getContentDescriptionAr() {
        return null;
    }

    public String getContentDescriptionCn() {
        return null;
    }

    public String getContentDescriptionEs() {
        return null;
    }

    public final String getContentDescription() {
        String locale = im.actor.runtime.Runtime.getLocaleRuntime().getCurrentLocale();
        if (locale.equals("Ru")) {
            return fallback(getContentDescriptionRu());
        } else if (locale.equals("Pt")) {
            return fallback(getContentDescriptionPt());
        } else if (locale.equals("Ar")) {
            return fallback(getContentDescriptionAr());
        } else if (locale.equals("Cn")) {
            return fallback(getContentDescriptionCn());
        } else if (locale.equals("Es")) {
            return fallback(getContentDescriptionEs());
        } else {
            return fallback(getContentDescriptionEn());
        }


    }

    private String fallback(String s) {
        if (s != null && !s.isEmpty()) {
            return s;
        } else {
            if (getContentDescriptionEn() != null && !getContentDescriptionEn().isEmpty()) {
                return getContentDescriptionEn();

            } else {
                return getDataType();
            }
        }
    }

    protected void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    protected void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public String getSimpleStringData() {
        try {
            return getJson().getJSONObject("data").getString("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
