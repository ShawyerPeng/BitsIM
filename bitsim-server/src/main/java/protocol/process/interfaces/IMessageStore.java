package protocol.process.interfaces;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import protocol.message.QoS;
import protocol.process.event.PubRelEvent;
import protocol.process.event.PublishEvent;

/**
 * 消息存储接口
 */
public interface IMessageStore {
    class StoredMessage implements Serializable {
        final String topic;
        final byte[] payload;
        final QoS qos;

        public StoredMessage(byte[] message, QoS qos, String topic) {
            this.qos = qos;
            this.payload = message;
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        public byte[] getPayload() {
            return payload;
        }

        public QoS getQos() {
            return qos;
        }
    }

    /**
     * 初始化存储
     */
    void initStore();

    /**
     * 返回某个clientId的离线消息列表
     */
    List<PublishEvent> listMessagesInSession(String clientId);

    /**
     * 在重发以后，移除publish的离线消息事件
     */
    void removeMessageInSessionForPublish(String clientId, Integer packgeID);

    /**
     * 存储publish的离线消息事件，为CleanSession=0的情况做重发准备
     */
    void storeMessageToSessionForPublish(PublishEvent pubEvent);

    /**
     * 存储Publish的包ID
     */
    void storePublicPacketId(String clientId, Integer packgeID);

    /**
     * 移除Publish的包ID
     */
    void removePublicPacketId(String clientId);

    /**
     * 移除PubRec的包ID
     */
    void removePubRecPacketId(String clientId);

    /**
     * 存储PubRec的包ID
     */
    void storePubRecPacketId(String clientId, Integer packgeID);

    /**
     * 当Qos>0的时候，临时存储Publish消息，用于重发
     */
    void storeQosPublishMessage(String publishKey, PublishEvent pubEvent);

    /**
     * 在收到对应的响应包后，删除Publish消息的临时存储
     */
    void removeQosPublishMessage(String publishKey);

    /**
     * 获取临时存储的Publish消息，在等待时间过后未收到对应的响应包，则重发该Publish消息
     */
    PublishEvent searchQosPublishMessage(String publishKey);

    /**
     * 当Qos=2的时候，临时存储PubRel消息，在未收到PubComp包时用于重发
     */
    void storePubRelMessage(String pubRelKey, PubRelEvent pubRelEvent);

    /**
     * 在收到对应的响应包后，删除PubRel消息的临时存储
     */
    void removePubRelMessage(String pubRelKey);

    /**
     * 获取临时存储的PubRel消息，在等待时间过后未收到对应的响应包，则重发该PubRel消息
     */
    PubRelEvent searchPubRelMessage(String pubRelKey);

    /**
     * 持久化存储保留Retain为1的指定topic的最新信息，该信息会在新客户端订阅某主题的时候发送给此客户端
     */
    void storeRetained(String topic, ByteBuf message, QoS qos);

    /**
     * 删除指定topic的Retain信息
     */
    void cleanRetained(String topic);

    /**
     * 从Retain中搜索对应topic中保存的信息
     */
    Collection<StoredMessage> searchRetained(String topic);
}