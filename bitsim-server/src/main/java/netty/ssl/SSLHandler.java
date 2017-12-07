package netty.ssl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SSLHandler {
    private static String MQTT_BROKER = "ssl://localhost:xxxx";
    private static String MQTT_USERNAME = "admin";
    private static String MQTT_PASSWORD = "password";

    private static String caFilePath = "G:\\openssl_ca\\ca.crt";
    private static String clientCrtFilePath = "G:\\openssl_ca\\client.crt";
    private static String clientKeyFilePath = "G:\\openssl_ca\\client.key";

    public void publishMessageByMqtt(String topic) throws Exception {
        String content = "Message from my device! I'm test!";
        int qos = 2;
        String clientId = "cspublish_mqtt";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(MQTT_BROKER, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(MQTT_USERNAME);
            connOpts.setPassword(MQTT_PASSWORD.toCharArray());
            connOpts.setConnectionTimeout(1000);
            connOpts.setKeepAliveInterval(2000);
            connOpts.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory(caFilePath, clientCrtFilePath, clientKeyFilePath, "cs123456"));
            try {
                sampleClient.connect(connOpts);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic, message);
                System.out.println("Message published");
            } catch (Throwable e) {
                System.out.println("Error " + e.getMessage());
            }
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("run...");
        SSLHandler handler = new SSLHandler();
        handler.publishMessageByMqtt("test...");
    }
}
// [使用 SSL/TLS 加密 Netty 程序](https://waylau.gitbooks.io/essential-netty-in-action/CORE%20FUNCTIONS/Securing%20Netty%20applications%20with%20SSLTLS.html)