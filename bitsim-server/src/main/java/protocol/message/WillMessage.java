package protocol.message;

import io.netty.buffer.ByteBuf;

/**
 * 遗嘱信息类
 */
public class WillMessage {
    private final String willTopic;
    private final ByteBuf willMessage;
    private final boolean willRetain;
    private final QoS willQoS;

    public WillMessage(String willTopic, ByteBuf willMessage, boolean willRetain, QoS willQoS) {
        this.willTopic = willTopic;
        this.willMessage = willMessage;
        this.willRetain = willRetain;
        this.willQoS = willQoS;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public ByteBuf getWillMessage() {
        return willMessage;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public QoS getWillQoS() {
        return willQoS;
    }

    @Override
    public String toString() {
        return "WillMessage{" +
                "willTopic='" + willTopic + '\'' +
                ", willMessage=" + willMessage +
                ", willRetain=" + willRetain +
                ", willQoS=" + willQoS +
                '}';
    }

    //// 遗嘱信息类
    //static final class WillMessage {
    //    private final String topic;
    //    private final ByteBuf payload;
    //    private final boolean retained;
    //    private final QoS qos;
    //
    //    public WillMessage(String topic, ByteBuf payload, boolean retained, QoS qos) {
    //        this.topic = topic;
    //        this.payload = payload;
    //        this.retained = retained;
    //        this.qos = qos;
    //    }
    //
    //    public String getTopic() {
    //        return topic;
    //    }
    //
    //    public ByteBuf getPayload() {
    //        return payload;
    //    }
    //
    //    public boolean isRetained() {
    //        return retained;
    //    }
    //
    //    public QoS getQos() {
    //        return qos;
    //    }
    //}
}