package protocol;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.log4j.Logger;
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
public class MqttProcess extends ChannelHandlerAdapter {
    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 协议的业务处理类
        ProtocolProcess process = ProtocolProcess.getInstance();
        Message message = (Message) msg;

        switch (message.getFixedHeader().getMessageType()) {
            case CONNECT:
                logger.error("------------------------------------------------- 1-CONNECT");
                process.processConnect(ctx.channel(), (ConnectMessage) message);
                break;
            case CONNACK:
                logger.error("------------------------------------------------- 2-CONNACK");
                break;
            case PUBLISH:
                logger.error("------------------------------------------------- 3-PUBLISH");
                process.processPublic(ctx.channel(), (PublishMessage) message);
                break;
            case PUBACK:
                logger.error("------------------------------------------------- 4-PUBACK");
                process.processPubAck(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBREC:
                logger.error("------------------------------------------------- 5-PUBREC");
                process.processPubRec(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBREL:
                logger.error("------------------------------------------------- 6-PUBREL");
                process.processPubRel(ctx.channel(), (PacketIdVariableHeader) message.getVariableHeader());
                break;
            case PUBCOMP:
                logger.error("------------------------------------------------- 7-PUBCOMP");
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
                process.processUnSubscribe(ctx.channel(), (UnsubscribeMessage) message);
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
                logger.error("------------------------------------------------- DEFAULT");
                break;
        }
    }

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
}
