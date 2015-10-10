/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

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
