package protocol.message;

/**
 * MQTT协议ConnAck消息类型实现类，连接确认消息类型
 */
public class ConnAckMessage extends Message {
    /**
     * 连接应答返回码（连接状态）
     */
    public enum ConnectionStatus {
        // 连接接受
        ACCEPTED(0x00),
        // 连接拒绝（不可接受的版本）
        UNACCEPTABLE_PROTOCOL_VERSION(0x01),
        // 连接拒绝（Client ID 服务器不允许）
        IDENTIFIER_REJECTED(0x02),
        // 连接拒绝（服务器不可达）
        SERVER_UNAVAILABLE(0x03),
        // 连接拒绝（错误的用户名和密码)
        BAD_USERNAME_OR_PASSWORD(0x04),
        // 连接拒绝（客户端没有通过授权认证）
        NOT_AUTHORIZED(0x05);

        private final int value;

        ConnectionStatus(int value) {
            this.value = value;
        }

        /**
         * 获取类型对应的值
         */
        public int value() {
            return value;
        }

        //通过读取到的整型来获取对应的QoS类型
        public static ConnectionStatus valueOf(byte i) {
            for (ConnectionStatus q : ConnectionStatus.values()) {
                if (q.value == i)
                    return q;
            }
            throw new IllegalArgumentException("连接响应值无效: " + i);
        }
    }

    /**
     * Fixed header + Variable header，没有Payload
     */
    public ConnAckMessage(FixedHeader fixedHeader, ConnAckVariableHeader variableHeader) {
        super(fixedHeader, variableHeader);
    }

    @Override
    public ConnAckVariableHeader getVariableHeader() {
        return (ConnAckVariableHeader) super.getVariableHeader();
    }

}
