package old;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import util.StringTool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 从 MQTT 服务器接受数据
 */
public class SubscribeMessage {
    private static final int qos = 2;
    private static final String broker = "tcp://localhost:1883";
    //private static String broker = "tcp://iot.eclipse.org:1883";
    private static final String username = "test";
    private static final String password = "test";
    private static final String TOPIC = "topic1";
    private static final String clientMac = StringTool.generalMacString();


    private ScheduledExecutorService scheduler;

    private static MqttClient connect(String clientId, String username, String password) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttConnectOptions connOpts = new MqttConnectOptions();
        // String[] uris = {"tcp://10.100.124.206:1883","tcp://10.100.124.206:1883"};
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        connOpts.setConnectionTimeout(10);
        connOpts.setKeepAliveInterval(20);
        // connOpts.setServerURIs(uris);
        connOpts.setWill("topic1", "close".getBytes(), 2, true);
        MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
        mqttClient.setCallback(new PushCallback());
        mqttClient.connect(connOpts);
        return mqttClient;
    }

    public static void subscribe(String clientId, String topic) throws MqttException {
        MqttClient mqttClient = connect(clientId, username, password);
        if (mqttClient != null) {
            sub(mqttClient, topic);
        }
    }

    private static void sub(MqttClient mqttClient, String topic) throws MqttException {
        int[] Qos = {qos};
        String[] topics = {topic};
        mqttClient.subscribe(topics, Qos);
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
        subscribe(clientMac, TOPIC);
    }
}