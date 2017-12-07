package old;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

/**
 * 消息回调函数
 */
public class MessageCallback implements MqttCallback {
    // 失败重连次数
    private int reconnect_trial = 10;
    private boolean queue_enable;
    private MqttClient CurrentClient;
    private BlockingQueue<String[]> MessageQueue;    // Producer

    private static final Logger logger = LogManager.getLogger(MessageCallback.class);

    private String timestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return now.format(format);
    }

    public MessageCallback(MqttClient CurrentClient) {
        this.CurrentClient = CurrentClient;
        queue_enable = false;
    }

    public MessageCallback(MqttClient CurrentClient, BlockingQueue<String[]> MessageQueue) {
        this.CurrentClient = CurrentClient;
        this.MessageQueue = MessageQueue;
        queue_enable = true;
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("[" + timestamp() + "] MQTT Connection Lost: " + cause.getMessage());
        while (reconnect_trial != 0 && !CurrentClient.isConnected()) {
            try {
                System.out.println("[" + timestamp() + "] System is trying to reconnect... (" + reconnect_trial + ")");
                CurrentClient.reconnect();
                if (CurrentClient.isConnected()) {
                    System.out.println("[" + timestamp() + "] System Reconnected!");
                    reconnect_trial = 10;
                    break;
                }
            } catch (MqttException e) {
                reconnect_trial++;
                System.err.println("MQTT Reconnect: " + e.toString());
                logger.warn(e.toString());
            }
            reconnect_trial--;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void messageArrived(String Topic, MqttMessage Message) throws Exception {
        if (queue_enable) {
            String[] data = new String[2];
            String msg = null;
            try {
                msg = new String(Message.getPayload(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.toString());
            }
//			System.out.println(timestamp() + ": ["+Topic + "]" + msg);
            data[0] = Topic;
            data[1] = msg;
            MessageQueue.put(data);
        } else {
            String msg = null;
            try {
                msg = new String(Message.getPayload(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.toString());
            }
            System.out.println(timestamp() + ": [" + Topic + "] " + msg);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        reconnect_trial = 10;
        logger.info(">>>>>>>>>>>>>>> deliveryComplete " + token.getTopics() + " <<<<<<<<<<<<<<<<");
        logger.info(">>>>>>>>>>>>>>> delivery complete clientId: " + token.getClient().getClientId() + " messageId: " + token.getMessageId() + " <<<<<<<<<<<<<<");
    }
}
