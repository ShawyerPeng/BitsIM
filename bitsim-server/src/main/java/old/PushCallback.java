package old;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * 发布消息的回调类
 * 必须实现 MqttCallback 的接口并实现对应的相关接口方法 CallBack 类将实现 MqttCallBack。
 * 每个客户机标识都需要一个回调实例。在此示例中，构造函数传递客户机标识以另存为实例数据。
 * 在回调中，将它用来标识已经启动了该回调的哪个实例。
 * 必须在回调类中实现三个方法
 * 由 MqttClient.connect 激活此回调。
 */
public class PushCallback implements MqttCallback {
    private Logger logger = Logger.getLogger(this.getClass());

    // 失败重连次数
    private int reconnect_trial = 10;
    private boolean queue_enable;
    private MqttClient currentClient;
    private BlockingQueue<String[]> MessageQueue;    // Producer

    private String timestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return now.format(format);
    }

    /**
     * 在连接断开时调用
     */
    @Override
    public void connectionLost(Throwable cause) {
        // 连接丢失后，一般在这里面进行重连
        System.out.println(">>>>>>>>>>>>>>> 连接断开，正在进行重连 <<<<<<<<<<<<<<<<");
        System.out.println("[" + timestamp() + "] MQTT Connection Lost: " + cause.getMessage());
        while (reconnect_trial != 0 && !currentClient.isConnected()) {
            try {
                System.out.println("[" + timestamp() + "] System is trying to reconnect... (" + reconnect_trial + ")");
                currentClient.reconnect();
                if (currentClient.isConnected()) {
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

    /**
     * 接收到已经发布的 QoS 1 或 QoS 2 消息的传递令牌时调用。
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // publish后会执行到这里
        logger.info(">>>>>>>>>>>>>>> delivery complete: topics: " + Arrays.toString(token.getTopics()) +
                ", clientId: " + token.getClient().getClientId() + ", messageId: " + token.getMessageId() + " <<<<<<<<<<<<<<<<");
    }

    /**
     * 接收已订阅主题的消息
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面
        logger.info("------------------------------------------------------------");
        logger.info(">>>>>>>>>>>>>>> 接收消息主题: " + topic + " <<<<<<<<<<<<<<<<");
        logger.info(">>>>>>>>>>>>>>> 接收消息 Qos: " + message.getQos() + " <<<<<<<<<<<<<<<<");
        logger.info(">>>>>>>>>>>>>>> 接收消息内容: " + new String(message.getPayload()) + " <<<<<<<<<<<<<<<<");
    }
}
