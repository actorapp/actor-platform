package im.actor.core.analytics;

import im.actor.core.api.ApiRawValue;

public class Event {

    private String actionType;
    private String actionId;
    private String actionTitle;
    private ApiRawValue params;

    public Event(String actionType, String actionId, String actionTitle, ApiRawValue params) {
        this.actionType = actionType;
        this.actionId = actionId;
        this.actionTitle = actionTitle;
        this.params = params;
    }

    public String getActionType() {
        return actionType;
    }

    public String getActionId() {
        return actionId;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public ApiRawValue getParams() {
        return params;
    }
}
