package im.actor.sdk.controllers.conversation.attach;

public class ShareMenuField {

    private int icon;
    private int color;
    private int selector;
    private int id;
    private String title;

    public ShareMenuField(int id, int icon, int color, String title) {
        this.id = id;
        this.icon = icon;
        this.color = color;
        this.title = title;
    }

    public ShareMenuField(int id, String title, int selector) {
        this.id = id;
        this.title = title;
        this.selector = selector;
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public String getTitle() {
        return title;
    }

    public int getSelector() {
        return selector;
    }

    public int getId() {
        return id;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
