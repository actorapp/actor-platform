package im.actor.tc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.Application;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.JsonContent;
import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

/**
 * Created by root on 12/31/15.
 */
public class TCActor extends Actor {

    private HashMap<Integer, HashSet<Long>> reidMap;
    private HashMap<Integer, Peer> peerMap;
    private HashSet<Integer> ids;
    private ActorRef check;
    private String url;

    public TCActor(String url) {
        this.reidMap = new HashMap<Integer, HashSet<Long>>();
        this.peerMap = new HashMap<Integer, Peer>();
        this.url = url;
    }

    @Override
    public void preStart() {

        ids = new HashSet<Integer>();
        check = ActorSystem.system().actorOf(Props.create(TCCheckActor.class, new ActorCreator<TCCheckActor>() {
            @Override
            public TCCheckActor create() {
                return new TCCheckActor(url, self());
            }
        }), "actor/tc_check");
        Log.d("TC", "create checker");
    }

    private void bind(int id, long rid, Peer peer) {
        ids.add(id);
        check.send(new TCCheckActor.Add(id));
        peerMap.put(id, peer);
        if (reidMap.get(id) != null) {
            reidMap.get(id).add(rid);
        } else {
            HashSet<Long> rids = new HashSet<Long>();
            rids.add(rid);
            reidMap.put(id, rids);
        }
    }

    public void onIdchecked(int id, JSONObject json) {
        try {
            for (long rid : reidMap.get(id)) {
                messenger().updateJsonMessageContentLocal(peerMap.get(id), rid, JsonContent.create(new Application.TCBotMesaage(), json));
                Log.d("TC", "update local");

            }
            if (json.getString("state").equals("finished")) {
                ids.remove(new Integer(id));
                check.send(new TCCheckActor.Remove(id));
                reidMap.remove(id);
                peerMap.remove(id);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Bind) {
            bind(((Bind) message).getId(), ((Bind) message).getRid(), ((Bind) message).getPeer());
        } else if (message instanceof IdChecked) {
            onIdchecked(((IdChecked) message).getId(), ((IdChecked) message).getJson());
        }
    }

    public static class Bind {
        int id;
        long rid;
        Peer peer;

        public Bind(int id, long rid, Peer peer) {
            this.id = id;
            this.rid = rid;
            this.peer = peer;
        }

        public int getId() {
            return id;
        }

        public long getRid() {
            return rid;
        }

        public Peer getPeer() {
            return peer;
        }
    }

    public static class IdChecked {
        int id;
        JSONObject json;

        public IdChecked(int id, JSONObject json) {
            this.id = id;
            this.json = json;
        }

        public int getId() {
            return id;
        }

        public JSONObject getJson() {
            return json;
        }
    }

}
