//package protocol.processor;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import io.netty.channel.Channel;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import protocol.message.PublishMessage;
//import protocol.message.QoS;
//import protocol.process.NettyAttrManager;
//import protocol.process.store.ClientSession;
//import protocol.process.store.TopicType;
//import service.UserGroupService;
//
///**
// * Publish 过程处理类
// */
//public class PublishProcessor extends MessageProcessor {
//    private final static Logger logger = LoggerFactory.getLogger(PublishProcessor.class);
//
//    @Autowired
//    private UserGroupService userGroupService;
//
//    /**
//     * 存储消息到 MongoDB
//     */
//    public void processPublish(final Channel session, final PublishMessage message) throws Exception {
//        logger.trace("PUB --PUBLISH--> SRV executePublish invoked with {}", message);
//
//        String clientId = NettyAttrManager.getAttrClientId(session);
//        final String topic = message.getVariableHeader().getTopic();
//        final QoS qos = message.getFixedHeader().getQos();
//        final Integer packetId = message.getVariableHeader().getPacketId();
//        logger.info("PUBLISH from clientID <{}> on topic <{}> with QoS {}", clientId, topic, qos);
//
//        if (StringUtils.isBlank(topic)) {
//            // 规定如果是 null topic，则为内置协议
//        } else if (StringUtils.startsWith(topic, TopicType.FRIEND.getType())) {
//            // 发给朋友的消息
//            handleFriendMsg(topic, message);
//        } else if (StringUtils.startsWith(topic, TopicType.GROUP.getType())) {
//            // 发给群组的消息
//            handleGroupMsg(topic, message);
//
//        } else if (StringUtils.startsWith(topic, TopicType.MYSELF.getType())) {
//            // 发给自己的，属于传统的问答模式的消息，这类消息需要直接透传到逻辑层，交由逻辑层处理
//            handleMyselfMsg(topic, session, message);
//        }
//    }
//
//    private void handleFriendMsg(String topic, final PublishMessage message) {
//        logger.info("handleFriendMsg topic:{},msg:{}", topic, message);
//        String[] split = topic.split("\\|");
//        if (split.length == 2) {
//            String toUid = split[1];
//            ClientSession clientSession = sessionStore.sessionForClient(toUid);
//            directSend(clientSession, topic, message.getFixedHeader().getQos(), false,
//                    (int) clientSession.getNextMessageId(), message.getPayload());
//        }
//    }
//
//    private void handleGroupMsg(String topic, final PublishMessage message) {
//        String[] split = topic.split("\\|");
//        if (split.length != 2) {
//            logger.warn("wrong topic for request & response msg");
//            return;
//        }
//        String groupId = split[1];
//        JSONObject jsonObject = userGroupService.listUserIdByGroupId(Integer.parseInt(groupId));
//        JSONArray data = jsonObject.getJSONArray("data");
//        for (int i = 0; i < data.size(); i++) {
//            String uid = data.getString(i);
//            ClientSession clientSession = sessionStore.sessionForClient(uid);
//            directSend(clientSession, topic, message.getFixedHeader().getQos(), false,
//                    (int) clientSession.getNextMessageId(), message.getPayload());
//        }
//    }
//
//    /**
//     * 处理问答类型的消息，消息透传到 logic 层，logic 层返回数据后直接封装到 mqtt publish msg 的 payload 中返回给客户端
//     * 这类消息 Qos 为 0，不保证消息一定到达
//     */
//    private void handleMyselfMsg(String topic, final Channel session, final PublishMessage msg) throws Exception {
//
//    }
//}