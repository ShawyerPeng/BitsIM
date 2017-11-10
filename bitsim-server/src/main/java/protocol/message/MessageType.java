package protocol.message;

/**
 * MQTT协议总共有十四种类型
 */
public enum MessageType {
    CONNECT(1),     // 客户端请求连接服务端
    CONNACK(2),     // 连接确认
    PUBLISH(3),     // 发布消息
    PUBACK(4),      // 发布确认
    PUBREC(5),      // 发布接收（有保证的交付第1部分）
    PUBREL(6),      // 发布释放（有保证的交付第2部分）
    PUBCOMP(7),     // 发布完成（有保证的交付第3部分）
    SUBSCRIBE(8),   // 客户端订阅请求
    SUBACK(9),      // 订阅确认
    UNSUBSCRIBE(10),// 客户端取消订阅请求
    UNSUBACK(11),   // 取消订阅确认
    PINGREQ(12),    // PING请求
    PINGRESP(13),   // PING回复
    DISCONNECT(14); // 客户端断开连接

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    /**
     * 获取类型对应的值
     */
    public int value() {
        return value;
    }

    /**
     * 把值转变成对应的类型并返回
     */
    public static MessageType valueOf(int type) {
        for (MessageType m : values()) {
            if (m.value == type) {
                return m;
            }
        }
        throw new IllegalArgumentException("未知的MQTT协议类型：" + type);
    }

}
