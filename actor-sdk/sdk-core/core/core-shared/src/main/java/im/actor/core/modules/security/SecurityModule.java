/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.security;

import java.util.List;

import im.actor.core.api.ApiAuthSession;
import im.actor.core.api.rpc.RequestGetAuthSessions;
import im.actor.core.api.rpc.RequestTerminateAllSessions;
import im.actor.core.api.rpc.RequestTerminateSession;
import im.actor.core.api.rpc.ResponseGetAuthSessions;
import im.actor.core.api.rpc.ResponseVoid;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class SecurityModule extends AbsModule {

    // j2objc workaround
    private static final ResponseVoid DUMB = null;
    private static final Void DUMB2 = null;

    public SecurityModule(ModuleContext context) {
        super(context);
    }

    public Promise<List<ApiAuthSession>> loadSessions() {
        return api(new RequestGetAuthSessions())
                .map(ResponseGetAuthSessions::getUserAuths);
    }

    public Promise<Void> terminateAllSessions() {
        return api(new RequestTerminateAllSessions())
                .map(r -> null);
    }

    public Promise<Void> terminateSession(int id) {
        return api(new RequestTerminateSession(id))
                .map(r -> null);
    }
}
