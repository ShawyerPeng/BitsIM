package api;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * 发送数据到 MQTT 服务器
 */
public class PublishMessage {
    private static int qos = 2; // 只有一次
    // 服务器地址（协议+地址+端口号）
    //private static String broker = "tcp://localhost:1883";
    private static String broker = "tcp://iot.eclipse.org:1883";
    private static String username = "test";
    private static String password = "test";
    public static final String TOPIC = "topic1";

    private static MqttClient connect(String clientId, String username, String password) throws MqttException {
        // MemoryPersistence 设置 clientid 的保存形式，默认为以内存保存
        MemoryPersistence persistence = new MemoryPersistence();
        // MQTT连接配置选项
        MqttConnectOptions connOpts = new MqttConnectOptions();
        // 设置连接的用户名
        connOpts.setUserName(username);
        // 设置连接的密码
        connOpts.setPassword(password.toCharArray());
        // 设置是否清空 session, false 表示服务器会保留客户端的连接记录，这里设置为 true 表示每次连接到服务器都以新的身份连接
        connOpts.setCleanSession(true);
        // 设置超时时间，单位：秒
        connOpts.setConnectionTimeout(10);
        // 设置心跳包发送间隔，单位：秒
        connOpts.setKeepAliveInterval(20);
        //String[] uris = {"tcp://10.100.124.206:1883","tcp://10.100.124.207:1883"};
        //connOpts.setServerURIs(uris);  // 起到负载均衡和高可用的作用
        MqttClient mqttClient = new MqttClient(broker, clientId, persistence);
        // 设置MQTT监听并且接受消息
        mqttClient.setCallback(new PushCallback());
        MqttTopic topic = mqttClient.getTopic(TOPIC);
        // setWill 方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
        connOpts.setWill(topic, "close".getBytes(), 0, true);
        // 进行连接
        mqttClient.connect(connOpts);
        return mqttClient;
    }

    public static void publish(MqttTopic topic, MqttMessage message) throws MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
        System.out.println("message is published completely!" + token.isComplete());
    }

    public static void publish(String str, String clientId, String topic) throws MqttException {
        MqttClient mqttClient = connect(clientId, username, password);

        if (mqttClient != null) {
            pub(mqttClient, str, topic);
            System.out.println("pub-->" + str);
        }

        //if (mqttClient != null) {
        //    mqttClient.disconnect();
        //}
    }

    private static void pub(MqttClient mqttClient, String msg, String topic) throws MqttException {
        MqttMessage message = new MqttMessage();
        // 发布内容
        message.setPayload(msg.getBytes());
        // 设置发布级别/服务质量
        message.setQos(qos);
        // 设置是否在服务器中保存消息体
        message.setRetained(false);
        // 发布消息
        mqttClient.publish(topic, message);
    }

    public static void main(String[] args) throws MqttException {
        publish("message content", "client-id-666", "topic1");
    }
}