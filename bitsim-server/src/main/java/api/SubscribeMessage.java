package api;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 从 MQTT 服务器接受数据
 */
public class SubscribeMessage {
    private static int qos = 2;
    //private static String broker = "tcp://localhost:1883";
    private static String broker = "tcp://iot.eclipse.org:1883";
    private static String username = "test";
    private static String password = "test";

    private ScheduledExecutorService scheduler;

    private static MqttClient connect(String clientId, String username, String password) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttConnectOptions connOpts = new MqttConnectOptions();
        // String[] uris = {"tcp://10.100.124.206:1883","tcp://10.100.124.206:1883"};
        connOpts.setCleanSession(false);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        connOpts.setConnectionTimeout(10);
        connOpts.setKeepAliveInterval(20);
        // connOpts.setServerURIs(uris);
        // connOpts.setWill(topic, "close".getBytes(), 2, true);
        MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
        mqttClient.setCallback(new PushCallback());
        mqttClient.connect(connOpts);
        return mqttClient;
    }

    private static void sub(MqttClient mqttClient, String topic) throws MqttException {
        int[] Qos = {qos};
        String[] topics = {topic};
        mqttClient.subscribe(topics, Qos);
    }

    public static void subscribe(String clientId, String topic) throws MqttException {
        MqttClient mqttClient = connect(clientId, username, password);
        if (mqttClient != null) {
            sub(mqttClient, topic);
        }
    }

    /**
     * 重新链接
     */
    public void reconnect(MqttClient mqttClient, String clientId, String username, String password) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (!mqttClient.isConnected()) {
                    try {
                        connect(clientId, username, password);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws MqttException {
        subscribe("client-id-999", "topic1");
    }
}