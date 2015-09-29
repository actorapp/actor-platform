package im.actor.core.analytics;

import im.actor.core.api.ApiRawValue;

public class Page {

    private String contentType;
    private String contentTypeDisplay;
    private String contentId;

    private ApiRawValue params;

    public Page(String contentType, String contentTypeDisplay, String contentId, ApiRawValue params) {
        this.contentType = contentType;
        this.contentTypeDisplay = contentTypeDisplay;
        this.contentId = contentId;
        this.params = params;
    }

    public Page(String contentType, String contentTypeDisplay, String contentId) {
        this.contentType = contentType;
        this.contentTypeDisplay = contentTypeDisplay;
        this.contentId = contentId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentTypeDisplay() {
        return contentTypeDisplay;
    }

    public String getContentId() {
        return contentId;
    }

    public ApiRawValue getParams() {
        return params;
    }
}