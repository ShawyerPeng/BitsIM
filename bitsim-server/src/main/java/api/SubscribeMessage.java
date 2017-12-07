package api;

import org.eclipse.paho.client.mqttv3.*;
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
    private static final String username = "user01";
    private static final String password = "123456";

    private static MqttClient client;
    private static final String clientMac = StringTool.generalMacString();
    private static final String TOPIC = "10001:10002";

    public SubscribeMessage() {
        try {
            client = new MqttClient(broker, clientMac, new MemoryPersistence());
            connect();
            client.subscribe(TOPIC, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(5 * 60);
        try {
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection Lost-----------");
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery Complete---------" + token.isComplete());
                }
                @Override
                public void messageArrived(String topic, MqttMessage arg1) throws Exception {
                    System.out.println("Message Arrived----------");
                    System.out.println(topic + ":" + arg1.toString());
                }
            });

            client.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {
        SubscribeMessage main = new SubscribeMessage();
    }
}