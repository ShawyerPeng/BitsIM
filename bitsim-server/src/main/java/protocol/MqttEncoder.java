package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;

import org.apache.log4j.Logger;

import protocol.message.ConnAckMessage;
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
import protocol.message.TopicSubscribe;
import protocol.message.UnsubscribeMessage;
import protocol.message.UnsubscribePayload;

/**
 * MQTT协议编码。想产生 Message 对象，并将他们编码成 ByteBuf 来发送到线上。
 */
public class MqttEncoder extends MessageToByteEncoder<Message> {
    private final static Logger logger = Logger.getLogger(MqttEncoder.class);
    public static final int MAX_LENGTH_LIMIT = 268435455;
    private final byte[] EMPTY = new byte[0];
    private final int UTF8_FIX_LENGTH = 2;//UTF编码的byte，最开始必须为2字节的长度字段

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        //
        ByteBufAllocator byteBufAllocator = ctx.alloc();
        // 最终被编码成ByteBuf对象
        ByteBuf encodedByteBuf;

        switch (msg.getFixedHeader().getMessageType()) {
            case CONNECT:
                encodedByteBuf = encodeConnectMessage(byteBufAllocator, (ConnectMessage) msg);
                break;
            case CONNACK:
                encodedByteBuf = encodeConnAckMessage(byteBufAllocator, (ConnAckMessage) msg);
                break;
            case PUBLISH:
                encodedByteBuf = encodePublishMessage(byteBufAllocator, (PublishMessage) msg);
                break;
            case SUBSCRIBE:
                encodedByteBuf = encodeSubscribeMessage(byteBufAllocator, (SubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                encodedByteBuf = encodeUnsubcribeMessage(byteBufAllocator, (UnsubscribeMessage) msg);
                break;
            case SUBACK:
                encodedByteBuf = encodeSubAckMessage(byteBufAllocator, (SubAckMessage) msg);
                break;
            case UNSUBACK:
            case PUBACK:
            case PUBREC:
            case PUBREL:
            case PUBCOMP:
                encodedByteBuf = encodeMessageByteFixedHeaderAndPacketId(byteBufAllocator, msg);
                break;
            case PINGREQ:
            case PINGRESP:
            case DISCONNECT:
                encodedByteBuf = encodeMessageByteFixedHeader(byteBufAllocator, msg);
                break;
            default:
                throw new IllegalArgumentException("未知的MQTT协议类型：" + msg.getFixedHeader().getMessageType().value());
        }
        // 写Message对象到ByteBuf
        out.writeBytes(encodedByteBuf);
    }

    /**
     * 编码ConnectMessage
     */
    private ByteBuf encodeConnectMessage(ByteBufAllocator bufAllocator, ConnectMessage message) {
        // 把消息每个字段从POJO中取出，并计算其大小，写入byteBuf
        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 10;//根据协议3.1.1，connect可变头固定大小为10
        int payloadSize = 0;//荷载大小

        // 取出固定头部所有信息
        FixedHeader fixedHeader = message.getFixedHeader();
        ConnectVariableHeader connectVariableHeader = message.getVariableHeader();
        ConnectPayload connectPayload = message.getPayload();

        // 取出可变头部所有信息
        String mqttName = connectVariableHeader.getProtocolName();
        byte[] mqttNameBytes = encodeStringUTF8(mqttName);
        int mqttVersion = connectVariableHeader.getProtocolVersionNumber();
        int connectflags = connectVariableHeader.isCleanSession() ? 0x02 : 0;
        connectflags |= connectVariableHeader.isHasWill() ? 0x04 : 0;
        connectflags |= connectVariableHeader.getWillQoS() == null ? 0 : connectVariableHeader.getWillQoS().val << 3;
        connectflags |= connectVariableHeader.isWillRetain() ? 0x20 : 0;
        connectflags |= connectVariableHeader.isHasPassword() ? 0x40 : 0;
        connectflags |= connectVariableHeader.isHasUsername() ? 0x80 : 0;
        int keepAlive = connectVariableHeader.getKeepAlive();

        // 取出荷载信息并计算荷载的大小
        String clientId = connectPayload.getClientId();
        byte[] clientIdBytes = encodeStringUTF8(clientId);
        payloadSize += clientIdBytes.length;

        String willTopic = connectPayload.getWillTopic();
        byte[] willTopicBytes = willTopic != null ? encodeStringUTF8(willTopic) : EMPTY;
        String willMessage = connectPayload.getWillMessage();
        byte[] willMessageBytes = willMessage != null ? encodeStringUTF8(willMessage) : EMPTY;
        if (connectVariableHeader.isHasWill()) {
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += willTopicBytes.length;
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += willMessageBytes.length;
        }

        String username = connectPayload.getUsername();
        byte[] usernameBytes = username != null ? encodeStringUTF8(username) : EMPTY;
        if (connectVariableHeader.isHasUsername()) {
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += usernameBytes.length;
        }

        String password = connectPayload.getPassword();
        byte[] passwordBytes = password != null ? encodeStringUTF8(password) : EMPTY;
        if (connectVariableHeader.isHasPassword()) {
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += passwordBytes.length;
        }

        // 计算固定头部长度，长度为可变头部长度+荷载长度编码的长度
        fixHeaderSize += countVariableLengthInt(variableHeaderSize + payloadSize);
        // 根据所有字段长度生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize + payloadSize);

        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize + payloadSize));//写固定头部第二个字节，剩余部分长度

        byteBuf.writeShort(mqttNameBytes.length);//写入协议名长度
        byteBuf.writeBytes(mqttNameBytes);//写入协议名
        byteBuf.writeByte(mqttVersion);//写入协议版本号
        byteBuf.writeByte(connectflags);//写入连接标志
        byteBuf.writeByte(keepAlive);//写入心跳包时长

        byteBuf.writeShort(clientIdBytes.length);//写入客户端ID长度
        byteBuf.writeBytes(clientIdBytes);//写入客户端ID
        if (connectVariableHeader.isHasWill()) {
            byteBuf.writeShort(willTopicBytes.length);//写入遗嘱主题长度
            byteBuf.writeBytes(willTopicBytes);//写入遗嘱主题
            byteBuf.writeShort(willMessageBytes.length);//写入遗嘱正文长度
            byteBuf.writeBytes(willMessageBytes);//写入遗嘱正文
        }
        if (connectVariableHeader.isHasUsername()) {
            byteBuf.writeShort(usernameBytes.length);
            byteBuf.writeBytes(usernameBytes);
        }
        if (connectVariableHeader.isHasPassword()) {
            byteBuf.writeShort(passwordBytes.length);
            byteBuf.writeBytes(passwordBytes);
        }

        return byteBuf;
    }

    /**
     * 编码ConnAckMessage
     */
    private ByteBuf encodeConnAckMessage(ByteBufAllocator bufAllocator, ConnAckMessage msg) {
        // 由协议3.1.1 P28可知，ConnAck消息长度固定为4字节
        ByteBuf byteBuf = bufAllocator.buffer(4);
        byteBuf.writeBytes(encodeFixHeader(msg.getFixedHeader()));//写固定头部第一个字节
        byteBuf.writeByte(2);//写入可变头部长度，固定为2字节
        byteBuf.writeByte(msg.getVariableHeader().isSessionPresent() ? 0x01 : 0x00);//写入连接确认标志
        byteBuf.writeByte(msg.getVariableHeader().getStatus().value());//写入返回码
        return byteBuf;
    }

    /**
     * 编码PublishMessage
     */
    private ByteBuf encodePublishMessage(ByteBufAllocator bufAllocator, PublishMessage message) {
        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 0;
        int payloadSize = 0;//荷载大小

        FixedHeader fixedHeader = message.getFixedHeader();
        PublishVariableHeader variableHeader = message.getVariableHeader();
        ByteBuf payload = message.getPayload().duplicate();

        String topicName = variableHeader.getTopic();
        byte[] topicNameBytes = encodeStringUTF8(topicName);

        variableHeaderSize += UTF8_FIX_LENGTH;
        variableHeaderSize += topicNameBytes.length;
        variableHeaderSize += fixedHeader.getQos().value() > 0 ? 2 : 0;//根据qos判断packageID的长度是否需要加上
        payloadSize = payload.readableBytes();
        fixHeaderSize += countVariableLengthInt(variableHeaderSize + payloadSize);

        // 生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize + payloadSize);
        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize + payloadSize));//写固定头部第二个字节，剩余部分长度
        byteBuf.writeShort(topicNameBytes.length);
        byteBuf.writeBytes(topicNameBytes);
        if (fixedHeader.getQos().value() > 0) {
            byteBuf.writeShort(variableHeader.getPacketId());
        }
        byteBuf.writeBytes(payload);//写入荷载

        return byteBuf;
    }

    /**
     * 编码SubscribeMessage
     */
    private ByteBuf encodeSubscribeMessage(ByteBufAllocator bufAllocator, SubscribeMessage message) {
        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 2;//协议P37页，订阅类型的可变头部长度都为2
        int payloadSize = 0;//荷载大小

        FixedHeader fixedHeader = message.getFixedHeader();
        PacketIdVariableHeader variableHeader = message.getVariableHeader();
        SubscribePayload payload = message.getPayload();

        // 遍历订阅消息组，计算荷载长度
        for (TopicSubscribe topic : payload.getTopicSubscribes()) {
            String topicName = topic.getTopicFilter();
            byte[] topicNameBytes = encodeStringUTF8(topicName);
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += topicNameBytes.length;
            payloadSize += 1;//添加qos的长度，qos长度只能为1
        }

        fixHeaderSize += countVariableLengthInt(variableHeaderSize + payloadSize);

        // 生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize + payloadSize);
        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize + payloadSize));//写固定头部第二个字节，剩余部分长度
        byteBuf.writeShort(variableHeader.getPacketId());//写入可变头部中的包ID
        // 写入荷载
        for (TopicSubscribe topic : payload.getTopicSubscribes()) {
            String topicName = topic.getTopicFilter();
            byte[] topicNameBytes = encodeStringUTF8(topicName);
            byteBuf.writeShort(topicNameBytes.length);
            byteBuf.writeBytes(topicNameBytes);
            byteBuf.writeByte(topic.getRequestedQoS().value());
        }

        return byteBuf;
    }

    /**
     * 编码UnsubcribeMessage
     */
    private ByteBuf encodeUnsubcribeMessage(ByteBufAllocator bufAllocator, UnsubscribeMessage message) {
        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 2;//协议P42页，取消订阅类型的可变头部长度固定为2
        int payloadSize = 0;//荷载大小

        FixedHeader fixedHeader = message.getFixedHeader();
        PacketIdVariableHeader variableHeader = message.getVariableHeader();
        UnsubscribePayload payload = message.getPayload();

        for (String topic : payload.getTopics()) {
            byte[] topicBytes = encodeStringUTF8(topic);
            payloadSize += UTF8_FIX_LENGTH;
            payloadSize += topicBytes.length;
        }

        fixHeaderSize += countVariableLengthInt(variableHeaderSize + payloadSize);

        // 生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize + payloadSize);
        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize + payloadSize));//写固定头部第二个字节，剩余部分长度
        byteBuf.writeShort(variableHeader.getPacketId());//写入可变头部中的包ID
        // 写入荷载
        for (String topic : payload.getTopics()) {
            byte[] topicBytes = encodeStringUTF8(topic);
            byteBuf.writeShort(topicBytes.length);
            byteBuf.writeBytes(topicBytes);
        }

        return byteBuf;
    }

    /**
     * 编码SubAckMessage
     */
    private ByteBuf encodeSubAckMessage(ByteBufAllocator bufAllocator, SubAckMessage message) {
        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 2;//协议P42页，取消订阅类型的可变头部长度固定为2
        int payloadSize = 0;//荷载大小

        FixedHeader fixedHeader = message.getFixedHeader();
        PacketIdVariableHeader variableHeader = message.getVariableHeader();
        SubAckPayload payload = message.getPayload();

        List<Integer> grantedQosLevels = payload.getGrantedQosLevel();
        payloadSize += grantedQosLevels.size();

        fixHeaderSize += countVariableLengthInt(variableHeaderSize + payloadSize);

        // 生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize + payloadSize);
        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize + payloadSize));//写固定头部第二个字节，剩余部分长度
        byteBuf.writeShort(variableHeader.getPacketId());//写入可变头部中的包ID
        for (Integer qos : grantedQosLevels) {
            byteBuf.writeByte(qos);
        }

        return byteBuf;
    }

    /**
     * 编码FixedHeader和PacketId
     */
    private ByteBuf encodeMessageByteFixedHeaderAndPacketId(ByteBufAllocator bufAllocator, Message message) {

        int fixHeaderSize = 1;//固定头部有1字节+可变部分长度字节
        int variableHeaderSize = 2;//只包含包ID的可变头部，长度固定为2

        FixedHeader fixedHeader = message.getFixedHeader();
        PacketIdVariableHeader variableHeader = (PacketIdVariableHeader) message.getVariableHeader();

        fixHeaderSize += countVariableLengthInt(variableHeaderSize);

        // 生成bytebuf
        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize + variableHeaderSize);
        // 写入byteBuf
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));//写固定头部第一个字节
        byteBuf.writeBytes(encodeRemainLength(variableHeaderSize));//写固定头部第二个字节，剩余部分长度
        byteBuf.writeShort(variableHeader.getPacketId());//写入可变头部中的包ID

        return byteBuf;
    }

    /**
     * 编码FixedHeader
     */
    private ByteBuf encodeMessageByteFixedHeader(ByteBufAllocator bufAllocator, Message message) {
        int fixHeaderSize = 2;//固定头部加上一个字节的剩余长度（剩余长度为0）
        FixedHeader fixedHeader = message.getFixedHeader();

        ByteBuf byteBuf = bufAllocator.buffer(fixHeaderSize);
        byteBuf.writeBytes(encodeFixHeader(fixedHeader));
        byteBuf.writeByte(0);//写入剩余长度，没有可变头部和荷载，所以剩余长度为0

        return null;
    }

    /**
     * 编码固定头部第一个字节
     */
    private byte[] encodeFixHeader(FixedHeader fixedHeader) {
        byte b = 0;
        b = (byte) (fixedHeader.getMessageType().value() << 4);
        b |= fixedHeader.isDup() ? 0x8 : 0x0;
        b |= fixedHeader.getQos().value() << 1;
        b |= fixedHeader.isRetain() ? 0x1 : 0;

        byte[] bArray = new byte[]{b};
        return bArray;
    }

    /**
     * 把消息长度信息编码成字节
     */
    private ByteBuf encodeRemainLength(int length) {
        if (length > MAX_LENGTH_LIMIT || length < 0) {
            throw new CorruptedFrameException(
                    "消息长度不能超过‘消息最大长度’:" + MAX_LENGTH_LIMIT + ",当前长度：" + length);
        }
        //剩余长度字段最多可编码4字节
        ByteBuf encoded = Unpooled.buffer(4);
        byte digit;
        do {
            digit = (byte) (length % 128);
            length = length / 128;
            if (length > 0) {
                digit = (byte) (digit | 0x80);
            }
            encoded.writeByte(digit);
        } while (length > 0);
        return encoded;
    }

    /**
     * 计算固定头部中长度编码占用的字节
     * 协议3.1.1 P16对长度有说明，长度/128即可得到需要使用的字节数，一直除到0
     */
    private int countVariableLengthInt(int length) {
        int count = 0;
        do {
            length /= 128;
            count++;
        } while (length > 0);
        return count;
    }

    /**
     * 将String类型编码为byte[]
     */
    private byte[] encodeStringUTF8(String str) {
        return str.getBytes(CharsetUtil.UTF_8);
    }
}
