package im.actor.tc;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class TCCheckActor extends Actor {
    private HashSet<Integer> ids;
    private String url;
    private ActorRef tc;

    public TCCheckActor(String url, ActorRef tc) {
        this.ids = new HashSet<>();
        this.url = url;
        this.tc = tc;
    }

    @Override
    public void preStart() {
        self().send(new Check());
    }

    private void check() {
        String end = "/httpAuth/app/rest/builds/id:";
        String getUrl;
        Log.d("TC ids", ids.toString());
        if (ids != null && url != null) {
            for (int i : ids) {
                getUrl = url + end + i;
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(getUrl).openConnection();
                    connection.setRequestProperty("Accept", "application/json");
//                        connection.setDoInput(true);
                    String username = "kor_ka";
                    String password = "Xw9YJk6VHRXGKB";

                    String encoded = Base64.encodeToString(new String(username + ":" + password).getBytes("UTF-8"), Base64.DEFAULT);

                    connection.setRequestProperty("Authorization", "Basic " + encoded);
                    connection.connect();
                    InputStream in = new BufferedInputStream(connection.getInputStream());


                    JSONObject data = new JSONObject(readStream(in));
                    data.put("url", url);
                    tc.send(new TCActor.IdChecked(i, data));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
        schedule(new Check(), 1000);
    }

    public void onAdd(int id) {
        ids.add(id);
    }

    public void onRemove(int id) {
        ids.remove(id);
    }

    private String readStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Check) {
            check();
        } else if (message instanceof Add) {
            onAdd(((Add) message).getId());
        } else if (message instanceof Remove) {
            onRemove(((Remove) message).getId());
        }
    }

    public static class Check {
    }

    public static class Add {
        int id;

        public Add(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class Remove {
        int id;

        public Remove(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

    }

    public interface TCCheckCallback {
        void onIdchecked(int id, JSONObject json);
    }
}
