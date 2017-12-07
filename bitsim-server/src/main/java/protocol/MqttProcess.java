package protocol;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import protocol.message.ConnectMessage;
import protocol.message.Message;
import protocol.message.PacketIdVariableHeader;
import protocol.message.PublishMessage;
import protocol.message.SubscribeMessage;
import protocol.message.UnsubscribeMessage;
import protocol.process.ProtocolProcess;

/**
 * MQTT协议业务处理
 */
@ChannelHandler.Sharable
@Component
public class MqttProcess extends ChannelHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MqttProcess.class);

    //@Autowired
    //private ConnectProcessor connMessageProcessor;
    //@Autowired
    //private PublishProcessor publishMessageProcessor;
    //@Autowired
    //private SubscribeProcessor subscribeMessageProcessor;
    //@Autowired
    //private UnsubscribeProcessor unSubscriptionMessageProcessor;
    //@Autowired
    //private DisconnectProcessor disconnectMessageProcessor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 协议的业务处理类
        ProtocolProcess process = ProtocolProcess.getInstance();
        Message message = (Message) msg;

        logger.error("收到客户端的消息：[Message]{}", message.toString());

        switch (message.getFixedHeader().getMessageType()) {
            case CONNECT:
                logger.error("------------------------------------------------- 1-CONNECT");
                logger.error("收到客户端的消息：[VariableHeader]{}", ((ConnectMessage) message).getVariableHeader().toString());
                logger.error("收到客户端的消息：[Payload]{}", ((ConnectMessage) message).getPayload().toString());
                process.processConnect(ctx.channel(), (ConnectMessage) message);
                break;
            case CONNACK:
                logger.error("------------------------------------------------- 2-CONNACK");
                break;
            case PUBLISH:
                logger.error("------------------------------------------------- 3-PUBLISH");
                logger.error("收到客户端的消息：[VariableHeader]{}", ((PublishMessage) message).getVariableHeader().toString());
                logger.error("收到客户端的消息：[Payload]{}", ((PublishMessage) message).getPayload().toString());
                process.processPublish(ctx.channel(), (PublishMessage) message);
                break;
            case PUBACK:
                logger.error("------------------------------------------------- 4-PUBACK");
                logger.error("收到客户端的消息：[VariableHeader]{}", ((PacketIdVariableHeader) message.getVariableHeader()).toString());
                process.processPubAck(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBREC:
                //logger.error("------------------------------------------------- 5-PUBREC");
                process.processPubRec(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBREL:
                logger.error("------------------------------------------------- 6-PUBREL");
                process.processPubRel(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBCOMP:
                //logger.error("------------------------------------------------- 7-PUBCOMP");
                process.processPubComp(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case SUBSCRIBE:
                logger.error("------------------------------------------------- 8-SUBSCRIBE");
                process.processSubscribe(ctx.channel(), (SubscribeMessage) message);
                break;
            case SUBACK:
                logger.error("------------------------------------------------- 9-SUBACK");
                break;
            case UNSUBSCRIBE:
                logger.error("------------------------------------------------- 10-UNSUBSCRIBE");
                process.processUnsubscribe(ctx.channel(), (UnsubscribeMessage) message);
                break;
            case UNSUBACK:
                logger.error("------------------------------------------------- 11-UNSUBACK");
                break;
            case PINGREQ:
                logger.error("------------------------------------------------- 12-PINGREQ");
                process.processPingReq(ctx.channel(), message);
                break;
            case PINGRESP:
                logger.error("------------------------------------------------- 13-PINGRESP");
                break;
            case DISCONNECT:
                logger.error("------------------------------------------------- 14-DISCONNECT");
                process.processDisconnet(ctx.channel(), message);
                break;
            default:
                logger.error("Unknown MessageType:{}", message.getFixedHeader().getMessageType());
                break;
        }
    }

    //@Override
    //public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    //    String clientID = NettyAttrManager.getAttrClientId(ctx.channel());
    //    if (clientID != null && !clientID.isEmpty()) {
    //        // if the channel was of a correctly connected client, inform messaging
    //        // else it was of a not completed CONNECT message or sessionStolen
    //        boolean stolen = false;
    //        Boolean stolenAttr = NettyAttrManager.getAttrCleanSession(ctx.channel());
    //        if (stolenAttr != null && stolenAttr == Boolean.TRUE) {
    //            stolen = true;
    //        }
    //        new SubscribeProcessor().processConnectionLost(clientID, stolen, ctx.channel());
    //    }
    //    ctx.close();
    //}

    /**
     * 事件追踪，处理超时事件，一旦检测到读超时，就断开连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) event;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            } else {
                // 写超时不处理
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CorruptedFrameException) {
            //something goes bad with decoding
            logger.warn("Error decoding a packet, probably a bad formatted packet, message: " + cause.getMessage());
        } else {
            logger.error("Ugly error on networking", cause);
        }
        ctx.close();
    }
}
