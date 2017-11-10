package protocol.message;

import io.netty.buffer.ByteBuf;

/**
 * MQTT协议Publish消息类型实现类，发布消息的消息类型
 */
public class PublishMessage extends Message {
    /**
     * Fixed header + Variable header + Payload
     */
    public PublishMessage(FixedHeader fixedHeader, PublishVariableHeader variableHeader, ByteBuf payload) {
        super(fixedHeader, variableHeader, payload);
    }

    @Override
    public PublishVariableHeader getVariableHeader() {
        return (PublishVariableHeader) super.getVariableHeader();
    }

    @Override
    public ByteBuf getPayload() {
        return (ByteBuf) super.getPayload();
    }
}
