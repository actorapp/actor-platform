package im.actor.sdk.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import im.actor.runtime.Log;
import im.actor.runtime.Runtime;

/**
 * Registration for Actor Push
 */
public final class ActorPushRegister {

    public static void registerForPush(final Context context, String endpoint, final Callback callback) {

        Runtime.dispatch(() -> {

            final SharedPreferences sharedPreferences = context.getSharedPreferences("actor_push_register", Context.MODE_PRIVATE);
            String registrationEndpoint = sharedPreferences.getString("registration_endpoint", null);
            String registrationData = sharedPreferences.getString("registration_data", null);

            OkHttpClient client = new OkHttpClient();

            if (registrationEndpoint != null && registrationData != null) {
                try {
                    JSONObject data = new JSONObject(registrationData);
                    startService(data, context);
                    callback.onRegistered(registrationEndpoint);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    sharedPreferences.edit().clear().commit();
                }
            }

            final Request request = new Request.Builder()
                    .url(endpoint)
                    .method("POST", RequestBody.create(MediaType.parse("application/json"), "{}"))
                    .build();

            client.newCall(request).enqueue(new com.squareup.okhttp.Callback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d("ACTOR_PUSH", "ACTOR_PUSH not registered: " + e.getMessage());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String res = response.body().string();
                        JSONObject js = new JSONObject(res).getJSONObject("data");
                        String endpoint1 = js.getString("endpoint");
                        sharedPreferences.edit()
                                .putString("registration_endpoint", endpoint1)
                                .putString("registration_data", js.toString())
                                .commit();
                        startService(js, context);
                        Log.d("ActorPushRegister", "Endpoint: " + endpoint1);
                        callback.onRegistered(endpoint1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // TODO: Handle?
                    }
                }
            });
        });
    }

    private static void startService(JSONObject config, Context context) {
        try {
            JSONObject object = config.getJSONObject("mqttServer");
            JSONArray hostsJ = object.getJSONArray("hosts");
            String[] hosts = new String[hostsJ.length()];
            for (int i = 0; i < hosts.length; i++) {
                hosts[i] = hostsJ.getString(i);
            }
            String username = object.getString("username");
            String password = object.getString("password");
            String topic = config.getString("topic");
            context.startService(new Intent(context, ActorPushService.class)
                    .putExtra("mqtt_urls", hosts)
                    .putExtra("mqtt_topic", topic)
                    .putExtra("mqtt_username", username)
                    .putExtra("mqtt_password", password));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        void onRegistered(String endpoint);
    }
}