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


    public static <T extends JsonContent> T create(Class<T> c, String simpleStringData) {
        JSONObject jsondata = new JSONObject();
        try {
            jsondata.put("data", simpleStringData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return create(c, jsondata);
    }


    public static <T extends JsonContent> T create(Class<T> c, JSONObject data) {
        try {
            T t = c.newInstance();
            JSONObject json = new JSONObject();
            json.put("dataType", t.getDataType());
            json.put("data", data);
            ContentRemoteContainer container = new ContentRemoteContainer(new ApiJsonMessage(json.toString()));
            t.setJsonObject(json);
            t.setRawJson(json.toString());
            t.setContentContainer(container);
            return t;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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

    public abstract String getContentDescriptionRu();

    public abstract String getContentDescriptionPt();

    public abstract String getContentDescriptionAr();

    public abstract String getContentDescriptionCn();

    public abstract String getContentDescriptionEs();

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
                return "";
            }
        }
    }

    public static <T extends JsonContent> AbsContent convert(AbsContentContainer container, Class<T> classToConvert) {
        if (container instanceof ContentRemoteContainer) {
            try {
                T type = classToConvert.newInstance();

                ApiMessage msg = ((ContentRemoteContainer) container).getMessage();
                if (msg instanceof ApiJsonMessage) {
                    try {
                        JSONObject object = new JSONObject(((ApiJsonMessage) msg).getRawJson());
                        if (object.get("dataType").equals(type.getDataType())) {
                            return type.create(classToConvert, object.getJSONObject("data"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
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
