package protocol.message;

/**
 * MQTT协议Publish消息类型的可变头部
 */
public class PublishVariableHeader {
    // 主题名
    private String topic;
    // 报文标识符 Packet Identifier，代表 QoS 1 和 QoS 2 消息
    private int packetId;

    /**
     * QoS 等级是 0 的消息没有packetId
     */
    public PublishVariableHeader(String topic) {
        this.topic = topic;
    }

    /**
     * 只有当 QoS 等级是 1 或 2 时，报文标识符（Packet Identifier）字段才能出现在 PUBLISH 报文中
     */
    public PublishVariableHeader(String topic, int packetId) {
        this.topic = topic;
        this.packetId = packetId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }
}