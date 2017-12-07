package protocol;

import protocol.message.ConnAckMessage;
import protocol.message.ConnAckVariableHeader;
import protocol.message.ConnectMessage;
import protocol.message.ConnectPayload;
import protocol.message.ConnectVariableHeader;
import protocol.message.FixedHeader;
import protocol.message.Message;
import protocol.message.PacketIdVariableHeader;
import protocol.message.PublishMessage;
import protocol.message.PublishVariableHeader;
import protocol.message.SubAckMessage;
import protocol.message.SubAckPayload;
import protocol.message.SubscribeMessage;
import protocol.message.SubscribePayload;
import protocol.message.UnsubscribeMessage;
import protocol.message.UnsubscribePayload;

import io.netty.buffer.ByteBuf;

/**
 * 消息工厂类
 */
public final class MqttMesageFactory {
    public static Message newMessage(FixedHeader fixedHeader, Object variableHeader, Object payload) {
        switch (fixedHeader.getMessageType()) {
            case CONNECT:
                return new ConnectMessage(fixedHeader,
                        (ConnectVariableHeader) variableHeader,
                        (ConnectPayload) payload);

            case CONNACK:
                return new ConnAckMessage(fixedHeader, (ConnAckVariableHeader) variableHeader);

            case SUBSCRIBE:
                return new SubscribeMessage(
                        fixedHeader,
                        (PacketIdVariableHeader) variableHeader,
                        (SubscribePayload) payload);

            case SUBACK:
                return new SubAckMessage(
                        fixedHeader,
                        (PacketIdVariableHeader) variableHeader,
                        (SubAckPayload) payload);

            case UNSUBSCRIBE:
                return new UnsubscribeMessage(
                        fixedHeader,
                        (PacketIdVariableHeader) variableHeader,
                        (UnsubscribePayload) payload);

            case PUBLISH:
                return new PublishMessage(
                        fixedHeader,
                        (PublishVariableHeader) variableHeader,
                        (ByteBuf) payload);

            case PUBACK:
            case UNSUBACK:
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
                return new Message(fixedHeader, variableHeader);

            case PINGREQ:
            case PINGRESP:
            case DISCONNECT:
                return new Message(fixedHeader);

            default:
                throw new IllegalArgumentException("unknown message type: " + fixedHeader.getMessageType());
        }
    }

    private MqttMesageFactory() {
    }
}