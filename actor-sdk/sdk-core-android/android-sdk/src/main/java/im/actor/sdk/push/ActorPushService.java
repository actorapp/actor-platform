package im.actor.sdk.push;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import im.actor.core.util.ExponentialBackoff;

/**
 * Actor Push service based on MQTT
 */
public class ActorPushService extends Service implements MqttCallback {

    private static final String TAG = "PushService";

    private final Executor connectionExecutor = Executors.newSingleThreadExecutor();

    private final MemoryPersistence persistence = new MemoryPersistence();
    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    private String packageName;
    private String receiverName;

    private SharedPreferences preferences;

    private String[] mqttUrls;
    private String mqttClientId;
    private String mqttTopic;
    private String mqttUsername;
    private String mqttPassword;

    private MqttClient mqttClient;

    private int attemptIndex = 0;
    private boolean isConnecting = false;

    private Random random = new Random();

    private ExponentialBackoff exponentialBackoff = new ExponentialBackoff(1000, 5 * 60000, 15);

    public ActorPushService() {

        // Configuration on MQTT connection options
        connectOptions.setCleanSession(false);
        connectOptions.setConnectionTimeout(5 /* 5 seconds */);
        connectOptions.setKeepAliveInterval(15 * 60 /* 15 minutes */);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        // Loading Application Package
        packageName = getApplicationContext().getPackageName();

        //
        // Searching for Receiver
        //
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent("im.actor.push.intent.RECEIVE");
        List<ResolveInfo> resolveInfoList = packageManager.queryBroadcastReceivers(intent, 0);
        for (ResolveInfo r : resolveInfoList) {
            if (packageName.equals(r.activityInfo.packageName)) {
                receiverName = r.activityInfo.name;
                break;
            }
        }

        //
        // Loading current MQTT state
        //
        preferences = getSharedPreferences("actor_push_service", MODE_PRIVATE);

        //
        // Loading unique clientId
        //
        mqttClientId = preferences.getString("mqtt_clientId", null);
        if (mqttClientId == null) {
            mqttClientId = UUID.randomUUID().toString();
            preferences.edit()
                    .putString("mqtt_clientId", mqttClientId)
                    .commit();
        }

        //
        // Loading registration info
        //
        String urls = preferences.getString("mqtt_hosts", null);
        if (urls != null) {
            mqttUrls = urls.split(",");
        }
        mqttTopic = preferences.getString("mqtt_topic", null);
        mqttUsername = preferences.getString("mqtt_username", null);
        mqttPassword = preferences.getString("mqtt_password", null);

        //
        // Starting service
        //
        if (mqttUrls == null || mqttTopic == null || mqttUsername == null || mqttPassword == null) {
            mqttUrls = null;
            mqttTopic = null;
            mqttUsername = null;
            mqttPassword = null;
            Log.d(TAG, "Not started");
        } else {
            tryConnect();
        }
    }

    //
    // Connection creation
    //

    private synchronized void connectToBroker(String[] hosts, String topic, String username, String password) {
        Log.d(TAG, "connectToBroker:" + hosts + ", topic:" + topic);

        // Cancelling old connection
        cancelConnection();

        // Saving credentials
        mqttUrls = hosts;
        mqttTopic = topic;
        mqttUsername = username;
        mqttPassword = password;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < hosts.length; i++) {
            if (i != 0) {
                b.append(",");
            }
            b.append(hosts[i]);
        }
        preferences.edit()
                .putString("mqtt_url", b.toString())
                .putString("mqtt_topic", mqttTopic)
                .putString("mqtt_username", mqttUsername)
                .putString("mqtt_password", mqttPassword)
                .commit();

        // Starting connection
        tryConnect();
    }

    private synchronized void tryConnect() {
        Log.d(TAG, "tryConnect");
        // If connected
        if (mqttClient != null && mqttClient.isConnected()) {
            Log.d(TAG, "Already connected");
            return;
        }

        // Checking state
        if (isConnecting) {
            Log.d(TAG, "Already connecting");
            return;
        }
        isConnecting = true;

        Log.d(TAG, "Starting connecting...");


        connectionExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // Clearing mqttClient
                if (mqttClient != null) {
                    try {
                        mqttClient.close();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                mqttClient = null;

                try {
                    Thread.sleep(exponentialBackoff.exponentialWait());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                // Setting credentials
                connectOptions.setUserName(mqttUsername);
                connectOptions.setPassword(mqttPassword.toCharArray());

                // Starting mqtt connection
                final int attempt = ++attemptIndex;
                Log.d(TAG, "Connecting...");
                MqttClient mqttClient;
                try {
                    mqttClient = new MqttClient(mqttUrls[random.nextInt(mqttUrls.length)], mqttClientId, persistence);
                    mqttClient.connect(connectOptions);
                    Log.d(TAG, "Connected");
                    mqttClient.setCallback(ActorPushService.this);
                    mqttClient.subscribe(mqttTopic, 1);
                    Log.d(TAG, "Complete");
                } catch (MqttException e) {
                    Log.d(TAG, "Exception");
                    e.printStackTrace();
                    onConnectionFailure();
                    return;
                }
                Log.d(TAG, "Success");
                onConnected(attempt, mqttClient);
            }
        });
    }

    private synchronized void cancelConnection() {
        Log.d(TAG, "cancelConnection");
        isConnecting = false;
        attemptIndex++;

        // Clearing mqttClient
        if (mqttClient != null) {
            try {
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        mqttClient = null;
    }

    private synchronized void onConnected(int attempt, MqttClient mqttClient) {
        if (this.attemptIndex == attempt) {
            this.isConnecting = false;
            this.mqttClient = mqttClient;
            exponentialBackoff.onSuccess();
        } else {
            // Incorrect attempt
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            try {
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }


    }

    private synchronized void onConnectionFailure() {
        Log.d(TAG, "Connect Failure");

        //
        // Trying to recreate connection
        //
        isConnecting = false;
        exponentialBackoff.onFailure();
        tryConnect();
    }

    //
    // MQTT callbacks
    //

    @Override
    public synchronized void connectionLost(Throwable cause) {
        Log.d(TAG, "Connection Lost");

        // Clearing connection
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            try {
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        this.mqttClient = null;

        exponentialBackoff.onFailure();

        //
        // Trying to recreate connection
        //
        tryConnect();
    }

    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload(), "utf-8");
        Log.d(TAG, "Received " + topic + " " + msg);

        if (packageName != null && receiverName != null) {
            sendBroadcast(new Intent("im.actor.push.intent.RECEIVE")
                    .setClassName(packageName, receiverName)
                    .putExtra("push_payload", msg));
        }
    }

    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        // Ignore
    }

    //
    // Internals
    //

    @Override
    public IBinder onBind(Intent intent) {
        // Do not implement binding as this will probably block from service restarting
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("mqtt_urls")
                && intent.hasExtra("mqtt_topic")
                && intent.hasExtra("mqtt_username")
                && intent.hasExtra("mqtt_password")) {
            String[] url = intent.getStringArrayExtra("mqtt_urls");
            String topic = intent.getStringExtra("mqtt_topic");
            String username = intent.getStringExtra("mqtt_username");
            String password = intent.getStringExtra("mqtt_password");
            connectToBroker(url, topic, username, password);
        }

        // Making Service restart after killing
        // TODO: May not work correctly on 4.4+ on some devices - need to implement workaround
        return START_STICKY;
    }
}