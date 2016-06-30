package im.actor.sdk.controllers.settings;

import java.util.ArrayList;

public class ActorSettingsCategories extends ArrayList<ActorSettingsCategory> {
    public ActorSettingsCategories addCategory(ActorSettingsCategory category) {
        super.add(category);
        return this;
    }
}
