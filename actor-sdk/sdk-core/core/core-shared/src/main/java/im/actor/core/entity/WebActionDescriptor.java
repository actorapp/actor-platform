package im.actor.core.entity;

public class WebActionDescriptor {

    private String uri;
    private String regexp;
    private String actionHash;

    public WebActionDescriptor(String uri, String regexp, String actionHash) {
        this.uri = uri;
        this.regexp = regexp;
        this.actionHash = actionHash;
    }

    public String getUri() {
        return uri;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getActionHash() {
        return actionHash;
    }
}
