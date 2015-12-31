package im.actor.tc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class TCCheckActor extends Actor {
    private ArrayList<Integer> ids;
    private String url;
    private TCCheckCallback callback;

    public TCCheckActor(ArrayList<Integer> ids, String url, TCCheckCallback callback) {
        this.ids = ids;
        this.url = url;
        this.callback = callback;
    }

    @Override
    public void preStart() {
        self().send(new Start());
    }

    private void onStart() {
        String end = "/httpAuth/app/rest/builds/id:";
        String getUrl;
        while (true) {
            if (ids != null && url != null) {
                for (int i : ids) {
                    getUrl = url + end + i;
                    try {
                        HttpURLConnection connection = (HttpURLConnection) new URL(getUrl).openConnection();
                        connection.setRequestMethod("GET");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.connect();
                        InputStream in = new BufferedInputStream(connection.getInputStream());


                        JSONObject json = new JSONObject(readStream(in));
                        json.put("url", url);
                        callback.onIdchecked(i, json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private String readStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Start) {
            onStart();
        }
    }

    public static class Start {

    }

    public interface TCCheckCallback {
        void onIdchecked(int id, JSONObject json);
    }
}
