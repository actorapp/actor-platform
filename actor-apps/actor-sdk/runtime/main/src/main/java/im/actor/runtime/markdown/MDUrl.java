package im.actor.runtime.markdown;

public class MDUrl extends MDText {

    private String urlTitle;
    private String url;

    public MDUrl(String urlTitle, String url) {
        this.urlTitle = urlTitle;
        this.url = url;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toMarkdown() {
        return "[" + urlTitle + "](" + url + ")";
    }
}
