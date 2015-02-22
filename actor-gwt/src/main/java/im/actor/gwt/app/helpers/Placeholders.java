package im.actor.gwt.app.helpers;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class Placeholders {
    public static String getPlaceholder(int id) {
        int index = Math.abs(id) % 7;
        return new String[]{
                "lblue",
                "blue",
                "purple",
                "red",
                "orange",
                "yellow",
                "green"
        }[index];
    }
}
