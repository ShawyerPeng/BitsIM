package protocol.processor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import protocol.MqttMesageFactory;
import protocol.message.*;
import protocol.process.ConnectionDescriptor;
import protocol.process.NettyAttrManager;
import protocol.process.event.PublishEvent;
import protocol.process.event.job.RePublishJob;
import protocol.process.interfaces.IMessageStore;
import protocol.process.subscribe.Subscription;
import service.UserService;
import util.QuartzManager;
import util.StringTool;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Connect 过程处理类
 */
public class ConnectProcessor extends MessageProcessor {
    private final static Logger Log = Logger.getLogger(ConnectProcessor.class);

    @Autowired
    private UserService userService;

    /**
     * 处理协议的CONNECT消息类型
     */
    public void processConnect(Channel client, ConnectMessage connectMessage) {
        Log.info("-------------------------------------------------");
        Log.info("处理Connect的数据");
        // 首先查看保留位是否为0，不为0则断开连接,协议P24
        if (!connectMessage.getVariableHeader().isReservedIsZero()) {
            client.close();
            return;
        }
        // 处理protocol name和protocol version, 如果返回码!=0，sessionPresent必为0，协议P24,P32
        if (!connectMessage.getVariableHeader().getProtocolName().equals("MQTT") ||
                connectMessage.getVariableHeader().getProtocolVersionNumber() != 4) {

            ConnAckMessage connAckMessage = (ConnAckMessage) MqttMesageFactory.newMessage(
                    FixedHeader.getConnAckFixedHeader(),
                    new ConnAckVariableHeader(ConnAckMessage.ConnectionStatus.UNACCEPTABLE_PROTOCOL_VERSION, false), null);

            client.writeAndFlush(connAckMessage);
            client.close();// 版本或协议名不匹配，则断开该客户端连接
            return;
        }

        // 处理Connect包的保留位不为0的情况，协议P24
        if (!connectMessage.getVariableHeader().isReservedIsZero()) {
            client.close();
        }

        // 处理clientId为null或长度为0的情况，协议P29
        if (connectMessage.getPayload().getClientId() == null || connectMessage.getPayload().getClientId().length() == 0) {
            // clientId为null的时候，cleanSession只能为1，此时给client设置一个随机的不存在的mac地址为ID，否则，断开连接
            if (connectMessage.getVariableHeader().isCleanSession()) {
                boolean isExist = true;
                String macClientId = StringTool.generalMacString();
                while (isExist) {
                    ConnectionDescriptor connectionDescriptor = clientIDs.get(macClientId);
                    if (connectionDescriptor == null) {
                        connectMessage.getPayload().setClientId(macClientId);
                        isExist = false;
                    } else {
                        macClientId = StringTool.generalMacString();
                    }
                }
            } else {
                // reject null clientID
                Log.info("客户端ID为空，cleanSession为0，根据协议，不接收此客户端");
                ConnAckMessage connAckMessage = (ConnAckMessage) MqttMesageFactory.newMessage(
                        FixedHeader.getConnAckFixedHeader(),
                        new ConnAckVariableHeader(ConnAckMessage.ConnectionStatus.IDENTIFIER_REJECTED, false), null);
                client.writeAndFlush(connAckMessage);
                client.close();
                return;
            }
        }

        //// 检查clientId的格式符合与否
        //if (!StringTool.isMacString(connectMessage.getPayload().getClientId())) {
        //    Log.info("客户端ID为{" + connectMessage.getPayload().getClientId() + "}，拒绝此客户端");
        //    ConnAckMessage connAckMessage = (ConnAckMessage) MqttMesageFactory.newMessage(
        //            FixedHeader.getConnAckFixedHeader(),
        //            new ConnAckVariableHeader(ConnectionStatus.IDENTIFIER_REJECTED, false), null);
        //    client.writeAndFlush(connAckMessage);
        //    client.close();
        //    return;
        //}

        // 如果会话中已经存储了这个新连接的ID，就关闭之前的clientId
        // if an old client with the same ID already exists close its session.
        if (clientIDs.containsKey(connectMessage.getPayload().getClientId())) {
            Log.error("客户端ID{" + connectMessage.getPayload().getClientId() + "}已存在，强制关闭老连接");
            Channel oldChannel = clientIDs.get(connectMessage.getPayload().getClientId()).getClient();
            boolean cleanSession = NettyAttrManager.getAttrCleanSession(oldChannel);
            // clean the subscriptions if the old used a cleanSession = true
            if (cleanSession) {
                cleanSession(connectMessage.getPayload().getClientId());
            }
            oldChannel.close();
        }

        // 若至此没问题，则将新客户端连接加入client的维护列表中
        ConnectionDescriptor connectionDescriptor = new ConnectionDescriptor(connectMessage.getPayload().getClientId(),
                client, connectMessage.getVariableHeader().isCleanSession());
        this.clientIDs.put(connectMessage.getPayload().getClientId(), connectionDescriptor);

        // 处理心跳包时间，把心跳包时长和一些其他属性都添加到会话中，方便以后使用
        int keepAlive = connectMessage.getVariableHeader().getKeepAlive();
        Log.debug("连接的心跳包时长是 {" + keepAlive + "} s");
        NettyAttrManager.setAttrClientId(client, connectMessage.getPayload().getClientId());
        NettyAttrManager.setAttrCleanSession(client, connectMessage.getVariableHeader().isCleanSession());
        // 协议P29规定，在超过1.5个keepAlive的时间以上没收到心跳包PingReq，就断开连接(但这里要注意把单位是s转为ms)
        NettyAttrManager.setAttrKeepAlive(client, keepAlive);
        // 添加心跳机制处理的Handler
        client.pipeline().addFirst("idleStateHandler", new IdleStateHandler(keepAlive, Integer.MAX_VALUE, Integer.MAX_VALUE, TimeUnit.SECONDS));

        // 处理Will flag（遗嘱信息）,协议P26
        if (connectMessage.getVariableHeader().isHasWill()) {
            QoS willQos = connectMessage.getVariableHeader().getWillQoS();
            // 获取遗嘱信息的具体内容
            ByteBuf willPayload = Unpooled.buffer().writeBytes(connectMessage.getPayload().getWillMessage().getBytes());
            WillMessage willMessage = new WillMessage(connectMessage.getPayload().getWillTopic(),
                    willPayload, connectMessage.getVariableHeader().isWillRetain(), willQos);
            // 把遗嘱信息与和其对应的的clientId存储在一起
            willStore.put(connectMessage.getPayload().getClientId(), willMessage);
        }

        // 处理身份验证（userNameFlag和passwordFlag）
        if (connectMessage.getVariableHeader().isHasUsername() && connectMessage.getVariableHeader().isHasPassword()) {
            String username = connectMessage.getPayload().getUsername();
            String pwd = connectMessage.getPayload().getPassword();
            // 此处对用户名和密码做验证
            if (!authenticator.checkValid(username, pwd)) {
                ConnAckMessage connAckMessage = (ConnAckMessage) MqttMesageFactory.newMessage(
                        FixedHeader.getConnAckFixedHeader(),
                        new ConnAckVariableHeader(ConnAckMessage.ConnectionStatus.BAD_USERNAME_OR_PASSWORD, false), null);
                client.writeAndFlush(connAckMessage);
                return;
            }
        }

        // 处理cleanSession为1的情况
        if (connectMessage.getVariableHeader().isCleanSession()) {
            // 移除所有之前的session并开启一个新的，并且原先保存的subscribe之类的都得从服务器删掉
            cleanSession(connectMessage.getPayload().getClientId());
        }

        //TODO 此处生成一个token(以后每次客户端每次请求服务器，都必须先验证此token正确与否)，并把token保存到本地以及传回给客户端
        // 鉴权获取不应该在这里做

//        String token = StringTool.generalRandomString(32);
//        sessionStore.addSession(connectMessage.getClientId(), token);
//        //把荷载封装成json字符串
//        JSONObject jsonObject = new JSONObject();
//        try {
//			jsonObject.put("token", token);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}

        // TODO 连接成功，主动 publish 下发初始化信息，例如用户好友列表，群组等基础信息
        // 上述逻辑可以换 HTTP 协议实现，减少 IM 服务器逻辑复杂性和压力
        // 处理回写的CONNACK,并回写，协议P29
        ConnAckMessage okResp = null;
        // 协议32,session present的处理
        if (!connectMessage.getVariableHeader().isCleanSession() && sessionStore.searchSubscriptions(connectMessage.getPayload().getClientId())) {
            okResp = (ConnAckMessage) MqttMesageFactory.newMessage(
                    FixedHeader.getConnAckFixedHeader(), new ConnAckVariableHeader(ConnAckMessage.ConnectionStatus.ACCEPTED, true), null);
        } else {
            okResp = (ConnAckMessage) MqttMesageFactory.newMessage(
                    FixedHeader.getConnAckFixedHeader(), new ConnAckVariableHeader(ConnAckMessage.ConnectionStatus.ACCEPTED, false), null);
        }
        client.writeAndFlush(okResp);
        Log.info("CONNACK处理完毕并成功发送");
        Log.info("连接的客户端clientId=" + connectMessage.getPayload().getClientId() + ", " +
                "cleanSession为" + connectMessage.getVariableHeader().isCleanSession());

        // 如果cleanSession=0,需要在重连的时候重发同一clientId存储在服务端的离线信息
        if (!connectMessage.getVariableHeader().isCleanSession()) {
            // force the republish of stored QoS1 and QoS2
            republishMessage(connectMessage.getPayload().getClientId());
        }
    }

    /**
     * 清除会话，除了要从订阅树中删掉会话信息，还要从会话存储中删除会话信息
     */
    private void cleanSession(String clientId) {
        // 从订阅树中删掉会话信息
        subscribeStore.removeForClient(clientId);
        // 从会话存储中删除信息
        sessionStore.wipeSubscriptions(clientId);
    }

    /**
     * 在客户端重连以后，针对QoS1和Qos2的消息，重发存储的离线消息
     */
    private void republishMessage(String clientId) {
        // 取出需要重发的消息列表
        // 查看消息列表是否为空，为空则返回
        // 不为空则依次发送消息并从会话中删除此消息
        List<PublishEvent> publishedEvents = messagesStore.listMessagesInSession(clientId);
        if (publishedEvents.isEmpty()) {
            logger.info("没有客户端{" + clientId + "}存储的离线消息");
            return;
        }

        logger.info("重发客户端{" + clientId + "}存储的离线消息");
        for (PublishEvent pubEvent : publishedEvents) {
            boolean dup = true;
            sendPublishMessage(pubEvent.getClientId(),
                    pubEvent.getTopic(),
                    pubEvent.getQos(),
                    Unpooled.buffer().writeBytes(pubEvent.getMessage()),
                    pubEvent.isRetain(),
                    pubEvent.getPacketId(),
                    dup);
            messagesStore.removeMessageInSessionForPublish(clientId, pubEvent.getPacketId());
        }
    }

    /**
     * 取出所有匹配topic的客户端，然后发送public消息给客户端
     */
    private void sendPublishMessage(String topic, QoS originQos, ByteBuf message, boolean retain, boolean dup) {
        for (final Subscription sub : subscribeStore.getClientListFromTopic(topic)) {

            String clientId = sub.getClientId();
            Integer sendPacketId = PacketIdManager.getNextMessageId();
            String publishKey = String.format("%s%d", clientId, sendPacketId);
            QoS qos = originQos;

            // 协议P43提到，假设请求的QoS级别被授权，客户端接收的PUBLISH消息的QoS级别小于或等于这个级别，PUBLISH 消息的级别取决于发布者的原始消息的QoS级别
            if (originQos.ordinal() > sub.getRequestedQos().ordinal()) {
                qos = sub.getRequestedQos();
            }

            PublishMessage publishMessage = (PublishMessage) MqttMesageFactory.newMessage(
                    FixedHeader.getPublishFixedHeader(dup, qos, retain), new PublishVariableHeader(topic, sendPacketId), message);

            if (this.clientIDs == null) {
                throw new RuntimeException("内部错误，clientIDs为null");
            } else {
                logger.debug("clientIDs为{" + this.clientIDs + "}");
            }

            if (this.clientIDs.get(clientId) == null) {
                throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
            } else {
                logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
            }

            if (originQos == QoS.AT_MOST_ONCE) {
                publishMessage = (PublishMessage) MqttMesageFactory.newMessage(
                        FixedHeader.getPublishFixedHeader(dup, qos, retain),
                        new PublishVariableHeader(topic), message);
                // 从会话列表中取出会话，然后通过此会话发送publish消息
                this.clientIDs.get(clientId).getClient().writeAndFlush(publishMessage);
            } else {
                publishKey = String.format("%s%d", clientId, sendPacketId);// 针对每个重新生成key，保证消息ID不会重复
                // 将ByteBuf转变为byte[]
                byte[] messageBytes = new byte[message.readableBytes()];
                message.getBytes(message.readerIndex(), messageBytes);
                PublishEvent storePublishEvent = new PublishEvent(topic, qos, messageBytes, retain, clientId, sendPacketId);

                // 从会话列表中取出会话，然后通过此会话发送publish消息
                this.clientIDs.get(clientId).getClient().writeAndFlush(publishMessage);
                // 存临时Publish消息，用于重发
                messagesStore.storeQosPublishMessage(publishKey, storePublishEvent);
                // 开启Publish重传任务，在制定时间内未收到PubAck包则重传该条Publish信息
                Map<String, Object> jobParam = new HashMap<String, Object>();
                jobParam.put("ProtocolProcess", this);
                jobParam.put("publishKey", publishKey);
                QuartzManager.addJob(publishKey, "publish", publishKey, "publish", RePublishJob.class, 10, 2, jobParam);
            }

            logger.info("服务器发送消息给客户端{" + clientId + "},topic{" + topic + "},qos{" + qos + "}");

            if (!sub.isCleanSession()) {
                //将ByteBuf转变为byte[]
                byte[] messageBytes = new byte[message.readableBytes()];
                message.getBytes(message.readerIndex(), messageBytes);
                PublishEvent newPublishEvt = new PublishEvent(topic, qos, messageBytes, retain, sub.getClientId(), sendPacketId != null ? sendPacketId : 0);
                messagesStore.storeMessageToSessionForPublish(newPublishEvt);
            }
        }
    }

    /**
     * 发送publish消息给指定ID的客户端
     */
    private void sendPublishMessage(String clientId, String topic, QoS qos, ByteBuf message, boolean retain, Integer packetId, boolean dup) {
        logger.info("发送pulicMessage给指定客户端");

        String publishKey = String.format("%s%d", clientId, packetId);

        PublishMessage publishMessage = (PublishMessage) MqttMesageFactory.newMessage(
                FixedHeader.getPublishFixedHeader(dup, qos, retain),
                new PublishVariableHeader(topic, packetId),
                message);

        if (this.clientIDs == null) {
            throw new RuntimeException("内部错误，clientIDs为null");
        } else {
            logger.debug("clientIDs为{" + this.clientIDs + "}");
        }

        if (this.clientIDs.get(clientId) == null) {
            throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
        } else {
            logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
        }

        if (qos == QoS.AT_MOST_ONCE) {
            publishMessage = (PublishMessage) MqttMesageFactory.newMessage(
                    FixedHeader.getPublishFixedHeader(dup, qos, retain),
                    new PublishVariableHeader(topic),
                    message);
            //从会话列表中取出会话，然后通过此会话发送publish消息
            this.clientIDs.get(clientId).getClient().writeAndFlush(publishMessage);
        } else {
            publishKey = String.format("%s%d", clientId, packetId);//针对每个重生成key，保证消息ID不会重复
            //将ByteBuf转变为byte[]
            byte[] messageBytes = new byte[message.readableBytes()];
            message.getBytes(message.readerIndex(), messageBytes);
            PublishEvent storePublishEvent = new PublishEvent(topic, qos, messageBytes, retain, clientId, packetId);

            //从会话列表中取出会话，然后通过此会话发送publish消息
            this.clientIDs.get(clientId).getClient().writeAndFlush(publishMessage);
            //存临时Publish消息，用于重发
            messagesStore.storeQosPublishMessage(publishKey, storePublishEvent);
            //开启Publish重传任务，在制定时间内未收到PubAck包则重传该条Publish信息
            Map<String, Object> jobParam = new HashMap<String, Object>();
            jobParam.put("ProtocolProcess", this);
            jobParam.put("publishKey", publishKey);
            QuartzManager.addJob(publishKey, "publish", publishKey, "publish", RePublishJob.class, 10, 2, jobParam);
        }
    }

    /**
     * 发送保存的Retain消息
     */
    private void sendPublishMessage(String clientId, String topic, QoS qos, ByteBuf message, boolean retain) {
        int packetId = PacketIdManager.getNextMessageId();
        sendPublishMessage(clientId, topic, qos, message, retain, packetId, false);
    }

    /**
     * 回写PubAck消息给发来publish的客户端
     */
    private void sendPubAck(String clientId, Integer packetId) {
        logger.info("发送PubAck消息给客户端");

        Message pubAckMessage = MqttMesageFactory.newMessage(
                FixedHeader.getPubAckFixedHeader(), new PacketIdVariableHeader(packetId), null);

        try {
            if (this.clientIDs == null) {
                throw new RuntimeException("内部错误，clientIDs为null");
            } else {
                logger.debug("clientIDs为{" + this.clientIDs + "}");
            }

            if (this.clientIDs.get(clientId) == null) {
                throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
            } else {
                logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
            }

            this.clientIDs.get(clientId).getClient().writeAndFlush(pubAckMessage);
        } catch (Throwable t) {
            logger.error(null, t);
        }
    }

    /**
     * 回写PubRec消息给发来publish的客户端
     */
    private void sendPubRec(String clientId, Integer packetId) {
        logger.trace("发送PubRec消息给客户端");

        Message pubRecMessage = MqttMesageFactory.newMessage(
                FixedHeader.getPubAckFixedHeader(),
                new PacketIdVariableHeader(packetId),
                null);

        try {
            if (this.clientIDs == null) {
                throw new RuntimeException("内部错误，clientIDs为null");
            } else {
                logger.debug("clientIDs为{" + this.clientIDs + "}");
            }

            if (this.clientIDs.get(clientId) == null) {
                throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
            } else {
                logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
            }

            this.clientIDs.get(clientId).getClient().writeAndFlush(pubRecMessage);
        } catch (Throwable t) {
            logger.error(null, t);
        }
    }

    /**
     * 回写PubRel消息给发来publish的客户端
     */
    private void sendPubRel(String clientId, Integer packetId) {
        logger.trace("发送PubRel消息给客户端");

        Message pubRelMessage = MqttMesageFactory.newMessage(
                FixedHeader.getPubAckFixedHeader(),
                new PacketIdVariableHeader(packetId),
                null);

        try {
            if (this.clientIDs == null) {
                throw new RuntimeException("内部错误，clientIDs为null");
            } else {
                logger.debug("clientIDs为{" + this.clientIDs + "}");
            }

            if (this.clientIDs.get(clientId) == null) {
                throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
            } else {
                logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
            }

            this.clientIDs.get(clientId).getClient().writeAndFlush(pubRelMessage);
        } catch (Throwable t) {
            logger.error(null, t);
        }
    }

    /**
     * 回写PubComp消息给发来publish的客户端
     */
    private void sendPubComp(String clientId, Integer packetId) {
        logger.trace("发送PubComp消息给客户端");

        Message pubcompMessage = MqttMesageFactory.newMessage(
                FixedHeader.getPubAckFixedHeader(), new PacketIdVariableHeader(packetId), null);

        try {
            if (this.clientIDs == null) {
                throw new RuntimeException("内部错误，clientIDs为null");
            } else {
                logger.debug("clientIDs为{" + this.clientIDs + "}");
            }

            if (this.clientIDs.get(clientId) == null) {
                throw new RuntimeException("不能从会话列表{" + this.clientIDs + "}中找到clientId:{" + clientId + "}");
            } else {
                logger.debug("从会话列表{" + this.clientIDs + "}查找到clientId:{" + clientId + "}");
            }

            this.clientIDs.get(clientId).getClient().writeAndFlush(pubcompMessage);
        } catch (Throwable t) {
            logger.error(null, t);
        }
    }

    /**
     * 处理一个单一订阅，存储到会话和订阅树
     */
    private void subscribeSingleTopic(Subscription newSubscription, final String topic) {
        logger.info("订阅topic{" + topic + "},Qos为{" + newSubscription.getRequestedQos() + "}");
        String clientId = newSubscription.getClientId();
        sessionStore.addNewSubscription(newSubscription, clientId);
        subscribeStore.addSubscrpition(newSubscription);
        //TODO 此处还需要将此订阅之前存储的信息发出去
        Collection<IMessageStore.StoredMessage> messages = messagesStore.searchRetained(topic);
        for (IMessageStore.StoredMessage storedMsg : messages) {
            logger.debug("send publish message for topic {" + topic + "}");
            sendPublishMessage(newSubscription.getClientId(), storedMsg.getTopic(), storedMsg.getQos(), Unpooled.buffer().writeBytes(storedMsg.getPayload()), true);
        }
    }
}
