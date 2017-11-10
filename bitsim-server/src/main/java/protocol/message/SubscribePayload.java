package protocol.message;

import java.util.List;

/**
 * MQTT协议Subscribe消息类型的荷载
 */
public class SubscribePayload {
    // 主题过滤器列表，它们表示客户端想要订阅的主题
    private List<TopicSubscribe> topicSubscribes;

    public SubscribePayload(List<TopicSubscribe> topicSubscribes) {
        super();
        this.topicSubscribes = topicSubscribes;
    }

    public List<TopicSubscribe> getTopicSubscribes() {
        return topicSubscribes;
    }
}

