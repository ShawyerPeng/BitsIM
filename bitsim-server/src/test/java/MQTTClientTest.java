import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import util.StringTool;

/**
 * 客户端启动类
 */
public class MQTTClientTest {
    private MqttClient client;
    private String host = "tcp://localhost:1883";
    //private String host = "tcp://iot.eclipse.org:1883";
    private String username = "test";
    private String password = "test";
    private MqttTopic topic;
    private MqttMessage message;
    private String myTopic = "test/topic";
    private String clientMac = StringTool.generalMacString();

    public MQTTClientTest() {
        try {
            client = new MqttClient(host, clientMac, new MemoryPersistence());
            connect();
            client.subscribe(myTopic, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(5 * 60);
        //String[] uris = {"tcp://10.100.124.206:1883","tcp://10.100.124.207:1883"};
        //options.setServerURIs(uris);  // 起到负载均衡和高可用的作用
        try {
            client.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection Lost-----------");
                }
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery Complete---------" + token.isComplete());
                }
                public void messageArrived(String topic, MqttMessage arg1) throws Exception {
                    System.out.println("Message Arrived----------");
                    System.out.println(topic + ":" + arg1.toString());
                }
            });

            topic = client.getTopic(myTopic);

            message = new MqttMessage();
            message.setQos(1);
            message.setRetained(true);
            System.out.println(message.isRetained() + "------ratained״̬");
            message.setPayload("我是消息内容！！！".getBytes());

            client.connect(options);

            client.publish(myTopic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MQTTClientTest s = new MQTTClientTest();
    }
}