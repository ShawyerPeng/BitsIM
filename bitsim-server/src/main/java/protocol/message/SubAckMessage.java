package protocol.message;


/**
 * MQTT协议SubAck消息类型实现类，对Subscribe包的确认
 */
public class SubAckMessage extends Message {
    /**
     * Fixed header + Variable header + Payload
     */
    public SubAckMessage(FixedHeader fixedHeader, PacketIdVariableHeader variableHeader, SubAckPayload payload) {
        super(fixedHeader, variableHeader, payload);
    }

    @Override
    public PacketIdVariableHeader getVariableHeader() {
        return (PacketIdVariableHeader) super.getVariableHeader();
    }

    @Override
    public SubAckPayload getPayload() {
        return (SubAckPayload) super.getPayload();
    }
}
