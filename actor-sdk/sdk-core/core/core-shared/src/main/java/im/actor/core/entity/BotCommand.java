/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import com.google.j2objc.annotations.Property;


public class BotCommand {

    @Property("readonly, nonatomic")
    private final String command;
    @Property("readonly, nonatomic")
    private final String description;
    @Property("readonly, nonatomic")
    private final String locKey;

    public BotCommand(String command, String description, String loc) {
        this.command = command;
        this.description = description;
        this.locKey = loc;
    }

    public String getSlashCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public String getLocKey() {
        return locKey;
    }
}
