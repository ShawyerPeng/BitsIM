package protocol.message;

/**
 * MQTT协议UnSubscribe消息类型实现类，用于取消订阅topic
 */
public class UnsubscribeMessage extends Message {
    /**
     * Fixed header + Variable header + Payload
     */
    public UnsubscribeMessage(FixedHeader fixedHeader, PacketIdVariableHeader variableHeader, UnsubscribePayload payload) {
        super(fixedHeader, variableHeader, payload);
    }

    @Override
    public PacketIdVariableHeader getVariableHeader() {
        return (PacketIdVariableHeader) super.getVariableHeader();
    }

    @Override
    public UnsubscribePayload getPayload() {
        return (UnsubscribePayload) super.getPayload();
    }
}
