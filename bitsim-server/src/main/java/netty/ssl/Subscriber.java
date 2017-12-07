package netty.ssl;

import org.eclipse.paho.client.mqttv3.MqttException;

import javax.net.SocketFactory;
import java.io.IOException;

public class Subscriber {
    public static void main(String[] args) {
        int qos = 2;
        String broker = "localhost";
        int port = 1883;
        String clientId = "mySubscriber";
        String subscriberTopic = "topic/foo";
        boolean cleanSession = true;
        boolean ssl = true;
        String password = null; // The password you set when you added the user to the passwords file.
        String userName = null;
        String protocol = "ssl://";
        SocketFactory socketFactory = null;

        String url = protocol + broker + ":" + port;

        try {
            if (ssl) {
                socketFactory = SSLSocketFactoryGenerator.getSocketFactory("subscriber.properties");
            }
            Client client = new Client(url, clientId, cleanSession, userName, password, ssl, socketFactory);
            client.connect();
            client.subscribe(subscriberTopic, qos);

            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

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