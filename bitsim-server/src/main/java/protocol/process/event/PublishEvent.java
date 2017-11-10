package protocol.process.event;

import java.io.Serializable;
import java.util.Arrays;

import protocol.message.QoS;

/**
 * 发送消息的事件类，把协议的处理当做事件来进行就可以很好的进行封装
 */
public class PublishEvent implements Serializable {
    private String topic;
    private QoS qos;
    private byte[] message;
    private boolean retain;
    private String clientId;
    // 针对Qos1和Qos2
    private int packetId;

    public PublishEvent(String topic, QoS qos, byte[] message, boolean retain, String clientId, Integer packetId) {
        this.topic = topic;
        this.qos = qos;
        this.message = message;
        this.retain = retain;
        this.clientId = clientId;
        if (qos != QoS.AT_MOST_ONCE) {
            this.packetId = packetId;
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public QoS getQos() {
        return qos;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public String toString() {
        return "PublishEvent{" +
                "topic='" + topic + '\'' +
                ", qos=" + qos +
                ", message=" + Arrays.toString(message) +
                ", retain=" + retain +
                ", clientId='" + clientId + '\'' +
                ", packetId=" + packetId +
                '}';
    }
}