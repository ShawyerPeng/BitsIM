package api;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import util.StringTool;

/**
 * 发送数据到 MQTT 服务器
 */
public class PublishMessage {
    private final int qos = 2;
    private final String broker = "tcp://localhost:1883";
    //private static String broker = "tcp://iot.eclipse.org:1883";
    private final String username = "user02";
    private final String password = "123456";

    private MqttClient client;
    private final String clientMac = StringTool.generalMacString();
    private final String TOPIC = "10001:10002";

    public PublishMessage() {
        try {
            client = new MqttClient(broker, clientMac, new MemoryPersistence());
            connect();
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

            MqttMessage message = new MqttMessage();
            // 发布内容
            message.setPayload("我是消息".getBytes());
            // 设置发布级别/服务质量
            message.setQos(qos);
            // 设置是否在服务器中保存消息体
            message.setRetained(false);

            client.publish(TOPIC, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MqttException {
        PublishMessage main = new PublishMessage();
    }
}