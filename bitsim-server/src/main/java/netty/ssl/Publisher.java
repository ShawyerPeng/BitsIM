package netty.ssl;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.SocketFactory;

public class Publisher {
    public static void main(String[] args) {
        int qos = 2;
        String broker = "localhost";
        int port = 1883;
        String clientId = "myPublisher";
        String pubisherTopic = "topic/foo";
        boolean cleanSession = true;
        boolean ssl = true;
        String password = "publisher";
        String userName = "publisher";
        String protocol = "ssl://";
        String payload = "23.0";
        SocketFactory socketFactory = null;

        String url = protocol + broker + ":" + port;

        try {
            if (ssl) {
                socketFactory = SSLSocketFactoryGenerator.getSocketFactory("publisher.properties");
            }
            Client client = new Client(url, clientId, cleanSession, userName, password, ssl, socketFactory);
            client.connect();
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            client.publish(pubisherTopic, message);
            client.disconnect();
        } catch (MqttException me) {
            System.out.println("There has been an error. ");
            System.out.println("Reason: " + me.getReasonCode());
            System.out.println("Message: " + me.getMessage());
            System.out.println("Localized Message: " + me.getLocalizedMessage());
            System.out.println("Cause: " + me.getCause());
            System.out.println("Exception Stack Trace: " + me);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}