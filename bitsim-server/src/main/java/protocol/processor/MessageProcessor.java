package protocol.processor;

import com.google.common.collect.Maps;
import com.google.protobuf.AbstractMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protocol.MqttMesageFactory;
import protocol.message.*;
import protocol.process.ConnectionDescriptor;
import protocol.process.interfaces.IAuthenticator;
import protocol.process.interfaces.IMessageStore;
import protocol.process.interfaces.ISessionStore;
import protocol.process.interfaces.impl.IdentityAuthenticator;
import protocol.process.interfaces.impl.MapDBPersistentStore;
import protocol.process.store.ClientSession;
import protocol.process.store.impl.SessionStoreImpl;
import protocol.process.subscribe.SubscribeStore;

import java.util.concurrent.ConcurrentMap;

/**
 * 协议处理类的公共父类
 */
public class MessageProcessor {
    protected final static Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    // 客户端连接映射表
    protected ConcurrentMap<String, ConnectionDescriptor> clientIDs = Maps.newConcurrentMap();
    // 存储遗嘱信息，通过ID映射遗嘱信息
    protected ConcurrentMap<String, WillMessage> willStore = Maps.newConcurrentMap();
    // 存储Session
    protected ISessionStore sessionStore = new MapDBPersistentStore();
    // 身份验证
    protected IAuthenticator authenticator = new IdentityAuthenticator();
    // 消息存储
    protected IMessageStore messagesStore = new MapDBPersistentStore();
    // 订阅存储
    protected SubscribeStore subscribeStore = new SubscribeStore();

    protected void setIdleTime(ChannelPipeline pipeline, int idleTime) {
        if (pipeline.names().contains("idleStateHandler")) {
            pipeline.remove("idleStateHandler");
        }
        pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, idleTime));
    }

    protected void directSend(ClientSession clientSession, String topic, QoS qos, boolean retained, Integer packetId, ByteBuf message) {
        String clientId = clientSession.getClientId();
        logger.debug("directSend invoked clientId <{}> on topic <{}> QoS {} retained {} messageId {}", clientId, topic, qos, retained, packetId);

        PublishMessage pubMessage = (PublishMessage) MqttMesageFactory.newMessage(
                FixedHeader.getPublishFixedHeader(false, qos, retained), new PublishVariableHeader(topic, packetId), message);
        logger.info("send publish message to <{}> on topic <{}>", clientId, topic);

        // set the PacketIdentifier only for QoS > 0
        if (pubMessage.getFixedHeader().getQos() != QoS.AT_MOST_ONCE) {
            pubMessage.getVariableHeader().setPacketId(packetId);
        } else {
            if (packetId != null) {
                throw new RuntimeException("Internal bad error, trying to forwardPublish a QoS 0 message with PacketIdentifier: " + packetId);
            }
        }
        if (clientIDs == null) {
            throw new RuntimeException("Internal bad error, found m_clientIDs to null while it should be initialized, somewhere it's overwritten!!");
        }
        logger.debug("clientIDs are {}", clientIDs);
        if (clientIDs.get(clientId) == null) {
            //TODO while we were publishing to the target client, that client disconnected,
            // could happen is not an error HANDLE IT
            throw new RuntimeException(String.format("Can't find a ConnectionDescriptor for client <%s> in cache <%s>", clientId, clientIDs));
        }
        Channel channel = clientIDs.get(clientId).getClient();
        logger.debug("Session for clientId {} is {}", clientId, channel);
        channel.writeAndFlush(pubMessage);
    }

    public void processConnectionLost(String clientID, boolean sessionStolen, Channel channel) {
        ConnectionDescriptor oldConnDescr = new ConnectionDescriptor(clientID, channel, true);
        clientIDs.remove(clientID, oldConnDescr);
        //If already removed a disconnect message was already processed for this clientID
        if (sessionStolen) {
            //de-activate the subscriptions for this ClientID
            ClientSession clientSession = sessionStore.sessionForClient(clientID);
            clientSession.setActive(false);
            logger.info("Lost connection with client <{}>", clientID);
        }
        //publish the Will message (if any) for the clientID
        if (!sessionStolen && willStore.containsKey(clientID)) {
            WillMessage will = willStore.get(clientID);
            forwardPublishWill(will, clientID);
            willStore.remove(clientID);
        }
    }


    /**
     * TODO:Specialized version to publish will testament message.
     */
    private void forwardPublishWill(WillMessage willMessage, String clientID) {
        // it has just to publish the message downstream to the subscribers
        // NB it's a will publish, it needs a PacketIdentifier for this conn, default to 1
        short messageId = 0;
        if (willMessage.getWillQoS() != QoS.AT_MOST_ONCE) {
            messageId = sessionStore.sessionForClient(clientID).getNextMessageId();
        }

//        IMessagesStore.StoredMessage tobeStored = asStoredMessage(will);
//        tobeStored.setClientID(clientID);
//        tobeStored.setMessageID(messageId);
//        route2Subscribers(tobeStored);
    }
}
