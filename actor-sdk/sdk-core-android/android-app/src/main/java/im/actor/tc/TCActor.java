package im.actor.tc;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.Application;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.JsonContent;
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

    private HashMap<Integer, ArrayList<Long>> reidMap;
    private HashMap<Integer, Peer> peerMap;
    private ArrayList<Integer> ids;
    private ActorRef check;
    private String url;

    public TCActor(String url) {
        this.reidMap = new HashMap<>();
        this.peerMap = new HashMap<>();
        this.url = url;
    }

    @Override
    public void preStart() {
        ids = new ArrayList<>();
        check = ActorSystem.system().actorOf(Props.create(TCCheckActor.class, new ActorCreator<TCCheckActor>() {
            @Override
            public TCCheckActor create() {
                return new TCCheckActor(ids, url, new TCCheckActor.TCCheckCallback() {
                    @Override
                    public void onIdchecked(int id, JSONObject json) {
                        try {
                            for (long rid : reidMap.get(id)) {
                                messenger().updateJsonMessageContentLocal(peerMap.get(id), rid, JsonContent.create(new Application.TCBotMesaage(), json));
                            }
                            if (json.getJSONObject("data").getString("state").equals("finished")) {
                                ids.remove(id);
                                reidMap.remove(id);
                                peerMap.remove(id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }), "actor/tc");
    }

    private void bind(int id, long rid, Peer peer) {
        ids.add(id);
        peerMap.put(id, peer);
        if (reidMap.get(id) != null) {
            reidMap.get(id).add(rid);
        } else {
            ArrayList<Long> rids = new ArrayList<>();
            rids.add(rid);
            reidMap.put(id, rids);
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Bind) {
            bind(((Bind) message).getId(), ((Bind) message).getRid(), ((Bind) message).getPeer());
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

}
