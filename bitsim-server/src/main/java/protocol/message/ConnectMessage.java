package protocol.message;

/**
 * MQTT协议Connect消息类型实现类，客户端请求服务器连接的消息类型
 */
public class ConnectMessage extends Message {
    /**
     * Fixed header + Variable header + Payload
     */
    public ConnectMessage(FixedHeader fixedHeader, ConnectVariableHeader variableHeader, ConnectPayload payload) {
        super(fixedHeader, variableHeader, payload);
    }

    @Override
    public ConnectVariableHeader getVariableHeader() {
        return (ConnectVariableHeader) super.getVariableHeader();
    }

    @Override
    public ConnectPayload getPayload() {
        return (ConnectPayload) super.getPayload();
    }
}