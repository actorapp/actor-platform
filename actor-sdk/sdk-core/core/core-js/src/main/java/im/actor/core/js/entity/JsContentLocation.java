package im.actor.core.js.entity;

public class JsContentLocation extends JsContent {

    public native static JsContentLocation create(double longitude, double latitude, String street, String place)/*-{
        return {content: "location", longitude: longitude, latitude: latitude, street: street, place: place};
    }-*/;

    protected JsContentLocation() {

    }
}
