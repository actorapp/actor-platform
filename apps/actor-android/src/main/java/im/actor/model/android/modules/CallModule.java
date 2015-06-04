/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.android.modules;

import android.content.Intent;

import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.fragment.call.CallActivity;
import im.actor.messenger.app.util.RandomUtil;
import im.actor.model.android.modules.call.CallActor;
import im.actor.model.android.modules.call.CallState;
import im.actor.model.android.modules.call.CurrentCall;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.mvvm.ValueModel;

import static im.actor.model.droidkit.actors.ActorSystem.system;

/**
 * Created by ex3ndr on 18.05.15.
 */
public class CallModule extends BaseModule {

    private ActorRef actorRef;
    private ValueModel<CurrentCall> currentCall = new ValueModel<CurrentCall>("app.call", null);

    public CallModule(final Modules modules) {
        super(modules);

        actorRef = system().actorOf(Props.create(CallActor.class, new ActorCreator<CallActor>() {
            @Override
            public CallActor create() {
                return new CallActor(modules, CallModule.this);
            }
        }), "actor/call");
    }

    public void onLoggedIn() {
        actorRef.send(new CallActor.StartEngine());
    }

    public synchronized void onIncomingCall(long rid, int uid) {
        currentCall.change(new CurrentCall(rid, uid, CallState.RINGING_INCOMING));
        MVVMEngine.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppContext.getContext().startActivity(
                        new Intent(AppContext.getContext(), CallActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    public synchronized void onCallEnded(long rid) {
        CurrentCall call = currentCall.get();
        if (call.getRid() == rid) {
            currentCall.change(new CurrentCall(rid, call.getCallUser(),
                    CallState.ENDED));
        }
    }

    public synchronized void onCallStarted(long rid) {
        CurrentCall call = currentCall.get();
        if (call.getRid() == rid) {
            currentCall.change(new CurrentCall(rid, call.getCallUser(),
                    CallState.IN_PROGRESS));
        }
    }

    public synchronized void onCallConnected(long rid) {
        CurrentCall call = currentCall.get();
        if (call.getRid() == rid) {
            currentCall.change(new CurrentCall(rid, call.getCallUser(),
                    CallState.RINGING));
        }
    }

    public synchronized long startCall(int uid) {
        CurrentCall call = currentCall.get();
        if (call != null) {
            if (call.getCallState() != CallState.ENDED) {
                return 0;
            }
        }

        long rid = RandomUtil.randomId();
        currentCall.change(new CurrentCall(rid, uid, CallState.CONNECTING));
        actorRef.send(new CallActor.PerformCall("uid_" + uid, rid));
        return rid;
    }


    public synchronized void answerCall(long rid) {
        CurrentCall call = currentCall.get();
        if (call.getRid() == rid) {
            currentCall.change(new CurrentCall(rid, call.getCallUser(), CallState.ANSWERING));
            actorRef.send(new CallActor.AnswerCall(rid));
        }
    }

    public synchronized void endCall(long rid) {
        CurrentCall call = currentCall.get();
        if (call.getRid() == rid) {
            currentCall.change(new CurrentCall(rid, call.getCallUser(), CallState.ENDED));
            actorRef.send(new CallActor.EndCall(rid));
        }
    }


    public ValueModel<CurrentCall> getCurrentCall() {
        return currentCall;
    }
}
