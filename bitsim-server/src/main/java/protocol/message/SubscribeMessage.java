package protocol.message;

/**
 * MQTT协议Subscribe消息类型实现类，用于订阅topic，订阅了消息的客户端，可以接受对应topic的信息
 */
public class SubscribeMessage extends Message {
    /**
     * Fixed header + Variable header + Payload
     */
    public SubscribeMessage(FixedHeader fixedHeader, PacketIdVariableHeader variableHeader, SubscribePayload payload) {
        super(fixedHeader, variableHeader, payload);
    }

    @Override
    public PacketIdVariableHeader getVariableHeader() {
        return (PacketIdVariableHeader) super.getVariableHeader();
    }

    @Override
    public SubscribePayload getPayload() {
        return (SubscribePayload) super.getPayload();
    }
}
