package protocol.message;

import io.netty.util.internal.StringUtil;

/**
 * MQTT协议的固定头部，有一个字节，由四个字段组成
 */
public class FixedHeader {
    private final MessageType messageType;  // MQTT协议头前4bit，代表报文类型
    private boolean dup;                    // MQTT协议头第5bit，代表打开标志，表示是否第一次发送
    private QoS qos;                        // MQTT协议头前6,7bit，代表服务质量
    private boolean retain;                 // MQTT协议头第8bit，代表是否保持
    private int remainingLength;            // 第二个字节，代表剩余长度

    public FixedHeader(MessageType messageType, boolean dup, QoS qos, boolean retain) {
        this.messageType = messageType;
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;
    }

    public FixedHeader(MessageType messageType, boolean dup, QoS qos, boolean retain, int remainingLength) {
        this.messageType = messageType;
        this.dup = dup;
        this.qos = qos;
        this.retain = retain;
        this.remainingLength = remainingLength;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean isDup() {
        return dup;
    }

    public void setDup(boolean dup) {
        this.dup = dup;
    }

    public QoS getQos() {
        return qos;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public int getRemainingLength() {
        return remainingLength;
    }

    public void setRemainingLength(int remainingLength) {
        this.remainingLength = remainingLength;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(StringUtil.simpleClassName(this))
                .append('[')
                .append("messageType=").append(messageType)
                .append(", isDup=").append(dup)
                .append(", qosLevel=").append(qos)
                .append(", isRetain=").append(retain)
                .append(", messageLength=").append(remainingLength)
                .append(']');
        return stringBuilder.toString();
    }

    /**
     * QoS=0
     */
    public static FixedHeader getConnectFixedHeader() {
        return new FixedHeader(MessageType.CONNECT, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getConnAckFixedHeader() {
        return new FixedHeader(MessageType.CONNACK, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * 重发标志dup + 服务质量等级QoS=? + 保留标志retain
     */
    public static FixedHeader getPublishFixedHeader(boolean dup, QoS qos, boolean retain) {
        return new FixedHeader(MessageType.PUBLISH, dup, qos, retain);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getPubAckFixedHeader() {
        return new FixedHeader(MessageType.PUBACK, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getPubRecFixedHeader() {
        return new FixedHeader(MessageType.PUBREC, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=1
     */
    public static FixedHeader getPubRelFixedHeader() {
        return new FixedHeader(MessageType.PUBREL, false, QoS.AT_LEAST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getPubCompFixedHeader() {
        return new FixedHeader(MessageType.PUBCOMP, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=1
     */
    public static FixedHeader getSubscribeFixedHeader() {
        return new FixedHeader(MessageType.SUBSCRIBE, false, QoS.AT_LEAST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getSubAckFixedHeader() {
        return new FixedHeader(MessageType.SUBACK, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=1
     */
    public static FixedHeader getUnsubscribeFixedHeader() {
        return new FixedHeader(MessageType.UNSUBSCRIBE, false, QoS.AT_LEAST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getUnsubAckFixedHeader() {
        return new FixedHeader(MessageType.UNSUBACK, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getPingRespFixedHeader() {
        return new FixedHeader(MessageType.PINGRESP, false, QoS.AT_MOST_ONCE, false);
    }

    /**
     * QoS=0
     */
    public static FixedHeader getDisconnectFixedHeader() {
        return new FixedHeader(MessageType.DISCONNECT, false, QoS.AT_MOST_ONCE, false);
    }
}
